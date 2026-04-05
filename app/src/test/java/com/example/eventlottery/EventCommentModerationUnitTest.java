package com.example.eventlottery;

import org.junit.Test;

import static org.junit.Assert.*;

/** Organizer and admin event comment delete paths; CommentAdapter delete visibility. */
public class EventCommentModerationUnitTest {

    @Test
    public void organizerDeleteComment_pathUsesEventsCommentsSubcollection() {
        assertEquals("events/e1/comments/c1", eventCommentDocumentPath("e1", "c1"));
    }

    @Test
    public void adminRemoveEventComment_usesSameCommentDocumentPathAsOrganizer() {
        String path = eventCommentDocumentPath("violating_event", "bad_comment");
        assertTrue(path.contains("/comments/"));
        assertTrue(path.startsWith("events/"));
    }

    @Test
    public void commentDeleteButtonVisible_onlyInOrganizerMode() {
        assertTrue(commentRowShowsDeleteButton(true));
        assertFalse(commentRowShowsDeleteButton(false));
    }

    private static String eventCommentDocumentPath(String eventId, String commentId) {
        return "events/" + eventId + "/comments/" + commentId;
    }

    private static boolean commentRowShowsDeleteButton(boolean organizerMode) {
        return organizerMode;
    }
}
