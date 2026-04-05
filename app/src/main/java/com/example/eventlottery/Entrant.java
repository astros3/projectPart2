package com.example.eventlottery;

/**
 * This class models an entrant (user) profile stored in Firestore at users/{deviceId}.
 * It is separate from organizer accounts; one device may have both profiles.
 * Includes notification preference (US 01.04.03).
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

    /**
     * No-arg constructor required for Firestore deserialization.
     */
    public Entrant() {
    }

    /**
     * Constructs an entrant with core profile fields; notifications default to enabled.
     * @param deviceID device id used as Firestore document id
     * @param fullName display name
     * @param email email address
     * @param phone phone number
     * @param role role string (e.g. entrant)
     */
    public Entrant(String deviceID, String fullName, String email, String phone, String role) {
        this.deviceID = deviceID;
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.role = role;
        this.notificationsEnabled = true;
    }

    /**
     * Constructs an entrant with profile and optional geolocation fields.
     * @param deviceID device id used as Firestore document id
     * @param fullName display name
     * @param email email address
     * @param phone phone number
     * @param role role string
     * @param latitude last known latitude, may be null
     * @param longitude last known longitude, may be null
     * @param locationAddress human-readable address, may be null
     */
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

    /**
     * Returns the device id for this profile.
     * @return device id, or null if unset
     */
    public String getDeviceID() {
        return deviceID;
    }

    /**
     * Sets the device id for this profile.
     * @param deviceID device id string
     */
    public void setDeviceID(String deviceID) {
        this.deviceID = deviceID;
    }

    /**
     * Returns a display name for the entrant, or a default if missing or blank.
     * @return trimmed full name, or "Unknown Entrant"
     */
    public String getFullName() {
        if (fullName == null || fullName.trim().isEmpty()) {
            return "Unknown Entrant";
        }
        return fullName.trim();
    }

    /**
     * Sets the entrant display name.
     * @param fullName full name to store
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Returns the email, never null.
     * @return email or empty string
     */
    public String getEmail() {
        return email != null ? email : "";
    }

    /**
     * Sets the email address.
     * @param email email to store
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the phone number, never null.
     * @return phone or empty string
     */
    public String getPhone() {
        return phone != null ? phone : "";
    }

    /**
     * Sets the phone number.
     * @param phone phone to store
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Returns the role string (e.g. entrant), never null.
     * @return role or empty string
     */
    public String getRole() {
        return role != null ? role : "";
    }

    /**
     * Sets the role string.
     * @param role role to store
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Returns last known latitude if set.
     * @return latitude or null
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * Sets last known latitude.
     * @param latitude latitude or null
     */
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    /**
     * Returns last known longitude if set.
     * @return longitude or null
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * Sets last known longitude.
     * @param longitude longitude or null
     */
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * Returns a human-readable location address, never null.
     * @return address or empty string
     */
    public String getLocationAddress() {
        return locationAddress != null ? locationAddress : "";
    }

    /**
     * Sets the human-readable location address.
     * @param locationAddress address to store
     */
    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    /**
     * Returns whether push/in-app notifications are enabled for this entrant.
     * @return true if notifications are enabled
     */
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    /**
     * Sets whether notifications are enabled.
     * @param notificationsEnabled true to enable
     */
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }
}
