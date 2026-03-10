package com.example.eventlottery;

/**
 * Purpose: Represents a user participating in the event lottery system.
 * This class holds personal identification data and maps to Firebase document structures.
 * Outstanding Issues: Needs integration with Firebase Auth UID for unique identification.
 */
public class Entrant {
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber; // Optional

    /**
     * Default constructor required for Firebase integration.
     */
    public Entrant() {}

    public Entrant(String firstName, String lastName, String email, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getFullName() { return firstName + " " + lastName; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
}