package com.example.eventlottery;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Co-organizer IDs on Event; assigned user is not a normal entrant pool member for that event
 * (co-organizer management screen).
 */
public class CoOrganizerAssignmentEventUnitTest {

    @Test
    public void event_coOrganizerIds_canBeCheckedByDeviceId() {
        Event e = new Event();
        e.setCoOrganizerIds(Arrays.asList("co_dev_1", "co_dev_2"));
        assertTrue(e.isCoOrganizer("co_dev_1"));
        assertTrue(e.isCoOrganizer("co_dev_2"));
        assertFalse(e.isCoOrganizer("regular_entrant"));
    }

    @Test
    public void event_emptyCoOrganizerList_nobodyIsCoOrganizer() {
        Event e = new Event();
        e.setCoOrganizerIds(Collections.emptyList());
        assertFalse(e.isCoOrganizer("anyone"));
    }

    @Test
    public void eventDetails_hidesJoinLeave_matchesCoOrganizerFlag() {
        Event e = new Event();
        e.setCoOrganizerIds(Arrays.asList("co_dev_1"));
        assertTrue(EventDetailsActivity.hidesJoinLeaveForCoOrganizer(e, "co_dev_1"));
        assertFalse(EventDetailsActivity.hidesJoinLeaveForCoOrganizer(e, "regular_entrant"));
        assertFalse(EventDetailsActivity.hidesJoinLeaveForCoOrganizer(e, null));
        assertFalse(EventDetailsActivity.hidesJoinLeaveForCoOrganizer(null, "co_dev_1"));
    }
}
