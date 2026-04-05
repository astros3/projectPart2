package com.example.eventlottery;

import com.google.firebase.Timestamp;

/**
 * This class models one entrant's waiting-list row in Firestore at
 * events/{eventId}/waitingList/{deviceId}. The document id is the entrant's device id.
 */
public class WaitingListEntry {

    /**
     * Lifecycle status of the entrant's application.
     * INVITED = entrant has been personally invited to join a private event's waiting list
     * (US 01.05.06 / 01.05.07). Accepting changes status to PENDING.
     */
    public enum Status {
        WAITING, PENDING, SELECTED, ACCEPTED, DECLINED, CANCELLED, INVITED
    }

    private String deviceId;
    private String status;
    private Timestamp joinTimestamp;
    /** When the invitation to accept/decline was sent (SELECTED / INVITED); used for 24h expiry. */
    private Long invitationSentMillis;

    /**
     * No-arg constructor for Firestore deserialization.
     */
    public WaitingListEntry() {
    }

    /**
     * Creates an entry with the given status and joinTimestamp set to now.
     * @param deviceId entrant device id (document id)
     * @param status application status enum
     */
    public WaitingListEntry(String deviceId, Status status) {
        this.deviceId = deviceId;
        this.status = status.name();
        this.joinTimestamp = Timestamp.now();
    }

    /**
     * Returns the entrant device id for this waiting-list document.
     * @return device id, or null if unset
     */
    public String getDeviceId() {
        return deviceId;
    }

    /**
     * Sets the entrant device id.
     * @param deviceId device id string
     */
    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    /**
     * Returns the status string (e.g. Status.PENDING.name()).
     * @return status string, or null
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status string persisted in Firestore.
     * @param status status name string
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns when the entrant joined the waiting list.
     * @return Firestore timestamp, or null
     */
    public Timestamp getJoinTimestamp() {
        return joinTimestamp;
    }

    /**
     * Sets the join timestamp.
     * @param joinTimestamp when the entrant joined
     */
    public void setJoinTimestamp(Timestamp joinTimestamp) {
        this.joinTimestamp = joinTimestamp;
    }

    /**
     * Returns when the invitation was sent (for expiry), if set.
     * @return epoch milliseconds, or null
     */
    public Long getInvitationSentMillis() {
        return invitationSentMillis;
    }

    /**
     * Sets when the invitation was sent.
     * @param invitationSentMillis epoch ms, or null
     */
    public void setInvitationSentMillis(Long invitationSentMillis) {
        this.invitationSentMillis = invitationSentMillis;
    }
}
