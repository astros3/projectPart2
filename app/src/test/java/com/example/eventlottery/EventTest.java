package com.example.eventlottery;

import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;

import static com.example.eventlottery.TestData.*;
import static org.junit.Assert.*;

/** Unit tests for Event model. Registration window: see EventRegistrationWindowUnitTest. */
public class EventTest {

    private Event event;

    /** Build one event with all main fields set and registration window "open" for tests that use it. */
    @Before
    public void setUp() {
        event = new Event(
                EVENT_ID_1,
                EVENT_TITLE,
                EVENT_DESCRIPTION,
                EVENT_LOCATION,
                ORGANIZER_ID,
                ORGANIZER_NAME,
                CAPACITY,
                WAITING_LIST_LIMIT,
                regStartOpen(),
                regEndOpen(),
                NOW_MS + 24 * HOUR_MS,
                true,
                PRICE
        );
    }

    /** No-arg constructor must give a non-null empty selection list so Firestore/UI never see null. */
    @Test
    public void defaultConstructor_initializesSelectionCriteriaEmpty() {
        Event e = new Event();
        assertNotNull(e.getSelectionCriteria());
        assertTrue(e.getSelectionCriteria().isEmpty());
    }

    /** Full constructor must set every field and leave selectionCriteria as empty list. */
    @Test
    public void fullConstructor_setsAllFields() {
        assertEquals(EVENT_ID_1, event.getEventId());
        assertEquals(EVENT_TITLE, event.getTitle());
        assertEquals(EVENT_DESCRIPTION, event.getDescription());
        assertEquals(EVENT_LOCATION, event.getLocation());
        assertEquals(ORGANIZER_ID, event.getOrganizerId());
        assertEquals(ORGANIZER_NAME, event.getOrganizerName());
        assertEquals(CAPACITY, event.getCapacity());
        assertEquals(WAITING_LIST_LIMIT, event.getWaitingListLimit());
        assertEquals(regStartOpen(), event.getRegistrationStartMillis());
        assertEquals(regEndOpen(), event.getRegistrationEndMillis());
        assertTrue(event.isGeolocationRequired());
        assertEquals(PRICE, event.getPrice(), 0.0);
        assertNotNull(event.getSelectionCriteria());
        assertTrue(event.getSelectionCriteria().isEmpty());
    }

    /** Getter must never return null; when internal list is null, return empty list. */
    @Test
    public void getSelectionCriteria_whenNull_returnsEmptyList() {
        Event e = new Event();
        e.setSelectionCriteria(null);
        assertNotNull(e.getSelectionCriteria());
        assertTrue(e.getSelectionCriteria().isEmpty());
    }

    /** Selection criteria list is stored and returned in order (e.g. for event edit UI). */
    @Test
    public void setSelectionCriteria_preservesList() {
        event.setSelectionCriteria(Arrays.asList("One per person", "Must be 18+"));
        assertEquals(2, event.getSelectionCriteria().size());
        assertEquals("One per person", event.getSelectionCriteria().get(0));
    }

    /** UI-only field: default text shown when entrant has not joined this event (e.g. history screen). */
    @Test
    public void getUserApplicationStatus_defaultIsNotSignedUp() {
        Event e = new Event();
        assertEquals("User not signed up for this event", e.getUserApplicationStatus());
    }

    /** Status can be updated for display (e.g. PENDING, ACCEPTED) without persisting to Firestore. */
    @Test
    public void setUserApplicationStatus_updatesStatus() {
        event.setUserApplicationStatus("PENDING");
        assertEquals("PENDING", event.getUserApplicationStatus());
    }

    /** Every field must be writable and readable for Firestore serialization and event edit screen. */
    @Test
    public void setters_roundTripAllFields() {
        Event e = new Event();
        e.setEventId("id1");
        e.setTitle("T");
        e.setDescription("D");
        e.setLocation("L");
        e.setOrganizerId("o1");
        e.setOrganizerName("ON");
        e.setCapacity(10);
        e.setWaitingListLimit(5);
        e.setRegistrationStartMillis(1L);
        e.setRegistrationEndMillis(2L);
        e.setEventDateMillis(3L);
        e.setGeolocationRequired(true);
        e.setPosterUri("http://example.com/poster.png");
        e.setPromoCode("123 456");
        e.setPrice(9.99);

        assertEquals("id1", e.getEventId());
        assertEquals("T", e.getTitle());
        assertEquals("D", e.getDescription());
        assertEquals("L", e.getLocation());
        assertEquals("o1", e.getOrganizerId());
        assertEquals("ON", e.getOrganizerName());
        assertEquals(10, e.getCapacity());
        assertEquals(5, e.getWaitingListLimit());
        assertEquals(1L, e.getRegistrationStartMillis());
        assertEquals(2L, e.getRegistrationEndMillis());
        assertEquals(3L, e.getEventDateMillis());
        assertTrue(e.isGeolocationRequired());
        assertEquals("http://example.com/poster.png", e.getPosterUri());
        assertEquals("123 456", e.getPromoCode());
        assertEquals(9.99, e.getPrice(), 0.001);
    }

    @Test
    public void testEventImageDetails() {
        Event event = new Event();
        event.setEventId("Event001");
        event.setPosterUri("https://example.com/image.png");

        assertEquals("Event001", event.getEventId());
        assertEquals("https://example.com/image.png", event.getPosterUri());
    }
}
