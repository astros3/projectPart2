package com.example.eventlottery;

import java.io.Serializable;

/**
 * This class holds entrant-side filter criteria for the event list and map screens.
 * It is Serializable so it can be passed between activities.
 */
public class EventFilterCriteria implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Creates a new EventFilterCriteria with all fields at their default (no filtering). */
    public EventFilterCriteria() {}

    /** Keyword to match against event title or description; empty string means no keyword filter. */
    private String keyword = "";
    /** Empty or "Any type" means no event-type filter. */
    private String eventType = "";
    /** Null or &lt;= 0 means no distance filter. */
    private Double maxDistanceKm;
    /** When true, only events with registration currently open are shown (list only; map always enforces this). */
    private boolean registrationOpenOnly;

    /**
     * Returns the keyword used to match event title or description.
     * @return trimmed keyword, or empty string
     */
    public String getKeyword() {
        return keyword != null ? keyword : "";
    }

    /**
     * Sets the search keyword (trimmed).
     * @param keyword text to match; null becomes empty
     */
    public void setKeyword(String keyword) {
        this.keyword = keyword != null ? keyword.trim() : "";
    }

    /**
     * Returns the selected event type filter.
     * @return event type label, or empty string for no type filter
     */
    public String getEventType() {
        return eventType != null ? eventType : "";
    }

    /**
     * Sets the event type filter (trimmed).
     * @param eventType type label or empty / "Any type" for no filter
     */
    public void setEventType(String eventType) {
        this.eventType = eventType != null ? eventType.trim() : "";
    }

    /**
     * Returns the maximum distance filter in kilometers, if set.
     * @return max distance in km, or null when distance filter is off
     */
    public Double getMaxDistanceKm() {
        return maxDistanceKm;
    }

    /**
     * Sets the maximum distance from the user position (km). Use null to disable.
     * @param maxDistanceKm positive distance in km, or null
     */
    public void setMaxDistanceKm(Double maxDistanceKm) {
        this.maxDistanceKm = maxDistanceKm;
    }

    /**
     * Returns whether only events with open registration should appear (list screen).
     * @return true if registration-open filter is on
     */
    public boolean isRegistrationOpenOnly() {
        return registrationOpenOnly;
    }

    /**
     * Sets whether to restrict the list to events with registration currently open.
     * @param registrationOpenOnly true to enable the filter
     */
    public void setRegistrationOpenOnly(boolean registrationOpenOnly) {
        this.registrationOpenOnly = registrationOpenOnly;
    }

    /**
     * Clears all filter constraints so defaults apply on each screen.
     */
    public void clear() {
        keyword = "";
        eventType = "";
        maxDistanceKm = null;
        registrationOpenOnly = false;
    }

    /**
     * Returns a new criteria instance with all fields at default (no filtering beyond screen rules).
     * @return new empty EventFilterCriteria
     */
    public static EventFilterCriteria empty() {
        return new EventFilterCriteria();
    }
}
