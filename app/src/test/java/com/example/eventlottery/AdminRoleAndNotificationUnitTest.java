package com.example.eventlottery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class AdminRoleAndNotificationUnitTest {

    @Test
    public void adminDeviceId_canExistAcrossEntrantAndOrganizerProfiles() {
        String adminDeviceId = "admin_device_123";

        AdminProfile adminProfile = new AdminProfile();
        adminProfile.setAdminId(adminDeviceId);

        Entrant entrant = new Entrant(adminDeviceId, "Admin Entrant", "admin@test.com", "111", "entrant");
        Organizer organizer = new Organizer(adminDeviceId, "Admin", "Organizer", "admin@test.com", "111");

        assertEquals(adminProfile.getAdminId(), entrant.getDeviceID());
        assertEquals(adminProfile.getAdminId(), organizer.getOrganizerId());
    }

    @Test
    public void entrant_notificationsEnabledDefaultsTrue_andCanBeDisabled() {
        Entrant entrant = new Entrant();
        assertTrue(entrant.isNotificationsEnabled());

        entrant.setNotificationsEnabled(false);
        assertEquals(false, entrant.isNotificationsEnabled());
    }

    @Test
    public void coOrganizerNotificationType_constantIsStable() {
        assertEquals("CO_ORGANIZER_ASSIGNED", NotificationHelper.TYPE_CO_ORGANIZER_ASSIGNED);
    }

    @Test
    public void organizer_defaultRoleIsOrganizer() {
        Organizer organizer = new Organizer();
        assertEquals("organizer", organizer.getRole());
    }

    @Test
    public void privateInviteNotificationType_constantIsStable() {
        assertEquals("PRIVATE_INVITE", NotificationHelper.TYPE_PRIVATE_INVITE);
    }
}
