package com.example.eventlottery;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Represents an event in the Event Lottery system.
 *
 * Responsibilities (from CRC card):
 * - Store event details
 * - Enforce registration start/end dates
 * - Maintain waiting list
 * - Maintain selected entrants
 * - Maintain accepted entrants
 * - Enforce capacity limit
 * - Store event poster
 *
 * Collaborators: Organizer, Entrant, WaitingListEntry, LotteryManager, QRCodeService
 */
public class Event {

    // ── Fields ──────────────────────────────────────────────────────────────

    private String eventId;
    private String title;
    private String description;
    private String location;

    /** The organizer who created this event. */
    private Organizer organizer;

    /** Maximum number of entrants that can be accepted (0 = unlimited). */
    private int capacity;

    /** Maximum size of the waiting list (0 = unlimited). */
    private int waitingListLimit;

    private Date registrationStart;
    private Date registrationEnd;
    private Date eventDate;

    /** Whether entrants must provide geolocation when joining. */
    private boolean geolocationRequired;

    /** URI / URL string for the event poster image. */
    private String posterUri;

    /** URI for the event's QR code image. */
    private String qrCodeUri;

    /** Price to attend the event in dollars (0.0 = free). */
    private double price;

    // ── Lists managed by this event ─────────────────────────────────────────

    /** All entrants who have joined the waiting list. */
    private final List<WaitingListEntry> waitingList;

    /** Entrants selected by the lottery but not yet responded. */
    private final List<WaitingListEntry> selectedEntrants;

    /** Entrants who have accepted their lottery invitation. */
    private final List<WaitingListEntry> acceptedEntrants;

    /** Entrants who declined or were replaced. */
    private final List<WaitingListEntry> cancelledEntrants;

    // ── Constructor ─────────────────────────────────────────────────────────

    public Event(String eventId, String title, String description, String location,
                 Organizer organizer, int capacity, int waitingListLimit,
                 Date registrationStart, Date registrationEnd, Date eventDate,
                 boolean geolocationRequired, double price) {
        this.eventId           = eventId;
        this.title             = title;
        this.description       = description;
        this.location          = location;
        this.organizer         = organizer;
        this.capacity          = capacity;
        this.waitingListLimit  = waitingListLimit;
        this.registrationStart = registrationStart;
        this.registrationEnd   = registrationEnd;
        this.eventDate         = eventDate;
        this.geolocationRequired = geolocationRequired;
        this.price               = price;

        this.waitingList        = new ArrayList<>();
        this.selectedEntrants   = new ArrayList<>();
        this.acceptedEntrants   = new ArrayList<>();
        this.cancelledEntrants  = new ArrayList<>();
    }

    // ── Registration date enforcement ────────────────────────────────────────

    /**
     * Returns true if the current time falls within the registration window.
     */
    public boolean isRegistrationOpen() {
        Date now = new Date();
        return now.after(registrationStart) && now.before(registrationEnd);
    }

    // ── Waiting list management ──────────────────────────────────────────────

    /**
     * Adds an entrant to the waiting list.
     *
     * @param entry The WaitingListEntry representing this entrant's interest.
     * @return true if added successfully, false if registration is closed,
     *         the entrant is already on the list, or the list is full.
     */
    public boolean addToWaitingList(WaitingListEntry entry) {
        if (!isRegistrationOpen()) {
            return false;
        }
        if (isEntrantOnWaitingList(entry.getEntrant())) {
            return false;
        }
        if (waitingListLimit > 0 && waitingList.size() >= waitingListLimit) {
            return false;
        }
        entry.setStatus(WaitingListEntry.Status.WAITING);
        waitingList.add(entry);
        return true;
    }

    /**
     * Removes an entrant from the waiting list (they chose to leave).
     *
     * @param entrant The entrant to remove.
     * @return true if they were found and removed.
     */
    public boolean removeFromWaitingList(Entrant entrant) {
        WaitingListEntry toRemove = findWaitingEntry(entrant, waitingList);
        if (toRemove != null) {
            waitingList.remove(toRemove);
            return true;
        }
        return false;
    }

    /**
     * Checks whether an entrant is already on the waiting list.
     */
    public boolean isEntrantOnWaitingList(Entrant entrant) {
        return findWaitingEntry(entrant, waitingList) != null;
    }

    /**
     * Returns the current size of the waiting list.
     */
    public int getWaitingListSize() {
        return waitingList.size();
    }

    // ── Selected entrants management ─────────────────────────────────────────

    /**
     * Marks an entry as selected (called by LotteryManager).
     * Moves the entry from waitingList to selectedEntrants.
     */
    public void selectEntrant(WaitingListEntry entry) {
        if (waitingList.contains(entry)) {
            waitingList.remove(entry);
            entry.setStatus(WaitingListEntry.Status.SELECTED);
            selectedEntrants.add(entry);
        }
    }

