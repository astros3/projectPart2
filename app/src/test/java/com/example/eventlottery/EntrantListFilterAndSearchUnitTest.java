package com.example.eventlottery;

import org.junit.Test;

import static org.junit.Assert.*;

/** Entrant home list: registration filter, capacity, keyword and event-type search (EventFilterUtils). */
public class EntrantListFilterAndSearchUnitTest {

    @Test
    public void entrantFilter_registrationOpenOnly_excludesClosedRegistration() {
        long now = System.currentTimeMillis();
        Event open = new Event();
        open.setRegistrationStartMillis(now - 60_000L);
        open.setRegistrationEndMillis(now + 60_000L);

        Event closed = new Event();
        closed.setRegistrationStartMillis(now - 120_000L);
        closed.setRegistrationEndMillis(now - 60_000L);

        EventFilterCriteria c = new EventFilterCriteria();
        c.setRegistrationOpenOnly(true);

        assertTrue(EventFilterUtils.matchesForList(open, c, null, null, false));
        assertFalse(EventFilterUtils.matchesForList(closed, c, null, null, false));
    }

    @Test
    public void entrantFilter_eventExposesCapacity() {
        Event e = new Event();
        e.setCapacity(42);
        assertEquals(42, e.getCapacity());
    }

    @Test
    public void entrantSearch_keywordMatchesTitleOrDescription() {
        Event e = new Event();
        e.setTitle("Summer Music Festival");
        e.setDescription("Outdoor concert");
        EventFilterCriteria c = new EventFilterCriteria();
        c.setKeyword("music");
        assertTrue(EventFilterUtils.matchesForList(e, c, null, null, false));

        c.setKeyword("hiking");
        assertFalse(EventFilterUtils.matchesForList(e, c, null, null, false));
    }

    @Test
    public void entrantSearch_keywordAndEventTypeTag_combine() {
        Event e = new Event();
        e.setTitle("Pool Party");
        e.setDescription("Swimming");
        e.setEventType("Swimming");
        long now = System.currentTimeMillis();
        e.setRegistrationStartMillis(now - 10_000L);
        e.setRegistrationEndMillis(now + 100_000L);

        EventFilterCriteria c = new EventFilterCriteria();
        c.setKeyword("pool");
        c.setEventType("Swimming");
        c.setRegistrationOpenOnly(true);

        assertTrue(EventFilterUtils.matchesForList(e, c, null, null, false));

        c.setEventType("Music");
        assertFalse(EventFilterUtils.matchesForList(e, c, null, null, false));
    }
}
