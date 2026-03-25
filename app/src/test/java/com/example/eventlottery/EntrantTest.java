package com.example.eventlottery;

import org.junit.Test;

import static com.example.eventlottery.TestData.*;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link Entrant} model.
 */
public class EntrantTest {

    /** Setup/registration path: deviceId, name, email, phone, role and getFullName all match. */
    @Test
    public void fullConstructor_setsAllFields() {
        Entrant e = new Entrant(ENTRANT_DEVICE_ID, ENTRANT_NAME, ENTRANT_EMAIL, ENTRANT_PHONE, "entrant");
        assertEquals(ENTRANT_DEVICE_ID, e.getDeviceID());
        assertEquals(ENTRANT_NAME, e.getFullName());
        assertEquals(ENTRANT_EMAIL, e.getEmail());
        assertEquals(ENTRANT_PHONE, e.getPhone());
        assertEquals("entrant", e.getRole());
        assertEquals(ENTRANT_NAME, e.getFullName());
    }

    /** Normal case: display name is the stored name. */
    @Test
    public void getFullName_whenNameSet_returnsName() {
        Entrant e = new Entrant("dev", "Alex Johnson", "a@b.com", "", "entrant");
        assertEquals("Alex Johnson", e.getFullName());
    }

    /** Avoid null/blank in UI; show a safe default. */
    @Test
    public void getFullName_whenNameNull_returnsUnknownEntrant() {
        Entrant e = new Entrant();
        e.setFullName(null);
        assertEquals("Unknown Entrant", e.getFullName());
    }

    /** Empty name is treated as "no name" for display. */
    @Test
    public void getFullName_whenNameEmpty_returnsUnknownEntrant() {
        Entrant e = new Entrant("dev", "", "a@b.com", "", "entrant");
        assertEquals("Unknown Entrant", e.getFullName());
    }

    /** Display should not show leading/trailing spaces from input. */
    @Test
    public void getFullName_trimsWhitespace() {
        Entrant e = new Entrant("dev", "  Bob  ", "b@b.com", "", "entrant");
        assertEquals("Bob", e.getFullName());
    }

    /** Firestore deserialization uses no-arg + setters; optional location fields must round-trip. */
    @Test
    public void noArgConstructor_allowsSetters() {
        Entrant e = new Entrant();
        e.setDeviceID(ENTRANT_DEVICE_ID);
        e.setFullName(ENTRANT_NAME);
        e.setEmail(ENTRANT_EMAIL);
        e.setPhone(ENTRANT_PHONE);
        e.setRole("entrant");
        e.setLatitude(53.5461);
        e.setLongitude(-113.4937);
        e.setLocationAddress("Edmonton, AB");

        assertEquals(ENTRANT_DEVICE_ID, e.getDeviceID());
        assertEquals(ENTRANT_NAME, e.getFullName());
        assertEquals(ENTRANT_EMAIL, e.getEmail());
        assertEquals(ENTRANT_PHONE, e.getPhone());
        assertEquals("entrant", e.getRole());
        assertEquals(53.5461, e.getLatitude(), 0.0001);
        assertEquals(-113.4937, e.getLongitude(), 0.0001);
        assertEquals("Edmonton, AB", e.getLocationAddress());
    }
}
