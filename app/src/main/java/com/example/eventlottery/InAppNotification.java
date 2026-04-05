package com.example.eventlottery;

import com.google.firebase.firestore.DocumentSnapshot;

/**
 * This class models one in-app notification row in users/{deviceId}/notifications,
 * written by NotificationHelper when organizers notify entrants.
 */
public class InAppNotification {

    private String id;
    private String type;
    private String title;
    private String message;
    private String eventId;
    private long timestampMillis;
    private boolean read;

    /**
     * No-arg constructor for Firestore-style construction.
     */
    public InAppNotification() {
    }

    /**
     * Builds an InAppNotification from a Firestore document snapshot.
     * @param doc notification document from users/{deviceId}/notifications
     * @return populated notification, never null
     */
    public static InAppNotification fromSnapshot(DocumentSnapshot doc) {
        InAppNotification n = new InAppNotification();
        n.id = doc.getId();
        n.type = doc.getString("type");
        n.title = doc.getString("title");
        n.message = doc.getString("message");
        n.eventId = doc.getString("eventId");
        Long ts = doc.getLong("timestampMillis");
        n.timestampMillis = ts != null ? ts : 0L;
        Boolean r = doc.getBoolean("read");
        n.read = r != null && r;
        return n;
    }

    /**
     * Returns the Firestore document id for this notification.
     * @return document id
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the notification type string (app-defined).
     * @return type, or empty string if null in storage
     */
    public String getType() {
        return type != null ? type : "";
    }

    /**
     * Returns the notification title.
     * @return title, or empty string if null in storage
     */
    public String getTitle() {
        return title != null ? title : "";
    }

    /**
     * Returns the notification body text.
     * @return message, or empty string if null in storage
     */
    public String getMessage() {
        return message != null ? message : "";
    }

    /**
     * Returns the related event id if any.
     * @return event id, or empty string if null in storage
     */
    public String getEventId() {
        return eventId != null ? eventId : "";
    }

    /**
     * Returns when the notification was created (epoch milliseconds).
     * @return timestamp in ms
     */
    public long getTimestampMillis() {
        return timestampMillis;
    }

    /**
     * Returns whether the user has marked this notification as read.
     * @return true if read
     */
    public boolean isRead() {
        return read;
    }
}
