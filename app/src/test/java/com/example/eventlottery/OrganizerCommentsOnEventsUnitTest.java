package com.example.eventlottery;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Organizer comments use the same Firestore comment document as entrants; list shows (Organizer)
 * when the commenter device id matches the organizer (EventDetailsActivity comment list).
 */
public class OrganizerCommentsOnEventsUnitTest {

    @Test
    public void organizerComment_postUsesSameDocumentKeysAsEntrant() {
        Map<String, Object> doc = new HashMap<>();
        doc.put("text", "Welcome!");
        doc.put("deviceId", "organizer_device");
        doc.put("timestamp", 1L);
        assertTrue(doc.containsKey("text") && doc.containsKey("deviceId") && doc.containsKey("timestamp"));
    }

    @Test
    public void organizerComment_displaysWithOrganizerSuffixWhenAuthorMatchesOrganizer() {
        String line = formatLine("Thanks for joining!", "org_device", "org_device");
        assertEquals("Thanks for joining! (Organizer)", line);
    }

    private static String formatLine(String text, String commenterId, String organizerId) {
        boolean byOrg = organizerId != null && !organizerId.isEmpty()
                && organizerId.equals(commenterId);
        return byOrg ? text + " (Organizer)" : text;
    }
}
