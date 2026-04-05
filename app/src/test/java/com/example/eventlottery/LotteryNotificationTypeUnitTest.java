package com.example.eventlottery;

import org.junit.Test;

import static org.junit.Assert.*;

/** US 01.04.01 / 01.04.02 — lottery win/loss notification types and copy (NotificationHelper). */
public class LotteryNotificationTypeUnitTest {

    @Test
    public void notificationHelper_lotteryWonTypeConstantIsStable() {
        assertEquals("LOTTERY_WON", NotificationHelper.TYPE_LOTTERY_WON);
    }

    @Test
    public void notificationHelper_lotteryLostTypeConstantIsStable() {
        assertEquals("LOTTERY_LOST", NotificationHelper.TYPE_LOTTERY_LOST);
    }

    @Test
    public void notificationHelper_lotteryWinTitleAndMessageNotEmpty() {
        assertNotNull(NotificationHelper.LOTTERY_WIN_TITLE);
        assertFalse(NotificationHelper.LOTTERY_WIN_TITLE.isEmpty());
        assertNotNull(NotificationHelper.LOTTERY_WIN_MESSAGE);
        assertFalse(NotificationHelper.LOTTERY_WIN_MESSAGE.isEmpty());
    }
}
