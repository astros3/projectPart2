package com.example.eventlottery;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles FCM token refresh and incoming push messages.
 * Token is stored in Firestore so it can be targeted by future server-side sends.
 * Incoming messages are displayed as OS notification banners.
 */
public class MyFirebaseMessagingService extends FirebaseMessagingService {

    /**
     * Called when a new FCM registration token is generated (first launch or token rotation).
     * Saves the token to the user's Firestore document so it can be used to send pushes.
     */
    @Override
    public void onNewToken(String token) {
        super.onNewToken(token);
        String deviceId = DeviceIdManager.getDeviceId(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> update = new HashMap<>();
        update.put("fcmToken", token);

        // Update both collections so organizers and entrants both keep a fresh token
        db.collection("users").document(deviceId).update(update);
        db.collection("organizers").document(deviceId).update(update);
    }

    /**
     * Called when a Data / Notification message is received while the app is in the foreground.
     * For background/killed state, the system shows the notification automatically.
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        String title = null;
        String body = null;

        if (remoteMessage.getNotification() != null) {
            title = remoteMessage.getNotification().getTitle();
            body  = remoteMessage.getNotification().getBody();
        }

        // Fall back to data payload fields
        if (title == null) title = remoteMessage.getData().get("title");
        if (body  == null) body  = remoteMessage.getData().get("message");

        if (title == null) title = getString(R.string.app_name);
        if (body  == null) body  = "";

        postNotification(this, title, body, (int) System.currentTimeMillis());
    }

    /**
     * High-importance heads-up style alert for organizer / lottery / invite messaging.
     * Used for FCM and for mirroring Firestore in-app notifications (EntrantNotificationBridge).
     */
    public static void postNotification(Context context, String title, String message, int id) {
        Intent intent = new Intent(context, EntrantMainScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(
                context, id, intent,
                PendingIntent.FLAG_ONE_SHOT | PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                context, NotificationChannelHelper.CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(message))
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setCategory(NotificationCompat.CATEGORY_MESSAGE)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (manager != null) {
            manager.notify(id, builder.build());
        }
    }
}
