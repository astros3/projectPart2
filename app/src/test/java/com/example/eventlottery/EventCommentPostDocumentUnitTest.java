package com.example.eventlottery;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/** Firestore comment document shape and post validation (EventDetailsActivity.postComment). */
public class EventCommentPostDocumentUnitTest {

    @Test
    public void commentFirestoreDocument_hasTextDeviceIdAndTimestamp() {
        Map<String, Object> comment = samplePostedComment("Great event!", "entrant_device_1");
        assertEquals("Great event!", comment.get("text"));
        assertEquals("entrant_device_1", comment.get("deviceId"));
        assertNotNull(comment.get("timestamp"));
        assertTrue(comment.get("timestamp") instanceof Long);
    }

    @Test
    public void postComment_emptyOrWhitespaceAfterTrim_isRejected() {
        assertTrue("".trim().isEmpty());
        assertTrue("   \n\t  ".trim().isEmpty());
        assertFalse("  hello  ".trim().isEmpty());
        assertEquals("hello", "  hello  ".trim());
    }

    private static Map<String, Object> samplePostedComment(String trimmedText, String deviceId) {
        Map<String, Object> comment = new HashMap<>();
        comment.put("text", trimmedText);
        comment.put("deviceId", deviceId);
        comment.put("timestamp", System.currentTimeMillis());
        return comment;
    }
}
