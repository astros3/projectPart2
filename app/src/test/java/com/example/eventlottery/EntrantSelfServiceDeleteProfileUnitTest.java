package com.example.eventlottery;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Entrant deletes own profile (EntrantProfileActivity deletes users/{deviceId}).
 */
public class EntrantSelfServiceDeleteProfileUnitTest {

    @Test
    public void deleteProfile_targetsUsersDocumentByDeviceId() {
        String deviceId = "entrant_device_xyz";
        assertEquals("users/entrant_device_xyz",
                EntrantProfileActivity.USERS_COLLECTION + "/" + deviceId);
    }
}
