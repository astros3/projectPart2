package com.example.eventlottery;

/**
 * Applies {@link EventFilterCriteria} to events, including optional distance from the user.
 */
public final class EventFilterUtils {

    private EventFilterUtils() {}

    /** Haversine distance in kilometres between two WGS84 points. */
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
     * List/home: optional registration window. Distance is applied only when {@code hasUserPosition}
     * is true and {@code userLat}/{@code userLng} are non-null; otherwise the max-distance constraint
     * is skipped (caller should toast if the user asked for distance but no fix was available).
     */
    public static boolean matchesForList(Event e, EventFilterCriteria c,
                                        Double userLat, Double userLng, boolean hasUserPosition) {
        if (c == null || e == null) return true;
        if (c.isRegistrationOpenOnly() && !e.isRegistrationOpen()) return false;
        return matchesKeywordTypeDistance(e, c, userLat, userLng, hasUserPosition);
    }

    /**
     * Map: only events with coordinates; registration window is constrained only when
     * {@link EventFilterCriteria#isRegistrationOpenOnly()} is true (same rule as the list).
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
