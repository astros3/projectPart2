package com.example.eventlottery;

import java.io.Serializable;

/**
 * Entrant-side filter applied to the event list and passed to the map screen.
 */
public class EventFilterCriteria implements Serializable {
    private static final long serialVersionUID = 1L;

    private String keyword = "";
    /** Empty or "Any type" means no event-type filter. */
    private String eventType = "";
    /** Null or &lt;= 0 means no distance filter. */
    private Double maxDistanceKm;
    /** When true, only events with registration currently open are shown (list only; map always enforces this). */
    private boolean registrationOpenOnly;

    public String getKeyword() {
        return keyword != null ? keyword : "";
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword != null ? keyword.trim() : "";
    }

    public String getEventType() {
        return eventType != null ? eventType : "";
    }

    public void setEventType(String eventType) {
        this.eventType = eventType != null ? eventType.trim() : "";
    }

    public Double getMaxDistanceKm() {
        return maxDistanceKm;
    }

    public void setMaxDistanceKm(Double maxDistanceKm) {
        this.maxDistanceKm = maxDistanceKm;
    }

    public boolean isRegistrationOpenOnly() {
        return registrationOpenOnly;
    }

    public void setRegistrationOpenOnly(boolean registrationOpenOnly) {
        this.registrationOpenOnly = registrationOpenOnly;
    }

    /** Clears all constraints (everything visible subject to screen rules). */
    public void clear() {
        keyword = "";
        eventType = "";
        maxDistanceKm = null;
        registrationOpenOnly = false;
    }

    public static EventFilterCriteria empty() {
        return new EventFilterCriteria();
    }
}
