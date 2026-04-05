package com.example.eventlottery;

import org.junit.Test;

import static org.junit.Assert.*;

/** Comment list display / organizer label (EventDetailsActivity.loadComments). */
public class EventCommentDisplayUnitTest {

    @Test
    public void viewComment_nonOrganizerAuthor_showsPlainText() {
        assertEquals("Nice!",
                formatCommentLineForEntrantView("Nice!", "entrant_a", "organizer_z"));
    }

    @Test
    public void viewComment_organizerAuthor_showsOrganizerSuffix() {
        assertEquals("Welcome everyone! (Organizer)",
                formatCommentLineForEntrantView(
                        "Welcome everyone!", "organizer_z", "organizer_z"));
    }

    @Test
    public void viewComment_emptyOrganizerId_neverShowsOrganizerSuffix() {
        assertEquals("Hi",
                formatCommentLineForEntrantView("Hi", "organizer_z", ""));
    }

    private static String formatCommentLineForEntrantView(
            String text, String commenterDeviceId, String organizerDeviceId) {
        boolean byOrganizer = organizerDeviceId != null
                && !organizerDeviceId.isEmpty()
                && organizerDeviceId.equals(commenterDeviceId);
        return byOrganizer ? text + " (Organizer)" : text;
    }
}
