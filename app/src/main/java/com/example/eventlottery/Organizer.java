package com.example.eventlottery;

/**
 * Model for an organizer account. Stored in Firestore at {@code organizers/{organizerId}}.
 * Separate from entrant accounts ({@link Entrant} in {@code users}); one device may have both.
 */
public class Organizer {
    private String organizerId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;
    private String role;
    /** Optional display name; used as fallback if first/last name are empty. */
    private String displayName;

    public Organizer() {
        this.role = "organizer";
    }

    /** Full profile constructor. */
    public Organizer(String organizerId, String firstName, String lastName, String email, String phoneNumber) {
        this.organizerId = organizerId;
        this.firstName = firstName != null ? firstName : "";
        this.lastName = lastName != null ? lastName : "";
        this.email = email != null ? email : "";
        this.phoneNumber = phoneNumber != null ? phoneNumber : "";
        this.displayName = null;
        this.role = "organizer";
    }

    /** Constructor with display name only (e.g. "Organizer"); other fields empty. */
    public Organizer(String organizerId, String displayName) {
        this.organizerId = organizerId;
        this.firstName = "";
        this.lastName = "";
        this.email = "";
        this.phoneNumber = "";
        this.displayName = displayName;
        this.role = "organizer";
    }

    /** Display name: firstName + lastName, else displayName, else "Organizer". */
    public String getFullName() {
        String name = (firstName != null ? firstName : "").trim() + " " + (lastName != null ? lastName : "").trim();
        name = name.trim();
        if (!name.isEmpty()) return name;
        if (displayName != null && !displayName.trim().isEmpty()) return displayName.trim();
        return "Organizer";
    }

    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
    /** Optional; used as fallback when first/last name are empty. */
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}