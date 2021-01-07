package com.anarock.calls;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.anarock.calls.utilities.CallDetailsQuery;
import com.bugsnag.android.Bugsnag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallsDetailsReceiver extends BroadcastReceiver {
    public static final ArrayList<String> WHITELISTED_PACKAGES = new ArrayList<String>() {{
        add("com.anarock.agentsapp");
        add("com.anarock.cpsourcing");
    }};

    /**
     * com.anarock.broadcast.GET_CALLS_DETAILS_EVENT
     */
    @Override
    public void onReceive(final Context context, Intent intent) {
        sendCallsDetails(context, intent);
    }

    private void sendCallsDetails(final Context context, final Intent intent) {
        int delayMS = intent.getIntExtra("delayMS", 1000);
        final String sourceId = intent.getStringExtra("sourceId");

        if(!WHITELISTED_PACKAGES.contains(sourceId))  {
            Bugsnag.notify(new Exception(String.format("Unidentified %s package requested call logs", sourceId)));
            return;
        }
        final String requestId = intent.getStringExtra("requestId");

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Map<String, Object>> callsDetails;
                if (intent.hasExtra("lastSyncedCallEndTime")) {
                    double lastSyncedCallEndTime = intent.getDoubleExtra("lastSyncedCallEndTime", 0);
                    callsDetails = CallDetailsQuery.getCallsDetails(context, lastSyncedCallEndTime);
                } else {
                    double since = intent.getDoubleExtra("since", 0);
                    callsDetails = CallDetailsQuery.getCallsDetailsCompat(context, since);
                }

                if (callsDetails.size() == 0) {
                    sendCallsDetails(context, callsDetails, sourceId, requestId);
                    return;
                }

                for (int i = 0; i < callsDetails.size(); i += BuildConfig.CALLS_PAGE_SIZE) {
                    ArrayList<Map<String, Object>> results = new ArrayList<>(callsDetails.subList(i, Math.min(callsDetails.size(), i + BuildConfig.CALLS_PAGE_SIZE)));
                    sendCallsDetails(context, results, sourceId, requestId);
                }
            }
        }, delayMS);
    }

    private void sendCallsDetails(Context context, List<Map<String, Object>> callsDetails, String sourceId, String requestId) {
        Intent intent = new Intent();
        intent.setAction("com.anarock.broadcast.CALLS_DETAILS_EVENT");
        String packageName = String.format("%s%s", sourceId, BuildConfig.SOURCE_ID_SUFFIX);
        intent.setPackage(packageName);

        HashMap<String, Object> data = new HashMap<>();
        data.put("requestId", requestId);
        data.put("callsDetails", callsDetails.toArray());
        intent.putExtra("data", data);

        context.sendBroadcast(intent);
    }
}
