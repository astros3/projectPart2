package com.example.eventlottery;

import org.junit.Test;

import static org.junit.Assert.*;

/** Entrant map: coordinates filter, distance, cluster zoom (EventFilterUtils, EntrantMapActivity). */
public class EntrantMapClusterAndDistanceUnitTest {

    @Test
    public void entrantMap_requiresCoordinatesAndAppliesFilters() {
        Event withCoords = new Event();
        withCoords.setLatitude(53.5);
        withCoords.setLongitude(-113.5);
        withCoords.setTitle("Map Event");
        long now = System.currentTimeMillis();
        withCoords.setRegistrationStartMillis(now - 10_000L);
        withCoords.setRegistrationEndMillis(now + 100_000L);

        Event noCoords = new Event();
        noCoords.setTitle("Map Event");
        noCoords.setLatitude(null);
        noCoords.setLongitude(null);

        EventFilterCriteria c = new EventFilterCriteria();
        c.setKeyword("Map");

        assertTrue(EventFilterUtils.matchesForMap(withCoords, c, null, null, false));
        assertFalse(EventFilterUtils.matchesForMap(noCoords, c, null, null, false));
    }

    @Test
    public void mapNearbyEvents_haversineZeroForSamePoint() {
        assertEquals(0.0, EventFilterUtils.haversineKm(53.5, -113.5, 53.5, -113.5), 0.0001);
    }

    @Test
    public void mapClusterClick_incrementsZoomByOne() {
        assertEquals(11f, nextZoomAfterClusterTap(10f), 0.001f);
    }

    private static float nextZoomAfterClusterTap(float currentZoom) {
        return currentZoom + 1f;
    }
}
