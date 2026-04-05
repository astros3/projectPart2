package com.example.eventlottery;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

/**
 * Creates and registers the app's notification channel (required on Android 8+).
 * Call createChannel(Context) once at app startup.
 */
public class NotificationChannelHelper {

    public static final String CHANNEL_ID = "event_lottery_notifications";
    private static final String CHANNEL_NAME = "Event Notifications";
    private static final String CHANNEL_DESC = "Lottery results, invitations, and organizer messages";

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
            );
            channel.setDescription(CHANNEL_DESC);
            channel.enableVibration(true);
            channel.enableLights(true);
            channel.setLockscreenVisibility(android.app.Notification.VISIBILITY_PUBLIC);
            channel.setShowBadge(true);
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
}
