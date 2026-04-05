package com.example.eventlottery;

import java.util.ArrayList;
import java.util.List;

/**
 * This class models an event in the Event Lottery app. It maps to a Firestore document at
 * events/{eventId}; the waiting list is the subcollection events/{eventId}/waitingList/{deviceId}.
 * <p>
 * Note: getUserApplicationStatus / setUserApplicationStatus are for local UI only
 * (e.g. EntrantHistoryScreenActivity); do not persist to Firestore.
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

    /** Device IDs of users assigned as co-organizers for this event. */
    private List<String> coOrganizerIds;
    /** DeviceId of whoever currently holds the edit lock; null when unlocked. */
    private String editLockHeldBy;
    /** Epoch-ms when the edit lock was acquired; used for 10-min timeout. */
    private long editLockAcquiredAt;

    /**
     * No-arg constructor; initializes selectionCriteria and coOrganizerIds to empty lists.
     */
    public Event() {
        this.selectionCriteria = new ArrayList<>();
        this.coOrganizerIds = new ArrayList<>();
    }

    /**
     * Full constructor for core event fields (excludes promoCode, posterUri, selectionCriteria lists).
     * selectionCriteria is initialized to an empty list.
     * @param eventId Firestore document id
     * @param title event title
     * @param description event description
     * @param location venue text
     * @param organizerId organizer device or user id
     * @param organizerName organizer display name
     * @param capacity max accepted entrants; 0 = unlimited
     * @param waitingListLimit max waiting list size; 0 = unlimited
     * @param registrationStartMillis registration window start (epoch ms)
     * @param registrationEndMillis registration window end (epoch ms)
     * @param eventDateMillis event date/time (epoch ms)
     * @param geolocationRequired whether join requires geolocation
     * @param price event price
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
     * Returns whether the current time is within the registration window (inclusive).
     * @return true if now is between registrationStartMillis and registrationEndMillis
     */
    public boolean isRegistrationOpen() {
        long now = System.currentTimeMillis();
        return now >= registrationStartMillis && now <= registrationEndMillis;
    }

    /**
     * Returns the event document id.
     * @return event id, or null
     */
    public String getEventId() {
        return eventId;
    }

    /**
     * Sets the event document id.
     * @param eventId id string
     */
    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    /**
     * Returns the event title.
     * @return title, or null
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the event title.
     * @param title title string
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Returns the event description.
     * @return description, or null
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the event description.
     * @param description description text
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the venue / location text.
     * @return location, or null
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the venue / location text.
     * @param location location string
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Returns the organizer id who owns this event.
     * @return organizer id, or null
     */
    public String getOrganizerId() {
        return organizerId;
    }

    /**
     * Sets the organizer id.
     * @param organizerId organizer id
     */
    public void setOrganizerId(String organizerId) {
        this.organizerId = organizerId;
    }

    /**
     * Returns the organizer display name.
     * @return organizer name, or null
     */
    public String getOrganizerName() {
        return organizerName;
    }

    /**
     * Sets the organizer display name.
     * @param organizerName name string
     */
    public void setOrganizerName(String organizerName) {
        this.organizerName = organizerName;
    }

    /**
     * Returns max accepted entrants; 0 means unlimited.
     * @return capacity
     */
    public int getCapacity() {
        return capacity;
    }

    /**
     * Sets max accepted entrants; use 0 for unlimited.
     * @param capacity capacity value
     */
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    /**
     * Returns max waiting-list size; 0 means unlimited.
     * @return waiting list limit
     */
    public int getWaitingListLimit() {
        return waitingListLimit;
    }

    /**
     * Sets max waiting-list size; use 0 for unlimited.
     * @param waitingListLimit limit value
     */
    public void setWaitingListLimit(int waitingListLimit) {
        this.waitingListLimit = waitingListLimit;
    }

    /**
     * Returns registration window start time (epoch ms).
     * @return start millis
     */
    public long getRegistrationStartMillis() {
        return registrationStartMillis;
    }

    /**
     * Sets registration window start time.
     * @param registrationStartMillis epoch ms
     */
    public void setRegistrationStartMillis(long registrationStartMillis) {
        this.registrationStartMillis = registrationStartMillis;
    }

    /**
     * Returns registration window end time (epoch ms).
     * @return end millis
     */
    public long getRegistrationEndMillis() {
        return registrationEndMillis;
    }

    /**
     * Sets registration window end time.
     * @param registrationEndMillis epoch ms
     */
    public void setRegistrationEndMillis(long registrationEndMillis) {
        this.registrationEndMillis = registrationEndMillis;
    }

    /**
     * Returns scheduled event date/time (epoch ms).
     * @return event millis
     */
    public long getEventDateMillis() {
        return eventDateMillis;
    }

    /**
     * Sets scheduled event date/time.
     * @param eventDateMillis epoch ms
     */
    public void setEventDateMillis(long eventDateMillis) {
        this.eventDateMillis = eventDateMillis;
    }

    /**
     * Returns whether entrants must provide geolocation when joining.
     * @return true if geolocation required
     */
    public boolean isGeolocationRequired() {
        return geolocationRequired;
    }

    /**
     * Sets whether geolocation is required to join.
     * @param geolocationRequired true to require
     */
    public void setGeolocationRequired(boolean geolocationRequired) {
        this.geolocationRequired = geolocationRequired;
    }

    /**
     * Returns whether this is a private event (US 02.01.02). Private events use invites instead of public QR.
     * @return true if private
     */
    public boolean isPrivate() {
        return isPrivate;
    }

    /**
     * Sets private event flag.
     * @param isPrivate true for private event
     */
    public void setPrivate(boolean isPrivate) {
        this.isPrivate = isPrivate;
    }

    /**
     * Returns stored poster image URI string, if any.
     * @return poster uri, or null
     */
    public String getPosterUri() {
        return posterUri;
    }

    /**
     * Sets poster image URI.
     * @param posterUri uri string
     */
    public void setPosterUri(String posterUri) {
        this.posterUri = posterUri;
    }

    /**
     * Returns stored QR image URI string, if any.
     * @return qr uri, or null
     */
    public String getQrCodeUri() {
        return qrCodeUri;
    }

    /**
     * Sets QR code image URI.
     * @param qrCodeUri uri string
     */
    public void setQrCodeUri(String qrCodeUri) {
        this.qrCodeUri = qrCodeUri;
    }

    /**
     * Returns human-readable promo code for manual entry (e.g. "555 555").
     * @return promo code, or null
     */
    public String getPromoCode() {
        return promoCode;
    }

    /**
     * Sets human-readable promo code.
     * @param promoCode code string
     */
    public void setPromoCode(String promoCode) {
        this.promoCode = promoCode;
    }

    /**
     * Returns event price.
     * @return price
     */
    public double getPrice() {
        return price;
    }

    /**
     * Sets event price.
     * @param price price value
     */
    public void setPrice(double price) {
        this.price = price;
    }

    /**
     * Returns selection criteria list, never null.
     * @return criteria list (copy-safe empty list if unset)
     */
    public List<String> getSelectionCriteria() {
        return selectionCriteria != null ? selectionCriteria : new ArrayList<>();
    }

    /**
     * Sets selection criteria list; null becomes empty list.
     * @param selectionCriteria list of criteria strings
     */
    public void setSelectionCriteria(List<String> selectionCriteria) {
        this.selectionCriteria = selectionCriteria != null ? selectionCriteria : new ArrayList<>();
    }

    /**
     * Returns event type/category for filters and map tags, never null.
     * @return type string or empty
     */
    public String getEventType() {
        return eventType != null ? eventType : "";
    }

    /**
     * Sets event type/category.
     * @param eventType type label
     */
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    /**
     * Returns venue latitude if set.
     * @return latitude or null
     */
    public Double getLatitude() {
        return latitude;
    }

    /**
     * Sets venue latitude.
     * @param latitude lat or null
     */
    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    /**
     * Returns venue longitude if set.
     * @return longitude or null
     */
    public Double getLongitude() {
        return longitude;
    }

    /**
     * Sets venue longitude.
     * @param longitude lng or null
     */
    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    /**
     * Returns true if both latitude and longitude are set and finite.
     * @return true if valid coordinates
     */
    public boolean hasCoordinates() {
        return latitude != null && longitude != null
                && !(Double.isNaN(latitude) || Double.isNaN(longitude));
    }

    /**
     * Returns UI-only application status for the current user; not persisted to Firestore.
     * @return status label string
     */
    public String getUserApplicationStatus() {
        return userapplicationstatus;
    }

    /**
     * Sets UI-only application status for list/detail screens; do not write to Firestore.
     * @param applicationStatus label to show
     */
    public void setUserApplicationStatus(String applicationStatus) {
        this.userapplicationstatus = applicationStatus;
    }

    /**
     * Returns co-organizer device ids, never null.
     * @return list of device ids
     */
    public List<String> getCoOrganizerIds() {
        return coOrganizerIds != null ? coOrganizerIds : new ArrayList<>();
    }

    /**
     * Sets co-organizer device ids; null becomes empty list.
     * @param coOrganizerIds device id list
     */
    public void setCoOrganizerIds(List<String> coOrganizerIds) {
        this.coOrganizerIds = coOrganizerIds != null ? coOrganizerIds : new ArrayList<>();
    }

    /**
     * Returns whether the given device id is listed as a co-organizer.
     * @param deviceId device id to check
     * @return true if co-organizer
     */
    public boolean isCoOrganizer(String deviceId) {
        return coOrganizerIds != null && coOrganizerIds.contains(deviceId);
    }

    /**
     * Returns device id holding the edit lock, or null if unlocked.
     * @return lock holder device id or null
     */
    public String getEditLockHeldBy() {
        return editLockHeldBy;
    }

    /**
     * Sets who holds the edit lock.
     * @param editLockHeldBy device id or null
     */
    public void setEditLockHeldBy(String editLockHeldBy) {
        this.editLockHeldBy = editLockHeldBy;
    }

    /**
     * Returns when the edit lock was acquired (epoch ms).
     * @return lock time
     */
    public long getEditLockAcquiredAt() {
        return editLockAcquiredAt;
    }

    /**
     * Sets when the edit lock was acquired.
     * @param editLockAcquiredAt epoch ms
     */
    public void setEditLockAcquiredAt(long editLockAcquiredAt) {
        this.editLockAcquiredAt = editLockAcquiredAt;
    }
}
