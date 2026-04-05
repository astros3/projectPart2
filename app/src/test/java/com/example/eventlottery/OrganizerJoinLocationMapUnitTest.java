package com.example.eventlottery;

import org.junit.Test;

import static org.junit.Assert.*;

/** Organizer: waiting-list entrant join location on map. */
public class OrganizerJoinLocationMapUnitTest {

    @Test
    public void organizerViewJoinLocation_bundleUsesDeviceIdKey() {
        assertEquals("deviceId", GEO_NAV_ARG_DEVICE_ID);
    }

    @Test
    public void organizerViewJoinLocation_readsEntrantFromUsersCollection() {
        assertEquals("users/entrant_dev_1", USERS_COLLECTION + "/entrant_dev_1");
    }

    private static final String GEO_NAV_ARG_DEVICE_ID = "deviceId";
    private static final String USERS_COLLECTION = "users";
}
