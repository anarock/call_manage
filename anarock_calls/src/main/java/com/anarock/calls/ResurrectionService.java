package com.anarock.calls;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import android.widget.Toast;

public class ResurrectionService extends Service {
    private static final int SERVICE_NOTIFICATION_ID = -1;
    public static final String SERVICE_NOTIFICATION_CHANNEL = "SERVICE_NOTIFICATION_CHANNEL";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Notification serviceNotification = createNotification();
            startForeground(SERVICE_NOTIFICATION_ID, serviceNotification);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private Notification createNotification() {
        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel =
                new NotificationChannel(SERVICE_NOTIFICATION_CHANNEL, getString(R.string.app_name),
                        NotificationManager.IMPORTANCE_DEFAULT);
        channel.setDescription("ReactInitializer service notification");
        notificationManager.createNotificationChannel(channel);
        return new NotificationCompat.Builder(this, channel.getId())
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.notification_icon)
                .setColorized(true)
                .setColor(this.getResources().getColor(R.color.primary))
                .setSubText("connected")
                .build();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        start(this);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static void start(Context context) {
        ContextCompat.startForegroundService(context, new Intent(context, ResurrectionService.class));
    }
}
