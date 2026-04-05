package com.example.eventlottery;

import org.junit.Test;

import static org.junit.Assert.*;

/** Organizer bulk notify: Firestore notification type constants and lottery copy. */
public class OrganizerNotifyTypeConstantsUnitTest {

    @Test
    public void notifyCancelledEntrants_notificationTypeConstantIsStable() {
        assertEquals("CANCELLED_ENTRANT_REJOIN", NotificationHelper.TYPE_CANCELLED_ENTRANT_REJOIN);
    }

    @Test
    public void notifySelectedEntrants_usesLotteryWinNotificationType() {
        assertEquals("LOTTERY_WON", NotificationHelper.TYPE_LOTTERY_WON);
    }

    @Test
    public void notifyWaitingListEntrants_notificationTypeConstantIsStable() {
        assertEquals("WAITING_LIST_UPDATE", NotificationHelper.TYPE_WAITING_LIST_UPDATE);
    }

    @Test
    public void lotteryWinTitleAndMessageStillDefinedForSelectedNotify() {
        assertFalse(NotificationHelper.LOTTERY_WIN_TITLE.isEmpty());
        assertFalse(NotificationHelper.LOTTERY_WIN_MESSAGE.isEmpty());
    }
}
