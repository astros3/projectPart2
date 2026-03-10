package com.example.eventlottery;

/**
 * Represents an organizer who creates and runs events.
 * Used for display (e.g. "By Organizer1") and future organizer-specific logic.
 */
public class Organizer {
    private String organizerId;
    private String displayName;

    public Organizer() {}

    public Organizer(String organizerId, String displayName) {
        this.organizerId = organizerId;
        this.displayName = displayName;
    }

    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}
