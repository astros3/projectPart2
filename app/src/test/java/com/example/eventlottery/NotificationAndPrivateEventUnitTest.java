package com.example.eventlottery;

import com.google.firebase.Timestamp;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for notification preferences and private event invite functionality.
 * Covers US 01.04.01, 01.04.02, 01.04.03, 01.05.05, 01.05.06, 01.05.07.
 */
public class NotificationAndPrivateEventUnitTest {

    // ─── US 01.04.03 — Notification opt-out ───────────────────────────────────

    /**
     * US 01.04.03: Notifications enabled by default so entrants receive
     * win/loss notifications without needing to opt in first.
     */
    @Test
    public void notificationsEnabled_defaultsToTrue() {
        Entrant entrant = new Entrant();
        assertTrue(entrant.isNotificationsEnabled());
    }

    /**
     * US 01.04.03: Entrant can opt out of notifications.
     * NotificationHelper checks this flag before sending.
     */
    @Test
    public void notificationsEnabled_canBeDisabled() {
        Entrant entrant = new Entrant("dev1", "Jane", "jane@test.com", "123", "entrant");
        assertTrue(entrant.isNotificationsEnabled());
        entrant.setNotificationsEnabled(false);
        assertFalse(entrant.isNotificationsEnabled());
    }

    /**
     * US 01.04.03: Entrant can re-enable notifications after opting out.
     */
    @Test
    public void notificationsEnabled_canBeReEnabled() {
        Entrant entrant = new Entrant();
        entrant.setNotificationsEnabled(false);
        assertFalse(entrant.isNotificationsEnabled());
        entrant.setNotificationsEnabled(true);
        assertTrue(entrant.isNotificationsEnabled());
    }

    /**
     * US 01.04.03: Notification preference persists through no-arg constructor
     * + setter (Firestore deserialization path).
     */
    @Test
    public void notificationsEnabled_persistsThroughNoArgConstructor() {
        Entrant entrant = new Entrant();
        entrant.setNotificationsEnabled(false);
        assertFalse(entrant.isNotificationsEnabled());
    }

    // ─── US 01.04.01 / 01.04.02 — Notification type constants ────────────────

    /**
     * US 01.04.01: Lottery win notification type constant must be stable —
     * stored in Firestore and compared in EntrantNotificationsActivity.
     */
    @Test
    public void notificationHelper_lotteryWonTypeConstantIsStable() {
        assertEquals("LOTTERY_WON", NotificationHelper.TYPE_LOTTERY_WON);
    }

    /**
     * US 01.04.02: Lottery lost notification type constant must be stable.
     */
    @Test
    public void notificationHelper_lotteryLostTypeConstantIsStable() {
        assertEquals("LOTTERY_LOST", NotificationHelper.TYPE_LOTTERY_LOST);
    }

    /**
     * US 01.04.01: Lottery win notification title and message are not empty.
     */
    @Test
    public void notificationHelper_lotteryWinTitleAndMessageNotEmpty() {
        assertNotNull(NotificationHelper.LOTTERY_WIN_TITLE);
        assertFalse(NotificationHelper.LOTTERY_WIN_TITLE.isEmpty());
        assertNotNull(NotificationHelper.LOTTERY_WIN_MESSAGE);
        assertFalse(NotificationHelper.LOTTERY_WIN_MESSAGE.isEmpty());
    }

    // ─── US 01.05.05 — Lottery selection criteria ─────────────────────────────

    /**
     * US 01.05.05: Event selection criteria can be set and retrieved correctly.
     */
    @Test
    public void event_selectionCriteria_canBeSetAndRetrieved() {
        Event event = new Event();
        List<String> criteria = Arrays.asList("Must be 18+", "Beginner level only");
        event.setSelectionCriteria(criteria);
        assertEquals(2, event.getSelectionCriteria().size());
        assertEquals("Must be 18+", event.getSelectionCriteria().get(0));
        assertEquals("Beginner level only", event.getSelectionCriteria().get(1));
    }

    /**
     * US 01.05.05: Selection criteria defaults to an empty list, not null,
     * so UI code can safely call .size() and .isEmpty() without null checks.
     */
    @Test
    public void event_selectionCriteria_defaultsToEmptyList() {
        Event event = new Event();
        assertNotNull(event.getSelectionCriteria());
        assertTrue(event.getSelectionCriteria().isEmpty());
    }

    /**
     * US 01.05.05: Setting null criteria falls back to empty list safely.
     */
    @Test
    public void event_setSelectionCriteria_nullFallsBackToEmptyList() {
        Event event = new Event();
        event.setSelectionCriteria(null);
        assertNotNull(event.getSelectionCriteria());
        assertTrue(event.getSelectionCriteria().isEmpty());
    }

