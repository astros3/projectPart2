package com.example.eventlottery;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * US 01.05.06 / 01.05.07 — private invites; US 02.01.02 ;
 * */
public class PrivateEventInviteUnitTest {

    @Test
    public void notificationHelper_privateInviteTypeConstantIsStable() {
        assertEquals("PRIVATE_INVITE", NotificationHelper.TYPE_PRIVATE_INVITE);
    }

    @Test
    public void notificationHelper_privateInviteTitleAndMessageNotEmpty() {
        assertNotNull(NotificationHelper.PRIVATE_INVITE_TITLE);
        assertFalse(NotificationHelper.PRIVATE_INVITE_TITLE.isEmpty());
        assertNotNull(NotificationHelper.PRIVATE_INVITE_MESSAGE);
        assertFalse(NotificationHelper.PRIVATE_INVITE_MESSAGE.isEmpty());
    }

    @Test
    public void event_isPrivate_defaultsFalse() {
        Event event = new Event();
        assertFalse(event.isPrivate());
    }

    @Test
    public void event_isPrivate_canBeSetToTrue() {
        Event event = new Event();
        event.setPrivate(true);
        assertTrue(event.isPrivate());
    }

    @Test
    public void event_isPrivate_canBeToggledBackToFalse() {
        Event event = new Event();
        event.setPrivate(true);
        assertTrue(event.isPrivate());
        event.setPrivate(false);
        assertFalse(event.isPrivate());
    }

    @Test
    public void waitingListEntry_invitedStatusIsStable() {
        assertEquals("INVITED", WaitingListEntry.Status.INVITED.name());
    }

    @Test
    public void waitingListEntry_canBeCreatedWithInvitedStatus() {
        WaitingListEntry entry = new WaitingListEntry("device123",
                WaitingListEntry.Status.INVITED);
        assertEquals("device123", entry.getDeviceId());
        assertEquals("INVITED", entry.getStatus());
        assertNotNull(entry.getJoinTimestamp());
    }

    @Test
    public void waitingListEntry_acceptInvite_changesStatusToPending() {
        WaitingListEntry entry = new WaitingListEntry("device123",
                WaitingListEntry.Status.INVITED);
        assertEquals("INVITED", entry.getStatus());
        entry.setStatus(WaitingListEntry.Status.PENDING.name());
        assertEquals("PENDING", entry.getStatus());
    }

    @Test
    public void waitingListEntry_declineInvite_changesStatusToDeclined() {
        WaitingListEntry entry = new WaitingListEntry("device123",
                WaitingListEntry.Status.INVITED);
        assertEquals("INVITED", entry.getStatus());
        entry.setStatus(WaitingListEntry.Status.DECLINED.name());
        assertEquals("DECLINED", entry.getStatus());
    }

    @Test
    public void waitingListEntry_afterAcceptingInvite_isPending() {
        WaitingListEntry entry = new WaitingListEntry("device123",
                WaitingListEntry.Status.INVITED);
        entry.setStatus(WaitingListEntry.Status.PENDING.name());
        assertEquals(WaitingListEntry.Status.PENDING.name(), entry.getStatus());
        assertTrue(WaitingListEntry.Status.PENDING.name().equals(entry.getStatus()));
    }
}
