package com.anarock.calls.utilities;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.CallLog.Calls;
import android.support.v4.content.ContextCompat;
import android.telecom.TelecomManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            String postDialDigits = cursor.getString(cursor.getColumnIndex(Calls.POST_DIAL_DIGITS));

            String[] numberParts = splitNumberAndPostDialDigits(phone);
            if (numberParts != null) {
                phone = numberParts[0];
                postDialDigits = numberParts[1];
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

    private static String[] splitNumberAndPostDialDigits(String phone) {
        // splits on first wait/pause character: "111,222;333,444" -> ["111", ",222;333,444"]
        Pattern p = Pattern.compile(String.format("^(.+?)([%s%s].+)$", TelecomManager.DTMF_CHARACTER_PAUSE, TelecomManager.DTMF_CHARACTER_WAIT));
        Matcher m = p.matcher(phone);

        if (!m.matches()) {
            return null;
        }

        phone = m.group(1);
        String postDialDigits = m.group(2);

        return new String[]{phone, postDialDigits};
    }
}
