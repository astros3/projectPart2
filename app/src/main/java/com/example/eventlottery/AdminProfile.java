package com.example.eventlottery;

/**
 * This class holds admin profile fields stored in Firestore at admins/{adminId}.
 * The document may exist for access control only; profile fields stay optional until edited.
 */
public class AdminProfile {

    private String adminId;
    private String firstName;
    private String lastName;
    private String email;
    private String phoneNumber;

    /**
     * No-arg constructor for Firestore deserialization.
     */
    public AdminProfile() {
    }

    /**
     * Returns the admin document id (typically device id).
     * @return admin id, or null if unset
     */
    public String getAdminId() {
        return adminId;
    }

    /**
     * Sets the admin document id.
     * @param adminId Firestore document id for this admin
     */
    public void setAdminId(String adminId) {
        this.adminId = adminId;
    }

    /**
     * Returns the admin first name.
     * @return first name, or null if unset
     */
    public String getFirstName() {
        return firstName;
    }

    /**
     * Sets the admin first name.
     * @param firstName first name to store
     */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Returns the admin last name.
     * @return last name, or null if unset
     */
    public String getLastName() {
        return lastName;
    }

    /**
     * Sets the admin last name.
     * @param lastName last name to store
     */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Returns the admin email.
     * @return email, or null if unset
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the admin email.
     * @param email email address to store
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the admin phone number.
     * @return phone number, or null if unset
     */
    public String getPhoneNumber() {
        return phoneNumber;
    }

    /**
     * Sets the admin phone number.
     * @param phoneNumber phone number to store
     */
    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
