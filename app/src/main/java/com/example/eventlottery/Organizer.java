package com.example.eventlottery;

/**
 * Represents an organizer account (CRC: Organizer).
 * Stored in Firestore at organizers/{organizerId}.
 * Organizers are a separate account type from users (entrants), with their own
 * profile fields (firstName, lastName, email, phoneNumber). One device can have
 * both an entrant profile (users) and an organizer profile (organizers).
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

    public Organizer(String organizerId, String firstName, String lastName, String email, String phoneNumber) {
        this.organizerId = organizerId;
        this.firstName = firstName != null ? firstName : "";
        this.lastName = lastName != null ? lastName : "";
        this.email = email != null ? email : "";
        this.phoneNumber = phoneNumber != null ? phoneNumber : "";
        this.displayName = null;
        this.role = "organizer";
    }

    /** For backward compatibility when registering with just a label. */
    public Organizer(String organizerId, String displayName) {
        this.organizerId = organizerId;
        this.firstName = "";
        this.lastName = "";
        this.email = "";
        this.phoneNumber = "";
        this.displayName = displayName;
        this.role = "organizer";
    }

    /** Full name for display (e.g. on events). Prefers firstName + lastName; falls back to displayName or "Organizer". */
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

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}