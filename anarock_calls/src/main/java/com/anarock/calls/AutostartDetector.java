package com.anarock.calls;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.telephony.TelephonyManager;

import com.bugsnag.android.Bugsnag;

public class AutostartDetector extends BroadcastReceiver {
    private final static String ACTION_EXPLICIT_BROADCAST = "com.anarock.calls.ACTION_AUTO_START_TIMEOUT";
    private final static int REQUEST_CODE_EXPLICIT_BROADCAST = 1;
    private final static long EXPLICIT_INTENT_DELAY = 30000L;

    private final static String AUTO_START_PREFERENCES = "AUTO_START_PREFERENCES";
    final static String AUTO_START_FAILURE = "AUTO_START_FAILURE";
    private final static String PREF_AUTOSTART_ENABLED = "PREF_AUTOSTART_ENABLED";
    private final static String AUTO_START_NOTIFICATION_CHANNEL_ID = "AUTO_START_CHANNEL";
    private final static int AUTO_START_NOTIFICATION_ID = 1;
    private final static String TEST_PHONE = "+919513686515";


    static boolean isAutoStartPermissionGranted(Context context) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AUTO_START_PREFERENCES, Context.MODE_PRIVATE);
        return sharedPreferences.getBoolean(PREF_AUTOSTART_ENABLED, false);
    }

    public static void testAutoStart(Context context) {
        long now = System.currentTimeMillis();

        Intent explicitIntent = new Intent(ACTION_EXPLICIT_BROADCAST);
        explicitIntent.setComponent(new ComponentName(context, AutostartDetector.class));

        PendingIntent explicitPendingIntent =
                PendingIntent.getBroadcast(context, REQUEST_CODE_EXPLICIT_BROADCAST, explicitIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        context.getSharedPreferences(AUTO_START_PREFERENCES, Context.MODE_PRIVATE).edit().putBoolean(PREF_AUTOSTART_ENABLED, false).apply();

        alarmManager.set(AlarmManager.RTC_WAKEUP, now + EXPLICIT_INTENT_DELAY, explicitPendingIntent);

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + TEST_PHONE));
            context.startActivity(callIntent);
        }
    }

    private static void showTestNotification(Context context) {
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(AUTO_START_NOTIFICATION_CHANNEL_ID, context.getString(R.string.app_name),
                            NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription("ReactInitializer service notification");
            notificationManager.createNotificationChannel(channel);
        }
        Notification notification = new NotificationCompat.Builder(context, AUTO_START_NOTIFICATION_CHANNEL_ID)
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.notification_icon)
                .setColorized(true)
                .setColor(context.getResources().getColor(R.color.primary))
                .setSubText("Testing permissions")
                .setContentText("Running tests. Please wait till the test is complete.")
                .setProgress(1, 0, true)
                .setOngoing(true)
                .build();
        notificationManager.notify(AUTO_START_NOTIFICATION_ID, notification);
    }

    static void openApp(Context context, boolean autoStartFailure) {
        Intent openIntent = new Intent(context, MainActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        Bundle extras = new Bundle();
        extras.putBoolean(AUTO_START_FAILURE, autoStartFailure);
        openIntent.putExtras(extras);
        context.startActivity(openIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(AUTO_START_PREFERENCES, Context.MODE_PRIVATE);
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        String action = intent.getAction();
        if (TelephonyManager.ACTION_PHONE_STATE_CHANGED.equals(action)) {
            String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
            String phone = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
            if ((phone != null) && phone.equals(TEST_PHONE)) {
                if (TelephonyManager.EXTRA_STATE_OFFHOOK.equals(state)) {
                    showTestNotification(context);
                    SelfKiller.killSelf(context);
                } else if (TelephonyManager.EXTRA_STATE_IDLE.equals(state) && ActivityTracker.getInstance().getCreatedActivities().size() == 0) {
                    sharedPreferences.edit().putBoolean(PREF_AUTOSTART_ENABLED, true).apply();
                    notificationManager.cancel(AUTO_START_NOTIFICATION_ID);
                    openApp(context, false);
                }
            }
        } else if (ACTION_EXPLICIT_BROADCAST.equals(action) && !isAutoStartPermissionGranted(context) && ActivityTracker.getInstance().getCreatedActivities().size() == 0) {
            Bugsnag.notify(new Exception("Auto Start test failed"));
            notificationManager.cancel(AUTO_START_NOTIFICATION_ID);
            openApp(context, true);
        }
    }
}
