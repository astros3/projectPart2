package com.example.eventlottery;

import com.google.firebase.firestore.DocumentSnapshot;

/**
 * One row in {@code users/{deviceId}/notifications}, written by {@link NotificationHelper}.
 */
public class InAppNotification {

    private String id;
    private String type;
    private String title;
    private String message;
    private String eventId;
    private long timestampMillis;
    private boolean read;

    public InAppNotification() {
    }

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

    public String getId() {
        return id;
    }

    public String getType() {
        return type != null ? type : "";
    }

    public String getTitle() {
        return title != null ? title : "";
    }

    public String getMessage() {
        return message != null ? message : "";
    }

    public String getEventId() {
        return eventId != null ? eventId : "";
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }

    public boolean isRead() {
        return read;
    }
}
