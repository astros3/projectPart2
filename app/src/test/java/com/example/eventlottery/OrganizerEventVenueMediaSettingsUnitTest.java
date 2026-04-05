package com.example.eventlottery;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Organizer toggles geolocation requirement; updates event poster URI (event edit screen, Event model).
 */
public class OrganizerEventVenueMediaSettingsUnitTest {

    @Test
    public void geolocationRequirement_canBeEnabledAndDisabledOnEvent() {
        Event e = new Event();
        assertFalse(e.isGeolocationRequired());
        e.setGeolocationRequired(true);
        assertTrue(e.isGeolocationRequired());
        e.setGeolocationRequired(false);
        assertFalse(e.isGeolocationRequired());
    }

    @Test
    public void eventPoster_canBeUpdatedWithNewUri() {
        Event e = new Event();
        e.setPosterUri("https://cdn.example.com/poster_v1.png");
        assertEquals("https://cdn.example.com/poster_v1.png", e.getPosterUri());
        e.setPosterUri("https://cdn.example.com/poster_v2.png");
        assertEquals("https://cdn.example.com/poster_v2.png", e.getPosterUri());
    }
}
