package com.example.eventlottery;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents an event in the Event Lottery system.
 * Stored in Firestore at events/{eventId}.
 * Waiting list is in subcollection events/{eventId}/waitingList/{deviceId}.
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
    private String posterUri;
    private String qrCodeUri;
    /** Human-readable code for manual entry (e.g. "555 555"). */
    private String promoCode;
    private double price;
    private List<String> selectionCriteria;

    public Event() {
        selectionCriteria = new ArrayList<>();
    }

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

    /** True if current time is within registration window. */
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
    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }
    public int getWaitingListLimit() { return waitingListLimit; }
    public void setWaitingListLimit(int waitingListLimit) { this.waitingListLimit = waitingListLimit; }
    public long getRegistrationStartMillis() { return registrationStartMillis; }
    public void setRegistrationStartMillis(long registrationStartMillis) { this.registrationStartMillis = registrationStartMillis; }
    public long getRegistrationEndMillis() { return registrationEndMillis; }
    public void setRegistrationEndMillis(long registrationEndMillis) { this.registrationEndMillis = registrationEndMillis; }
    public long getEventDateMillis() { return eventDateMillis; }
    public void setEventDateMillis(long eventDateMillis) { this.eventDateMillis = eventDateMillis; }
    public boolean isGeolocationRequired() { return geolocationRequired; }
    public void setGeolocationRequired(boolean geolocationRequired) { this.geolocationRequired = geolocationRequired; }
    public String getPosterUri() { return posterUri; }
    public void setPosterUri(String posterUri) { this.posterUri = posterUri; }
    public String getQrCodeUri() { return qrCodeUri; }
    public void setQrCodeUri(String qrCodeUri) { this.qrCodeUri = qrCodeUri; }
    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }
    public double getPrice() { return price; }
    public void setPrice(double price) { this.price = price; }
    public List<String> getSelectionCriteria() { return selectionCriteria != null ? selectionCriteria : new ArrayList<>(); }
    public void setSelectionCriteria(List<String> selectionCriteria) { this.selectionCriteria = selectionCriteria != null ? selectionCriteria : new ArrayList<>(); }
}
