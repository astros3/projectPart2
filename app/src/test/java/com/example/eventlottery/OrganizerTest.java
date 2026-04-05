package com.example.eventlottery;

import org.junit.Test;

import static com.example.eventlottery.TestData.*;
import static org.junit.Assert.*;

/**
 * Unit tests for Organizer model.
 */
public class OrganizerTest {

    /** Welcome flow checks role to decide if device is already an organizer; default must be "organizer". */
    @Test
    public void noArgConstructor_setsRoleToOrganizer() {
        Organizer o = new Organizer();
        assertEquals("organizer", o.getRole());
    }

    /** Full profile path: all fields set and getFullName is first + last; role is "organizer". */
    @Test
    public void fullConstructor_setsFieldsAndNullToEmpty() {
        Organizer o = new Organizer(ORGANIZER_ID, "Jane", "Smith", "jane@example.com", "+1-780-555-0000");
        assertEquals(ORGANIZER_ID, o.getOrganizerId());
        assertEquals("Jane", o.getFirstName());
        assertEquals("Smith", o.getLastName());
        assertEquals("jane@example.com", o.getEmail());
        assertEquals("+1-780-555-0000", o.getPhoneNumber());
        assertEquals("Jane Smith", o.getFullName());
        assertEquals("organizer", o.getRole());
    }

    /** Null args are converted to empty strings so we never store null in Firestore. */
    @Test
    public void fullConstructor_nullStringsBecomeEmpty() {
        Organizer o = new Organizer("id", null, null, null, null);
        assertEquals("", o.getFirstName());
        assertEquals("", o.getLastName());
        assertEquals("", o.getEmail());
        assertEquals("", o.getPhoneNumber());
        assertEquals("Organizer", o.getFullName());
    }

    /** Two-arg constructor used when registering as organizer with a single label (e.g. "Event Coordinator"). */
    @Test
    public void displayNameConstructor_setsDisplayNameOnly() {
        Organizer o = new Organizer(ORGANIZER_ID, "Event Coordinator");
        assertEquals(ORGANIZER_ID, o.getOrganizerId());
        assertEquals("", o.getFirstName());
        assertEquals("", o.getLastName());
        assertEquals("Event Coordinator", o.getDisplayName());
        assertEquals("Event Coordinator", o.getFullName());
    }

    /** Display name for events: primary is firstName + " " + lastName. */
    @Test
    public void getFullName_prefersFirstAndLastName() {
        Organizer o = new Organizer("id", "Alice", "Brown", "a@b.com", "");
        assertEquals("Alice Brown", o.getFullName());
    }

    /** When first/last are empty, show displayName (e.g. from displayName-only constructor). */
    @Test
    public void getFullName_fallsBackToDisplayNameWhenFirstLastEmpty() {
        Organizer o = new Organizer("id", "Event Coordinator");
        assertEquals("Event Coordinator", o.getFullName());
    }

    /** Last fallback so UI never shows a blank name. */
    @Test
    public void getFullName_returnsOrganizerWhenAllEmpty() {
        Organizer o = new Organizer();
        o.setOrganizerId("id");
        assertEquals("Organizer", o.getFullName());
    }

    /** All fields must round-trip for profile edit and Firestore. */
    @Test
    public void setters_roundTrip() {
        Organizer o = new Organizer();
        o.setOrganizerId("oid");
        o.setFirstName("F");
        o.setLastName("L");
        o.setEmail("e@e.com");
        o.setPhoneNumber("123");
        o.setDisplayName("DN");
        o.setRole("organizer");

        assertEquals("oid", o.getOrganizerId());
        assertEquals("F", o.getFirstName());
        assertEquals("L", o.getLastName());
        assertEquals("e@e.com", o.getEmail());
        assertEquals("123", o.getPhoneNumber());
        assertEquals("DN", o.getDisplayName());
        assertEquals("organizer", o.getRole());
    }
}
