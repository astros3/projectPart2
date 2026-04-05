package com.example.eventlottery;

import org.junit.Test;

import static org.junit.Assert.*;

/** Admin multi-role device id; co-organizer notification type (NotificationHelper). */
public class AdminMultiRoleAndCoOrganizerUnitTest {

    @Test
    public void coOrganizerInvite_notificationTypeConstantIsStable() {
        assertEquals("CO_ORGANIZER_ASSIGNED", NotificationHelper.TYPE_CO_ORGANIZER_ASSIGNED);
    }

    @Test
    public void administrator_canBeEntrantAndOrganizerWithSameDeviceId() {
        String deviceId = "admin_multi_role_device";
        AdminProfile admin = new AdminProfile();
        admin.setAdminId(deviceId);
        Entrant entrant = new Entrant(deviceId, "Admin User", "a@a.com", "", "entrant");
        Organizer organizer = new Organizer(deviceId, "Admin", "User", "a@a.com", "");
        assertEquals(deviceId, admin.getAdminId());
        assertEquals(deviceId, entrant.getDeviceID());
        assertEquals(deviceId, organizer.getOrganizerId());
    }
}
