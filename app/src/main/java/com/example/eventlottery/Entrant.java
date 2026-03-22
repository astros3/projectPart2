package com.example.eventlottery;

/**
 * Model for an entrant (user) profile. Stored in Firestore at {@code users/{deviceId}}.
 * Separate from organizer accounts; one device may have both an Entrant and an Organizer profile.
 * Includes notification preference flag for US 01.04.03.
 */
public class Entrant {
    private String deviceID;
    private String name;
    private String email;
    private String phone;
    private String role;
    private Double latitude;
    private Double longitude;
    private String locationAddress;
    private boolean notificationsEnabled = true; // US 01.04.03

    public Entrant() {}

    /** Constructor for core profile fields. notificationsEnabled defaults to true. */
    public Entrant(String deviceID, String name, String email, String phone, String role) {
        this.deviceID = deviceID;
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.notificationsEnabled = true;
    }

    /** Display name; "Unknown Entrant" if name is null or blank. */
    public String getFullName() {
        String fullName = name != null ? name.trim() : "";
        return fullName.isEmpty() ? "Unknown Entrant" : fullName;
    }

    public String getDeviceID() { return deviceID; }
    public void setDeviceID(String deviceID) { this.deviceID = deviceID; }

    /** Full name (single field; may be "First Last"). */
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    /** Role string (e.g. "entrant") used by welcome flow. */
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getLocationAddress() { return locationAddress; }
    public void setLocationAddress(String locationAddress) { this.locationAddress = locationAddress; }

    /**
     * Whether the entrant wants to receive notifications from organizers and admins.
     * Defaults to true. Set to false when entrant opts out (US 01.04.03).
     */
    public boolean isNotificationsEnabled() { return notificationsEnabled; }
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
}