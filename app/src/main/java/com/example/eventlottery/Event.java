package com.example.eventlottery;

import java.util.ArrayList;
import java.util.List;

/**
 * Model for an event in the Event Lottery system. Maps to Firestore
 * document at {@code events/{eventId}}; waiting list is subcollection
 * {@code events/{eventId}/waitingList/{deviceId}}.
 * <p>
 * Note: {@link #getUserApplicationStatus()} / {@link #setUserApplicationStatus(String)}
 * are for local UI only (e.g. EntrantHistoryScreenActivity); do not persist to Firestore.
 */
public class Event {
    private String eventId;
    private String title;
    private String description;
    private String location;
    private String organizerId;
    private String organizerName;
    private int capacity;
    private int waitingListLimit;
    private long registrationStartMillis;
    private long registrationEndMillis;
    private long eventDateMillis;
    private boolean geolocationRequired;
    private boolean isPrivate; // US 02.01.02
    private String posterUri;
    private String qrCodeUri;
    private String promoCode;
    private double price;
    private List<String> selectionCriteria;
    /** Event category (e.g. Swimming, Music); used for map/filter tags. */
    private String eventType;
    /** Venue coordinates from Places when organizer saves; used for entrant map. */
    private Double latitude;
    private Double longitude;
    private String userapplicationstatus = "User not signed up for this event";

    /** No-arg constructor; selectionCriteria initialized to empty list. */
    public Event() {
        this.selectionCriteria = new ArrayList<>();
    }

    /**
     * Full constructor for event data (excluding promoCode, posterUri, selectionCriteria).
     * selectionCriteria is set to an empty list.
     */
    public Event(String eventId, String title, String description, String location,
                 String organizerId, String organizerName, int capacity, int waitingListLimit,
                 long registrationStartMillis, long registrationEndMillis, long eventDateMillis,
                 boolean geolocationRequired, double price) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.location = location;
        this.organizerId = organizerId;
        this.organizerName = organizerName;
        this.capacity = capacity;
        this.waitingListLimit = waitingListLimit;
        this.registrationStartMillis = registrationStartMillis;
        this.registrationEndMillis = registrationEndMillis;
        this.eventDateMillis = eventDateMillis;
        this.geolocationRequired = geolocationRequired;
        this.price = price;
        this.selectionCriteria = new ArrayList<>();
    }

    /**
     * Whether the current time falls within the registration window.
     * @return true if now is between registrationStartMillis and registrationEndMillis (inclusive)
     */
    public boolean isRegistrationOpen() {
        long now = System.currentTimeMillis();
        return now >= registrationStartMillis && now <= registrationEndMillis;
    }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public String getOrganizerId() { return organizerId; }
    public void setOrganizerId(String organizerId) { this.organizerId = organizerId; }
    public String getOrganizerName() { return organizerName; }
    public void setOrganizerName(String organizerName) { this.organizerName = organizerName; }
    /** Max accepted entrants; 0 = unlimited. */
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    /** Max entrants on waiting list; 0 = unlimited. */
    public int getWaitingListLimit() { return waitingListLimit; }
    public void setWaitingListLimit(int waitingListLimit) { this.waitingListLimit = waitingListLimit; }
    public long getRegistrationStartMillis() { return registrationStartMillis; }
    public void setRegistrationStartMillis(long registrationStartMillis) { this.registrationStartMillis = registrationStartMillis; }
    public long getRegistrationEndMillis() { return registrationEndMillis; }
    public void setRegistrationEndMillis(long registrationEndMillis) { this.registrationEndMillis = registrationEndMillis; }
    public long getEventDateMillis() { return eventDateMillis; }
    public void setEventDateMillis(long eventDateMillis) { this.eventDateMillis = eventDateMillis; }
    /** Whether entrants must provide geolocation when joining. */
    public boolean isGeolocationRequired() { return geolocationRequired; }
    public void setGeolocationRequired(boolean geolocationRequired) { this.geolocationRequired = geolocationRequired; }
    /**
     * Whether this is a private event (US 02.01.02).
     * Private events are not visible on public listing and do not generate a QR code.
     * Organizer invites entrants manually (US 02.01.03).
     */
    public boolean isPrivate() { return isPrivate; }
    public void setPrivate(boolean isPrivate) { this.isPrivate = isPrivate; }
    public String getPosterUri() { return posterUri; }
    public void setPosterUri(String posterUri) { this.posterUri = posterUri; }
    public String getQrCodeUri() { return qrCodeUri; }
    public void setQrCodeUri(String qrCodeUri) { this.qrCodeUri = qrCodeUri; }
    /** Human-readable code for manual entry (e.g. "555 555"). */
    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public List<String> getSelectionCriteria() { return selectionCriteria != null ? selectionCriteria : new ArrayList<>(); }
    public void setSelectionCriteria(List<String> selectionCriteria) { this.selectionCriteria = selectionCriteria != null ? selectionCriteria : new ArrayList<>(); }
    public String getEventType() { return eventType != null ? eventType : ""; }
    public void setEventType(String eventType) { this.eventType = eventType; }
    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }
    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }
    public boolean hasCoordinates() {
        return latitude != null && longitude != null
                && !(Double.isNaN(latitude) || Double.isNaN(longitude));
    }
    /** UI-only: current user's application status for this event. Do not persist to Firestore. */
    public String getUserApplicationStatus() { return userapplicationstatus; }
    public void setUserApplicationStatus(String applicationStatus) { this.userapplicationstatus = applicationStatus; }
}