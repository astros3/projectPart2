package com.example.eventlottery;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for sending in-app notifications to entrants.
 * Notifications are stored in Firestore at users/{deviceId}/notifications.
 * Respects the entrant's notificationsEnabled flag (US 01.04.03).
 * Sends win notifications (US 01.04.01) and loss notifications (US 01.04.02).
 */
public class NotificationHelper {

    /** Notification type for when an entrant wins the lottery (US 01.04.01). */
    public static final String TYPE_LOTTERY_WON  = "LOTTERY_WON";

    /** Notification type for when an entrant loses the lottery (US 01.04.02). */
    public static final String TYPE_LOTTERY_LOST = "LOTTERY_LOST";

    /** Title/body for the “you won the lottery” / sign-up invitation (organizer + automatic draw). */
    public static final String LOTTERY_WIN_TITLE = "You've been selected! 🎉";
    public static final String LOTTERY_WIN_MESSAGE =
            "Congratulations! You were chosen from the waiting list. Open the event to accept or decline your spot.";

    /**
     * Sends the standard lottery-win notification (chosen entrants should sign up / respond).
     * Same payload as after a lottery draw; organizer may resend from Selected Entrants.
     */
    public static void sendLotteryWinNotification(FirebaseFirestore db, String deviceId, String eventId) {
        sendNotification(db, deviceId, TYPE_LOTTERY_WON, LOTTERY_WIN_TITLE, LOTTERY_WIN_MESSAGE, eventId);
    }

    /**
     * Sends a notification to a specific entrant, but only if they have
     * notifications enabled (US 01.04.03).
     *
     * @param db       Firestore instance
     * @param deviceId Target entrant's device ID
     * @param type     Notification type (e.g. TYPE_LOTTERY_WON)
     * @param title    Notification title
     * @param message  Notification message body
     * @param eventId  Related event ID
     */
    public static void sendNotification(FirebaseFirestore db, String deviceId,
                                        String type, String title,
                                        String message, String eventId) {
        // Check opt-out preference before sending (US 01.04.03)
        db.collection("users").document(deviceId).get()
                .addOnSuccessListener(doc -> {
                    if (doc == null || !doc.exists()) return;

                    Entrant entrant = doc.toObject(Entrant.class);
                    if (entrant == null) return;

                    // Respect notification opt-out (US 01.04.03)
                    if (!entrant.isNotificationsEnabled()) return;

                    // Build notification document
                    Map<String, Object> notification = new HashMap<>();
                    notification.put("type", type);
                    notification.put("title", title);
                    notification.put("message", message);
                    notification.put("eventId", eventId);
                    notification.put("timestampMillis", System.currentTimeMillis());
                    notification.put("read", false);

                    db.collection("users")
                            .document(deviceId)
                            .collection("notifications")
                            .add(notification);
                });
    }
}