    /**
     * Records that a selected entrant has accepted the invitation.
     * Enforces capacity — acceptance is only recorded if there is room.
     *
     * @return true if accepted and capacity allows, false otherwise.
     */
    public boolean acceptInvitation(Entrant entrant) {
        WaitingListEntry entry = findWaitingEntry(entrant, selectedEntrants);
        if (entry == null) {
            return false;
        }
        if (capacity > 0 && acceptedEntrants.size() >= capacity) {
            return false;
        }
        selectedEntrants.remove(entry);
        entry.setStatus(WaitingListEntry.Status.ACCEPTED);
        acceptedEntrants.add(entry);
        return true;
    }

    /**
     * Records that a selected entrant has declined the invitation.
     * Moves them to the cancelled list so LotteryManager can replace them.
     */
    public void declineInvitation(Entrant entrant) {
        WaitingListEntry entry = findWaitingEntry(entrant, selectedEntrants);
        if (entry != null) {
            selectedEntrants.remove(entry);
            entry.setStatus(WaitingListEntry.Status.DECLINED);
            cancelledEntrants.add(entry);
        }
    }

    /**
     * Cancels a selected entrant (called by Organizer).
     */
    public void cancelEntrant(Entrant entrant) {
        WaitingListEntry entry = findWaitingEntry(entrant, selectedEntrants);
        if (entry == null) {
            entry = findWaitingEntry(entrant, acceptedEntrants);
            if (entry != null) acceptedEntrants.remove(entry);
        } else {
            selectedEntrants.remove(entry);
        }
        if (entry != null) {
            entry.setStatus(WaitingListEntry.Status.CANCELLED);
            cancelledEntrants.add(entry);
        }
    }

    // ── Capacity enforcement ─────────────────────────────────────────────────

    /**
     * Returns true if the event has remaining capacity for accepted entrants.
     */
    public boolean hasCapacity() {
        return capacity <= 0 || acceptedEntrants.size() < capacity;
    }

    /**
     * Returns the number of spots still available.
     * Returns Integer.MAX_VALUE if there is no capacity limit.
     */
    public int getRemainingCapacity() {
        if (capacity <= 0) return Integer.MAX_VALUE;
        return Math.max(0, capacity - acceptedEntrants.size());
    }

    // ── Poster ───────────────────────────────────────────────────────────────

    public void setPosterUri(String posterUri) {
        this.posterUri = posterUri;
    }

    public String getPosterUri() {
        return posterUri;
    }

    // ── QR Code ──────────────────────────────────────────────────────────────

    public void setQrCodeUri(String qrCodeUri) {
        this.qrCodeUri = qrCodeUri;
    }

    public String getQrCodeUri() {
        return qrCodeUri;
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private WaitingListEntry findWaitingEntry(Entrant entrant, List<WaitingListEntry> list) {
        for (WaitingListEntry entry : list) {
            if (entry.getEntrant().equals(entrant)) {
                return entry;
            }
        }
        return null;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    public String getEventId()               { return eventId; }
    public String getTitle()                 { return title; }
    public void   setTitle(String title)     { this.title = title; }

    public String getDescription()                   { return description; }
    public void   setDescription(String description) { this.description = description; }

    public String getLocation()                  { return location; }
    public void   setLocation(String location)   { this.location = location; }

    public Organizer getOrganizer()              { return organizer; }

    public int  getCapacity()                    { return capacity; }
    public void setCapacity(int capacity)        { this.capacity = capacity; }

    public int  getWaitingListLimit()                    { return waitingListLimit; }
    public void setWaitingListLimit(int waitingListLimit){ this.waitingListLimit = waitingListLimit; }

    public Date getRegistrationStart()                       { return registrationStart; }
    public void setRegistrationStart(Date registrationStart) { this.registrationStart = registrationStart; }

    public Date getRegistrationEnd()                     { return registrationEnd; }
    public void setRegistrationEnd(Date registrationEnd) { this.registrationEnd = registrationEnd; }

    public Date getEventDate()                   { return eventDate; }
    public void setEventDate(Date eventDate)     { this.eventDate = eventDate; }

    public boolean isGeolocationRequired()                       { return geolocationRequired; }
    public void    setGeolocationRequired(boolean geolocationRequired) {
        this.geolocationRequired = geolocationRequired;
    }

    public double getPrice()             { return price; }
    public void   setPrice(double price) { this.price = price; }

    public List<WaitingListEntry> getWaitingList()       { return new ArrayList<>(waitingList); }
    public List<WaitingListEntry> getSelectedEntrants()  { return new ArrayList<>(selectedEntrants); }
    public List<WaitingListEntry> getAcceptedEntrants()  { return new ArrayList<>(acceptedEntrants); }
    public List<WaitingListEntry> getCancelledEntrants() { return new ArrayList<>(cancelledEntrants); }
}