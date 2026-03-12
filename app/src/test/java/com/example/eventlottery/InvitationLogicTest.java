package com.example.eventlottery;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

public class InvitationLogicTest {
    private EntrantListManager manager;

    @Before
    public void setUp() {
        manager = EntrantListManager.getInstance();
        manager.getWaitingList().clear();
        manager.getSelectedList().clear();
    }

    @Test
    public void testAcceptInvitationMovesUser() {
        // Setup: User is in selected list
        manager.getWaitingList().add("User_123");
        manager.drawEntrants(1);

        assertTrue(manager.getSelectedList().contains("User_123"));

        // Action: User accepts
        manager.acceptInvitation("User_123");

        // Verify: No longer selected, moved to accepted
        assertFalse(manager.getSelectedList().contains("User_123"));
        assertTrue(manager.getAcceptedList().contains("User_123"));
    }

    @Test
    public void testDeclineInvitationVacatesSpot() {
        // Setup
        manager.getWaitingList().add("User_456");
        manager.drawEntrants(1);

        // Action: User declines
        manager.declineInvitation("User_456");

        // Verify: Spot is now empty for the next draw
        assertEquals(0, manager.getSelectedList().size());
        //assertTrue(manager.rejectedList().contains("User_456"));
    }
}