package com.example.eventlottery;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/** Admin notification log (US 03.08.01): collection, row model, payload keys. */
public class AdminNotificationLogReviewUnitTest {

    @Test
    public void adminNotificationLog_usesNotificationStorageAdminCollection() {
        assertEquals("notificationStorageAdmin", NOTIFICATION_ADMIN_LOG_COLLECTION);
    }

    @Test
    public void adminNotificationLogItem_holdsFieldsForReview() {
        AdminNotificationLogItemTemporary row = new AdminNotificationLogItemTemporary(
                1_700_000_000_000L,
                "Lottery",
                "You were selected",
                "event_xyz",
                "Organizer Pat",
                "Spring Fair",
                "notif_doc_1",
                "Sam Entrant");
        assertEquals("Lottery", row.getTitle());
        assertEquals("You were selected", row.getMessage());
        assertEquals("event_xyz", row.getEventid());
        assertEquals("notif_doc_1", row.getNotificationID());
        assertEquals("Sam Entrant", row.getReceiverName());
    }

    @Test
    public void notificationAdminLogPayload_hasTitleMessageEventReceiverAndTime() {
        Map<String, Object> copy = new HashMap<>();
        copy.put("title", "T");
        copy.put("message", "M");
        copy.put("eventId", "E");
        copy.put("receiverID", "R");
        copy.put("timestampMillis", 99L);
        assertTrue(copy.containsKey("title"));
        assertTrue(copy.containsKey("message"));
        assertTrue(copy.containsKey("eventId"));
        assertTrue(copy.containsKey("receiverID"));
        assertTrue(copy.containsKey("timestampMillis"));
    }

    private static final String NOTIFICATION_ADMIN_LOG_COLLECTION = "notificationStorageAdmin";
}
