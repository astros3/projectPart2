package com.example.eventlottery;

import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/** Join/leave UI hidden for co-organizers (EventDetailsActivity.hidesJoinLeaveForCoOrganizer). */
public class CoOrganizerJoinGateUnitTest {

    @Test
    public void hidesJoin_whenDeviceIsCoOrganizer() {
        Event e = new Event();
        e.setCoOrganizerIds(Collections.singletonList("co_dev"));
        assertTrue(EventDetailsActivity.hidesJoinLeaveForCoOrganizer(e, "co_dev"));
    }

    @Test
    public void showsJoinPath_whenNotCoOrganizer() {
        Event e = new Event();
        e.setCoOrganizerIds(Collections.singletonList("co_dev"));
        assertFalse(EventDetailsActivity.hidesJoinLeaveForCoOrganizer(e, "entrant_dev"));
    }

    @Test
    public void hidesJoin_nullSafe() {
        assertFalse(EventDetailsActivity.hidesJoinLeaveForCoOrganizer(null, "a"));
        Event e = new Event();
        assertFalse(EventDetailsActivity.hidesJoinLeaveForCoOrganizer(e, null));
    }
}
