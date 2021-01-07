package com.anarock.calls.utilities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.Build;
import android.provider.CallLog.Calls;
import androidx.core.content.ContextCompat;
import android.telephony.PhoneNumberUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallDetailsQuery {
    private static List<Integer> FAILED_CALL_TYPES = new ArrayList<Integer>() {{
        add(Calls.MISSED_TYPE);
        add(Calls.BLOCKED_TYPE);
        add(Calls.REJECTED_TYPE);
    }};

    public static List<Map<String, Object>> getCallsDetails(final Context context, final double lastSyncedCallEndTime) {
        String selection = null;
        String callEndTimeString = String.format("%s + (%s * 1000)", Calls.DATE, Calls.DURATION);
        String sortOrder = callEndTimeString + " DESC LIMIT 1";

        if (lastSyncedCallEndTime != 0) {
            selection = callEndTimeString + " > " + lastSyncedCallEndTime;
            sortOrder = callEndTimeString + " DESC";
        }

        return query(context, selection, sortOrder);
    }

    public static List<Map<String, Object>> getCallsDetailsCompat(final Context context, final double since) {
        String selection = null;
        String sortOrder = Calls.DATE + " DESC LIMIT 1";

        if (since != 0) {
            selection = Calls.DATE + " > " + since;
            sortOrder = Calls.DATE + " DESC";
        }

        return query(context, selection, sortOrder);
    }

    private static List<Map<String, Object>> query(Context context, String selection, String sortOrder) {
        List<Map<String, Object>> callsDetails = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            return callsDetails;
        }

        Cursor cursor = context.getContentResolver().query(
                Calls.CONTENT_URI,
                null,
                selection,
                null,
                sortOrder
        );

        if (cursor == null) {
            return callsDetails;
        }

        while (cursor.moveToNext()) {
            int id = cursor.getInt(cursor.getColumnIndex(Calls._ID));

            String phone = cursor.getString(cursor.getColumnIndex(Calls.NUMBER));
            if (phone == null) continue;

            String postDialDigits = "";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                postDialDigits = cursor.getString(cursor.getColumnIndex(Calls.POST_DIAL_DIGITS));
            }

            // some devices inclclude post-dial digits in NUMBER column instead of  POST_DIAL_DIGITS column
            String numberPostDialDigits = PhoneNumberUtils.extractPostDialPortion(phone);
            if (numberPostDialDigits.length() > 0) {
                postDialDigits = numberPostDialDigits;
                phone = phone.substring(0, phone.indexOf(numberPostDialDigits));
            }

            String formattedPhone = PhoneNumberFormatter.getE164PhoneNumber(context, phone);
            if (null == formattedPhone) continue;

            formattedPhone += postDialDigits;
            double startTime = cursor.getDouble(cursor.getColumnIndex(Calls.DATE));
            int duration = cursor.getInt(cursor.getColumnIndex(Calls.DURATION));
            int callTypeId = cursor.getInt(cursor.getColumnIndex(Calls.TYPE));

            if (callTypeId == Calls.OUTGOING_TYPE) {
                callTypeId *= duration > 0 ? 1 : -1;
            }

            if (FAILED_CALL_TYPES.contains(callTypeId)) {
                duration = 0;
            }

            Map<String, Object> callDetails = new HashMap<>();
            callDetails.put("id", id);
            callDetails.put("phone", formattedPhone);
            callDetails.put("duration", duration);
            callDetails.put("startTime", startTime);
            callDetails.put("callTypeId", callTypeId);
            callsDetails.add(callDetails);
        }

        cursor.close();

        return callsDetails;
    }
}
