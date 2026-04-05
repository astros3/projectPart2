package com.example.eventlottery;

import org.junit.Test;

import static org.junit.Assert.*;

/** US 01.04.03 — entrant notification opt-in / opt-out (Entrant.isNotificationsEnabled()). */
public class EntrantNotificationPreferenceUnitTest {

    @Test
    public void notificationsEnabled_defaultsToTrue() {
        Entrant entrant = new Entrant();
        assertTrue(entrant.isNotificationsEnabled());
    }

    @Test
    public void notificationsEnabled_canBeDisabled() {
        Entrant entrant = new Entrant("dev1", "Jane", "jane@test.com", "123", "entrant");
        assertTrue(entrant.isNotificationsEnabled());
        entrant.setNotificationsEnabled(false);
        assertFalse(entrant.isNotificationsEnabled());
    }

    @Test
    public void notificationsEnabled_canBeReEnabled() {
        Entrant entrant = new Entrant();
        entrant.setNotificationsEnabled(false);
        assertFalse(entrant.isNotificationsEnabled());
        entrant.setNotificationsEnabled(true);
        assertTrue(entrant.isNotificationsEnabled());
    }

    @Test
    public void notificationsEnabled_persistsThroughNoArgConstructor() {
        Entrant entrant = new Entrant();
        entrant.setNotificationsEnabled(false);
        assertFalse(entrant.isNotificationsEnabled());
    }
}
