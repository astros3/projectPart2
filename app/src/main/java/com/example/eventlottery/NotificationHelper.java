package com.example.eventlottery;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for sending in-app notifications to entrants.
 * Notifications are stored in Firestore at users/{deviceId}/notifications.
 * Respects the entrant's notificationsEnabled flag (US 01.04.03).
 * Sends win notifications (US 01.04.01), loss notifications (US 01.04.02),
 * and private event invite notifications (US 01.05.06).
 */
public class NotificationHelper {

    /** Notification type for when an entrant wins the lottery (US 01.04.01). */
    public static final String TYPE_LOTTERY_WON  = "LOTTERY_WON";

    /** Notification type for when an entrant loses the lottery (US 01.04.02). */
    public static final String TYPE_LOTTERY_LOST = "LOTTERY_LOST";

    /** Notification type for private event waiting list invitation (US 01.05.06). */
    public static final String TYPE_PRIVATE_INVITE = "PRIVATE_INVITE";

    /** Title/body for the lottery win notification. */
    public static final String LOTTERY_WIN_TITLE = "You've been selected! 🎉";
    public static final String LOTTERY_WIN_MESSAGE =
            "Congratulations! You were chosen from the waiting list. Open the event to accept or decline your spot.";

    /** Title/body for the private event invite notification (US 01.05.06). */
    public static final String PRIVATE_INVITE_TITLE = "You've been invited! 🔒";
    public static final String PRIVATE_INVITE_MESSAGE =
            "You have been personally invited to join the waiting list for a private event. Open the event to accept or decline.";

    /**
     * Sends the standard lottery-win notification (US 01.04.01).
     * Same payload as after a lottery draw; organizer may resend from Selected Entrants.
     */
    public static void sendLotteryWinNotification(FirebaseFirestore db,
                                                  String deviceId,
                                                  String eventId) {
        sendNotification(db, deviceId, TYPE_LOTTERY_WON,
                LOTTERY_WIN_TITLE, LOTTERY_WIN_MESSAGE, eventId);
    }

    /**
     * Sends a private event waiting list invitation notification (US 01.05.06).
     * Respects notification opt-out (US 01.04.03).
     *
     * @param db       Firestore instance
     * @param deviceId Target entrant's device ID
     * @param eventId  The private event's ID
     */
    public static void sendPrivateInviteNotification(FirebaseFirestore db,
                                                     String deviceId,
                                                     String eventId) {
        sendNotification(db, deviceId, TYPE_PRIVATE_INVITE,
                PRIVATE_INVITE_TITLE, PRIVATE_INVITE_MESSAGE, eventId);
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

                    NotificationMAINstorageForAdmin(db, title, message, eventId, deviceId);
                });
    }

    /**
     * Stores a copy of every notification in a top-level collection for admin log review
     * (US 03.08.01).
     */
    public static void NotificationMAINstorageForAdmin(FirebaseFirestore db, String title,
                                                       String message, String eventId,
                                                       String receiverID) {
        Map<String, Object> notificationMAINstorage = new HashMap<>();
        notificationMAINstorage.put("title", title);
        notificationMAINstorage.put("message", message);
        notificationMAINstorage.put("eventId", eventId);
        notificationMAINstorage.put("receiverID", receiverID);
        notificationMAINstorage.put("timestampMillis", System.currentTimeMillis());

        db.collection("notificationStorageAdmin")
                .add(notificationMAINstorage);
    }
}