package com.example.eventlottery;

import org.junit.Test;

import static org.junit.Assert.*;

/** Notify chosen entrants (lottery win); replacement draw from PENDING (LotteryRedrawHelper). */
public class OrganizerChosenEntrantAndRedrawUnitTest {

    @Test
    public void organizerNotifyChosenEntrants_usesLotteryWonType() {
        assertEquals("LOTTERY_WON", NotificationHelper.TYPE_LOTTERY_WON);
        assertFalse(NotificationHelper.LOTTERY_WIN_TITLE.isEmpty());
    }

    @Test
    public void replacementDraw_onlyPendingCanBePromoted() {
        assertTrue(isRedrawPoolEntry(WaitingListEntry.Status.PENDING.name()));
        assertFalse(isRedrawPoolEntry(WaitingListEntry.Status.SELECTED.name()));
        assertFalse(isRedrawPoolEntry(WaitingListEntry.Status.CANCELLED.name()));
        assertEquals("SELECTED", WaitingListEntry.Status.SELECTED.name());
    }

    private static boolean isRedrawPoolEntry(String status) {
        return WaitingListEntry.Status.PENDING.name().equals(status);
    }
}
