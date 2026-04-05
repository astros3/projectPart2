package com.example.eventlottery;

import org.junit.Test;

import static org.junit.Assert.*;

/** Event.isRegistrationOpen() — registration start/end millis window. */
public class EventRegistrationWindowUnitTest {

    @Test
    public void isRegistrationOpen_whenWindowIncludesFuture_returnsTrue() {
        Event event = new Event();
        event.setRegistrationStartMillis(0);
        event.setRegistrationEndMillis(Long.MAX_VALUE);
        assertTrue(event.isRegistrationOpen());
    }

    @Test
    public void isRegistrationOpen_whenWindowAlreadyEnded_returnsFalse() {
        Event event = new Event();
        event.setRegistrationStartMillis(0);
        event.setRegistrationEndMillis(1);
        assertFalse(event.isRegistrationOpen());
    }
}