    /**
     * US 01.05.05: Selection criteria survives a set → get round-trip
     * (simulates Firestore save/load).
     */
    @Test
    public void event_selectionCriteria_roundTrip() {
        Event event = new Event();
        List<String> criteria = Arrays.asList("No prior experience", "Age 10+");
        event.setSelectionCriteria(criteria);
        List<String> retrieved = event.getSelectionCriteria();
        assertEquals(criteria.size(), retrieved.size());
        for (int i = 0; i < criteria.size(); i++) {
            assertEquals(criteria.get(i), retrieved.get(i));
        }
    }

    // ─── US 01.05.06 / 01.05.07 — Private event invite ────────────────────────

    /**
     * US 01.05.06: Private invite notification type constant must be stable —
     * stored in Firestore and compared in EntrantNotificationsActivity.
     */
    @Test
    public void notificationHelper_privateInviteTypeConstantIsStable() {
        assertEquals("PRIVATE_INVITE", NotificationHelper.TYPE_PRIVATE_INVITE);
    }

    /**
     * US 01.05.06: Private invite notification title and message are not empty.
     */
    @Test
    public void notificationHelper_privateInviteTitleAndMessageNotEmpty() {
        assertNotNull(NotificationHelper.PRIVATE_INVITE_TITLE);
        assertFalse(NotificationHelper.PRIVATE_INVITE_TITLE.isEmpty());
        assertNotNull(NotificationHelper.PRIVATE_INVITE_MESSAGE);
        assertFalse(NotificationHelper.PRIVATE_INVITE_MESSAGE.isEmpty());
    }

    /**
     * US 01.05.06 / US 02.01.02: Event isPrivate flag defaults to false
     * (public by default) and can be set to true for private events.
     */
    @Test
    public void event_isPrivate_defaultsFalse() {
        Event event = new Event();
        assertFalse(event.isPrivate());
    }

    /**
     * US 02.01.02: Organizer can mark an event as private.
     */
    @Test
    public void event_isPrivate_canBeSetToTrue() {
        Event event = new Event();
        event.setPrivate(true);
        assertTrue(event.isPrivate());
    }

    /**
     * US 02.01.02: Private flag can be toggled back to false.
     */
    @Test
    public void event_isPrivate_canBeToggledBackToFalse() {
        Event event = new Event();
        event.setPrivate(true);
        assertTrue(event.isPrivate());
        event.setPrivate(false);
        assertFalse(event.isPrivate());
    }

    /**
     * US 01.05.06: WaitingListEntry INVITED status constant is stable —
     * stored in Firestore and checked in EventDetailsActivity.
     */
    @Test
    public void waitingListEntry_invitedStatusIsStable() {
        assertEquals("INVITED", WaitingListEntry.Status.INVITED.name());
    }

    /**
     * US 01.05.06: WaitingListEntry can be created with INVITED status
     * (simulates organizer inviting entrant to private event).
     */
    @Test
    public void waitingListEntry_canBeCreatedWithInvitedStatus() {
        WaitingListEntry entry = new WaitingListEntry("device123",
                WaitingListEntry.Status.INVITED);
        assertEquals("device123", entry.getDeviceId());
        assertEquals("INVITED", entry.getStatus());
        assertNotNull(entry.getJoinTimestamp());
    }

    /**
     * US 01.05.07: Accepting a private invite changes status from INVITED to PENDING
     * (entrant joins the waiting list).
     */
    @Test
    public void waitingListEntry_acceptInvite_changesStatusToPending() {
        WaitingListEntry entry = new WaitingListEntry("device123",
                WaitingListEntry.Status.INVITED);
        assertEquals("INVITED", entry.getStatus());

        // Simulates accept action in EventDetailsActivity
        entry.setStatus(WaitingListEntry.Status.PENDING.name());
        assertEquals("PENDING", entry.getStatus());
    }

    /**
     * US 01.05.07: Declining a private invite changes status from INVITED to DECLINED.
     */
    @Test
    public void waitingListEntry_declineInvite_changesStatusToDeclined() {
        WaitingListEntry entry = new WaitingListEntry("device123",
                WaitingListEntry.Status.INVITED);
        assertEquals("INVITED", entry.getStatus());

        // Simulates decline action in EventDetailsActivity
        entry.setStatus(WaitingListEntry.Status.DECLINED.name());
        assertEquals("DECLINED", entry.getStatus());
    }

    /**
     * US 01.05.07: After accepting a private invite (status = PENDING),
     * entrant is now on the waiting list and can participate in the lottery.
     */
    @Test
    public void waitingListEntry_afterAcceptingInvite_isPending() {
        WaitingListEntry entry = new WaitingListEntry("device123",
                WaitingListEntry.Status.INVITED);
        entry.setStatus(WaitingListEntry.Status.PENDING.name());
        assertEquals(WaitingListEntry.Status.PENDING.name(), entry.getStatus());
        // PENDING means they are now on the public waiting list
        assertTrue(WaitingListEntry.Status.PENDING.name().equals(entry.getStatus()));
    }
}