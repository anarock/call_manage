package com.anarock.calls;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

import com.anarock.calls.utilities.CallDetailsQuery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallsDetailsReceiver extends BroadcastReceiver {
    /**
     * com.anarock.broadcast.GET_CALLS_DETAILS_EVENT
     */
    @Override
    public void onReceive(final Context context, Intent intent) {
        sendCallsDetails(context, intent);
    }

    private void sendCallsDetails(final Context context, final Intent intent) {
        int delayMS = intent.getIntExtra("delayMS", 1000);
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
                    sendCallsDetails(context, callsDetails, requestId);
                    return;
                }

                for (int i = 0; i < callsDetails.size(); i += BuildConfig.CALLS_PAGE_SIZE) {
                    ArrayList<Map<String, Object>> results = new ArrayList<>(callsDetails.subList(i, Math.min(callsDetails.size(), i + BuildConfig.CALLS_PAGE_SIZE)));
                    sendCallsDetails(context, results, requestId);
                }
            }
        }, delayMS);
    }

    private void sendCallsDetails(Context context, List<Map<String, Object>> callsDetails, String requestId) {
        Intent intent = new Intent();
        intent.setAction("com.anarock.broadcast.CALLS_DETAILS_EVENT");
        intent.setPackage(BuildConfig.AGENTS_APP_ID + BuildConfig.AGENTS_APP_ID_SUFFIX);

        HashMap<String, Object> data = new HashMap<>();
        data.put("requestId", requestId);
        data.put("callsDetails", callsDetails.toArray());
        intent.putExtra("data", data);

        context.sendBroadcast(intent);
    }
}
