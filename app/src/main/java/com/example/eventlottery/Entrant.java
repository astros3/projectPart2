package com.example.eventlottery;

/**
 * Model for an entrant (user) profile. Stored in Firestore at {@code users/{deviceId}}.
 * Separate from organizer accounts; one device may have both an Entrant and an Organizer profile.
 * Includes notification preference flag for US 01.04.03.
 */
public class Entrant {

    private String deviceID;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private Double latitude;
    private Double longitude;
    private String locationAddress;
    private boolean notificationsEnabled = true; // US 01.04.03

    /** Required empty constructor for Firestore */
    public Entrant() {
    }

    /** Constructor for core profile fields */
    public Entrant(String deviceID, String fullName, String email, String phone, String role) {
        this.deviceID = deviceID;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.notificationsEnabled = true;
    }

    /** Full constructor */
    public Entrant(String deviceID, String fullName, String email, String phone, String role,
                   Double latitude, Double longitude, String locationAddress) {
        this.deviceID = deviceID;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locationAddress = locationAddress;
        this.notificationsEnabled = true;
    }

    public String getDeviceID() {
        return deviceID;
    }

    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    /** Returns display name safely */
    public String getFullName() {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Unknown Entrant";
        }
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getEmail() {
        return email != null ? email : "";
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone != null ? phone : "";
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    /** Role string, for example "entrant" */
    public String getRole() {
        return role != null ? role : "";
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public String getLocationAddress() {
        return locationAddress != null ? locationAddress : "";
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
}