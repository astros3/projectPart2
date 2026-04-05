package com.example.eventlottery;

import java.util.Set;

/**
 * Applies EventFilterCriteria to events, including optional distance from the user.
 */
public final class EventFilterUtils {

    private EventFilterUtils() {}

    /**
     * Determines whether a private event should be visible to the entrant on the home list.
     * Private events appear only if the user already has a waiting-list entry for that event.
     *
     * @param e                        the event to evaluate
     * @param userWaitingListEventIds  set of event IDs the user has joined
     * @return true if the event should be shown to this entrant
     */
    public static boolean passesEntrantPrivateListVisibility(Event e, Set<String> userWaitingListEventIds) {
        if (e == null) {
            return false;
        }
        if (!e.isPrivate()) {
            return true;
        }
        return userWaitingListEventIds != null && userWaitingListEventIds.contains(e.getEventId());
    }

    /**
     * Returns whether an event should appear as a map pin on the entrant map.
     * Private events are never shown as map pins.
     *
     * @param e the event to evaluate
     * @return true if the event should be shown on the map
     */
    public static boolean showEventOnEntrantMap(Event e) {
        return e != null && !e.isPrivate();
    }

    /**
     * Computes the Haversine distance in kilometres between two WGS84 coordinate pairs.
     *
     * @param lat1 latitude of the first point in decimal degrees
     * @param lon1 longitude of the first point in decimal degrees
     * @param lat2 latitude of the second point in decimal degrees
     * @param lon2 longitude of the second point in decimal degrees
     * @return distance in kilometres between the two points
     */
    public static double haversineKm(double lat1, double lon1, double lat2, double lon2) {
        final double r = 6371.0;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return r * c;
    }

    /**
     * Returns whether an event satisfies the given criteria for the home list.
     * Distance is applied only when hasUserPosition is true and
     * coordinates are available; otherwise the max-distance constraint is skipped.
     *
     * @param e               the event to evaluate
     * @param c               filter criteria to apply
     * @param userLat         user's current latitude, or null if unknown
     * @param userLng         user's current longitude, or null if unknown
     * @param hasUserPosition true if a GPS fix is available
     * @return true if the event passes all active filter constraints
     */
    public static boolean matchesForList(Event e, EventFilterCriteria c,
                                        Double userLat, Double userLng, boolean hasUserPosition) {
        if (c == null || e == null) return true;
        if (c.isRegistrationOpenOnly() && !e.isRegistrationOpen()) return false;
        return matchesKeywordTypeDistance(e, c, userLat, userLng, hasUserPosition);
    }

    /**
     * Returns whether an event satisfies the given criteria for the map view.
     * Events without coordinates are always excluded. The registration-open filter is
     * applied when isRegistrationOpenOnly() is true.
     *
     * @param e               the event to evaluate
     * @param c               filter criteria to apply, or null for no filtering beyond coordinates
     * @param userLat         user's current latitude, or null if unknown
     * @param userLng         user's current longitude, or null if unknown
     * @param hasUserPosition true if a GPS fix is available
     * @return true if the event has coordinates and passes all active filter constraints
     */
    public static boolean matchesForMap(Event e, EventFilterCriteria c,
                                       Double userLat, Double userLng, boolean hasUserPosition) {
        if (e == null) return false;
        if (!e.hasCoordinates()) return false;
        if (c != null && c.isRegistrationOpenOnly() && !e.isRegistrationOpen()) {
            return false;
        }
        if (c == null) return true;
        return matchesKeywordTypeDistance(e, c, userLat, userLng, hasUserPosition);
    }

    private static boolean matchesKeywordTypeDistance(Event e, EventFilterCriteria c,
                                                       Double userLat, Double userLng,
                                                       boolean hasUserPosition) {
        String kw = c.getKeyword();
        if (!kw.isEmpty()) {
            String t = e.getTitle() != null ? e.getTitle().toLowerCase() : "";
            String d = e.getDescription() != null ? e.getDescription().toLowerCase() : "";
            String k = kw.toLowerCase();
            if (!t.contains(k) && !d.contains(k)) return false;
        }

        String tf = c.getEventType();
        if (!tf.isEmpty() && !tf.equalsIgnoreCase(e.getEventType())) return false;

        Double maxKm = c.getMaxDistanceKm();
        if (maxKm != null && maxKm > 0) {
            if (!hasUserPosition || userLat == null || userLng == null) {
                return true;
            }
            if (!e.hasCoordinates()) return false;
            double dist = haversineKm(userLat, userLng, e.getLatitude(), e.getLongitude());
            if (dist > maxKm) return false;
        }
        return true;
    }
}
