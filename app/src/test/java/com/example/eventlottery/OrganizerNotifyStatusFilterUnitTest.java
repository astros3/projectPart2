package com.example.eventlottery;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Status filters for organizer notify flows through cancelled entrants and selected entrants,
 */
public class OrganizerNotifyStatusFilterUnitTest {

    @Test
    public void cancelledListNotify_includesCancelledAndDeclinedOnly() {
        assertTrue(shouldNotifyCancelledListEntry(WaitingListEntry.Status.CANCELLED.name()));
        assertTrue(shouldNotifyCancelledListEntry(WaitingListEntry.Status.DECLINED.name()));
        assertFalse(shouldNotifyCancelledListEntry(WaitingListEntry.Status.PENDING.name()));
        assertFalse(shouldNotifyCancelledListEntry(WaitingListEntry.Status.SELECTED.name()));
        assertFalse(shouldNotifyCancelledListEntry(WaitingListEntry.Status.ACCEPTED.name()));
    }

    @Test
    public void selectedListNotify_includesSelectedStatusOnly() {
        assertTrue(shouldNotifySelectedListEntry(WaitingListEntry.Status.SELECTED.name()));
        assertFalse(shouldNotifySelectedListEntry(WaitingListEntry.Status.PENDING.name()));
        assertFalse(shouldNotifySelectedListEntry(WaitingListEntry.Status.CANCELLED.name()));
        assertFalse(shouldNotifySelectedListEntry(WaitingListEntry.Status.DECLINED.name()));
    }

    @Test
    public void waitingListNotify_includesPendingStatusOnly() {
        assertTrue(shouldNotifyWaitingListEntry(WaitingListEntry.Status.PENDING.name()));
        assertFalse(shouldNotifyWaitingListEntry(WaitingListEntry.Status.SELECTED.name()));
        assertFalse(shouldNotifyWaitingListEntry(WaitingListEntry.Status.CANCELLED.name()));
        assertFalse(shouldNotifyWaitingListEntry(WaitingListEntry.Status.DECLINED.name()));
    }

    private static boolean shouldNotifyCancelledListEntry(String status) {
        return WaitingListEntry.Status.CANCELLED.name().equals(status)
                || WaitingListEntry.Status.DECLINED.name().equals(status);
    }

    private static boolean shouldNotifySelectedListEntry(String status) {
        return WaitingListEntry.Status.SELECTED.name().equals(status);
    }

    private static boolean shouldNotifyWaitingListEntry(String status) {
        return WaitingListEntry.Status.PENDING.name().equals(status);
    }
}
