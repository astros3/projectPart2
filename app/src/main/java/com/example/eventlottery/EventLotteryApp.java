package com.example.eventlottery;

import android.app.Application;

/**
 * Registers the notification channel once and attaches the global entrant notification bridge.
 */
public class EventLotteryApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        NotificationChannelHelper.createChannel(this);
        EntrantNotificationBridge.tryRegisterAfterEntrantCheck(this);
    }
}
