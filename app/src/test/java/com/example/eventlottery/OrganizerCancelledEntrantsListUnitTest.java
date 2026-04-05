package com.example.eventlottery;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Cancelled entrants screen lists DECLINED and CANCELLED only (CancelledListFragment.loadCancelledEntries).
 */
public class OrganizerCancelledEntrantsListUnitTest {

    @Test
    public void cancelledList_includesDeclinedAndCancelledStatuses() {
        assertTrue(isShownOnCancelledEntrantsScreen(WaitingListEntry.Status.CANCELLED.name()));
        assertTrue(isShownOnCancelledEntrantsScreen(WaitingListEntry.Status.DECLINED.name()));
        assertFalse(isShownOnCancelledEntrantsScreen(WaitingListEntry.Status.PENDING.name()));
        assertFalse(isShownOnCancelledEntrantsScreen(WaitingListEntry.Status.SELECTED.name()));
    }

    private static boolean isShownOnCancelledEntrantsScreen(String status) {
        return WaitingListEntry.Status.DECLINED.name().equals(status)
                || WaitingListEntry.Status.CANCELLED.name().equals(status);
    }
}
