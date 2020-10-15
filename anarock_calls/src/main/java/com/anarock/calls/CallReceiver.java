package com.anarock.calls;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.telephony.TelephonyManager;

import com.anarock.calls.utilities.CallDetailsQuery;
import com.anarock.calls.utilities.PhoneNumberFormatter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CallReceiver extends BroadcastReceiver {
    private static final String OUTGOING = "outgoing";
    private static final String RINGING = "ringing";
    private static final String OFF_HOOK = "off_hook";
    private static final String IDLE = "idle";

    private static final String KEY_PAYLOAD = "payload";
    private static int prevCallId;
    private static int sendCallDetailsRetriesMax = 5;

    private Context context;
    private int sendCallDetailsRetries;

    /**
     * android.intent.action.PHONE_STATE
     * android.intent.action.NEW_OUTGOING_CALL
     */
    @Override
    public void onReceive(final Context context, Intent intent) {
        this.context = context;

        sendCallDetailsRetries = 0;

        final String eventType = getEventType(intent);
        if (eventType == null) {
            return;
        }

        String phone = getPhoneNumber(eventType, intent);
        if (phone == null) {
            return;
        }

        final Map<String, Object> data = new HashMap<>();
        data.put("phone", phone);

        if (!eventType.equals(IDLE)) {
            sendEvent(eventType, data);
            return;
        }

        sendCallDetails(eventType, data);
    }

    private void sendCallDetails(final String eventType, final Map<String, Object> data) {
        if (sendCallDetailsRetries++ > sendCallDetailsRetriesMax) {
            sendEvent(eventType, data);
            return;
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                List<Map<String, Object>> callsDetails = CallDetailsQuery.getCallsDetails(context, 0);

                if (callsDetails.size() > 0) {
                    Map<String, Object> callDetails = callsDetails.get(0);

                    int callId = (int) callDetails.get("id");
                    if (callId != prevCallId) {
                        prevCallId = callId;
                        sendEvent(eventType, callDetails);
                        return;
                    }
                }

                sendCallDetails(eventType, data);
            }
        }, 1000);
    }

    public static Intent getCallEventIntent(String eventType, Map<String, Object> payload) {
        HashMap<String, Object> eventData = new HashMap<>();
        eventData.put("type", eventType);
        eventData.put(KEY_PAYLOAD, payload);
        Intent intent = new Intent();
        intent.setPackage(BuildConfig.AGENTS_APP_ID + BuildConfig.AGENTS_APP_ID_SUFFIX);
        intent.setAction("com.anarock.broadcast.CALL_EVENT");
        intent.putExtra("data", eventData);
        return intent;
    }

    private void sendEvent(String eventType, Map<String, Object> payload) {
        Intent intent = getCallEventIntent(eventType, payload);
        context.sendBroadcast(intent);
    }

    private String getPhoneNumber(String eventType, Intent intent) {
        switch (eventType) {
            case OUTGOING:
                return PhoneNumberFormatter.getE164PhoneNumber(context, intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER));
            case RINGING:
            case OFF_HOOK:
            case IDLE:
                return PhoneNumberFormatter.getE164PhoneNumber(context, intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER));
        }
        return null;
    }

    private String getEventType(Intent intent) {
        String type = null;
        if (Intent.ACTION_NEW_OUTGOING_CALL.equals(intent.getAction())) {
            type = OUTGOING;
        } else if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(intent.getAction())) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            if (TelephonyManager.EXTRA_STATE_RINGING.equals(state)) {
                type = RINGING;
            } else if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
                type = OFF_HOOK;
            } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state)) {
                type = IDLE;
            }
        }
        return type;
    }
}
