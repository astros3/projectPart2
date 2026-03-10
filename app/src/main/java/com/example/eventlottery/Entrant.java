package com.example.eventlottery;

/**
 * Represents an entrant (CRC: Entrant): a person entering/joining events.
 * Stored in Firestore at users/{deviceId}. Used for profile and for joining waiting lists.
 * Entrants and organizers are separate account types; only organizers create/edit events.
 */
public class Entrant {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    private Double latitude;
    private Double longitude;
    private String locationAddress;

    public Entrant() {}

    public Entrant(String firstName, String lastName, String email, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getLocationAddress() { return locationAddress; }
    public void setLocationAddress(String locationAddress) { this.locationAddress = locationAddress; }
}