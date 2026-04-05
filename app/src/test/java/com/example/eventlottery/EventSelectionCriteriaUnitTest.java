package com.example.eventlottery;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

/** US 01.05.05 — event lottery selection criteria list on Event. */
public class EventSelectionCriteriaUnitTest {

    @Test
    public void event_selectionCriteria_canBeSetAndRetrieved() {
        Event event = new Event();
        List<String> criteria = Arrays.asList("Must be 18+", "Beginner level only");
        event.setSelectionCriteria(criteria);
        assertEquals(2, event.getSelectionCriteria().size());
        assertEquals("Must be 18+", event.getSelectionCriteria().get(0));
        assertEquals("Beginner level only", event.getSelectionCriteria().get(1));
    }

    @Test
    public void event_selectionCriteria_defaultsToEmptyList() {
        Event event = new Event();
        assertNotNull(event.getSelectionCriteria());
        assertTrue(event.getSelectionCriteria().isEmpty());
    }

    @Test
    public void event_setSelectionCriteria_nullFallsBackToEmptyList() {
        Event event = new Event();
        event.setSelectionCriteria(null);
        assertNotNull(event.getSelectionCriteria());
        assertTrue(event.getSelectionCriteria().isEmpty());
    }

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
}
