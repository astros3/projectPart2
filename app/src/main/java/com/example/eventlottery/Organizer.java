package com.example.eventlottery;

/**
 * This class models an organizer account stored in Firestore at organizers/{organizerId}.
 * It is separate from entrant profiles (users collection); one device may have both.
 */
public class Organizer {

    private boolean notificationsEnabled;

    /**
     * Returns whether organizer notifications are enabled.
     * @return true if enabled
     */
    public boolean isNotificationsEnabled() {
        return notificationsEnabled;
    }

    /**
     * Sets whether organizer notifications are enabled.
     * @param notificationsEnabled true to enable
     */
    public void setNotificationsEnabled(boolean notificationsEnabled) {
        this.notificationsEnabled = notificationsEnabled;
    }

    private String organizerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String role;
    /** Optional display name; used as fallback if first/last name are empty. */
    private String displayName;

    /**
     * No-arg constructor; sets role to organizer.
     */
    public Organizer() {
        this.role = "organizer";
    }

    /**
     * Constructs an organizer with full name and contact fields.
     * @param organizerId Firestore document id for this organizer
     * @param firstName first name; null stored as empty
     * @param lastName last name; null stored as empty
     * @param email email; null stored as empty
     * @param phoneNumber phone; null stored as empty
     */
    public Organizer(String organizerId, String firstName, String lastName, String email, String phoneNumber) {
        this.organizerId = organizerId;
        this.firstName = firstName != null ? firstName : "";
        this.lastName = lastName != null ? lastName : "";
        this.email = email != null ? email : "";
        this.phoneNumber = phoneNumber != null ? phoneNumber : "";
        this.displayName = null;
        this.role = "organizer";
    }

    /**
     * Constructs an organizer with only a display label; other name fields empty.
     * @param organizerId Firestore document id
     * @param displayName fallback display name (e.g. "Organizer")
     */
    public Organizer(String organizerId, String displayName) {
        this.organizerId = organizerId;
        this.firstName = "";
        this.lastName = "";
        this.email = "";
        this.phoneNumber = "";
        this.displayName = displayName;
        this.role = "organizer";
    }

    /**
     * Returns a display name: first plus last name if non-blank, else displayName, else "Organizer".
     * @return resolved display string
     */
    public String getFullName() {
        String name = (firstName != null ? firstName : "").trim() + " " + (lastName != null ? lastName : "").trim();
        name = name.trim();
        if (!name.isEmpty()) return name;
        if (displayName != null && !displayName.trim().isEmpty()) return displayName.trim();
        return "Organizer";
    }

    /**
     * Returns the organizer document id.
     * @return organizer id, or null if unset
     */
    public String getOrganizerId() {
        return organizerId;
    }

    /**
     * Sets the organizer document id.
     * @param organizerId id string
     */
    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    /**
     * Returns the first name.
     * @return first name, or null
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the first name.
     * @param firstName first name
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the last name.
     * @return last name, or null
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the last name.
     * @param lastName last name
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the email.
     * @return email, or null
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email.
     * @param email email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the phone number.
     * @return phone, or null
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the phone number.
     * @param phoneNumber phone number
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    /**
     * Returns optional display name used when first/last name are empty.
     * @return display name, or null
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the optional display name.
     * @param displayName display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the role string (typically organizer).
     * @return role, or null
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the role string.
     * @param role role to store
     */
    public void setRole(String role) {
        this.role = role;
    }
}
