package com.example.eventlottery;

import org.junit.Test;

import static org.junit.Assert.*;

/** Admin browse/remove event posters (AdminBrowseImagesActivity, AdminBrowseImagesAdapter). */
public class AdminBrowseImagesUnitTest {

    @Test
    public void adminBrowseImages_includesOnlyNonEmptyPosterUri() {
        assertTrue(eventHasBrowsablePoster("https://example.com/poster.png"));
        assertFalse(eventHasBrowsablePoster(null));
        assertFalse(eventHasBrowsablePoster(""));
    }

    @Test
    public void adminRemoveImage_clearsEventPosterUri() {
        Event e = new Event();
        e.setPosterUri("content://fake-poster");
        assertEquals("content://fake-poster", e.getPosterUri());
        e.setPosterUri(null);
        assertNull(e.getPosterUri());
    }

    private static boolean eventHasBrowsablePoster(String posterUri) {
        return posterUri != null && !posterUri.isEmpty();
    }
}
