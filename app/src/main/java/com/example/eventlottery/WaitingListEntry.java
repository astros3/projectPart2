package com.example.eventlottery;

import com.google.firebase.Timestamp;

/**
 * Model for one entrant's place on an event waiting list. Stored in Firestore at
 * {@code events/{eventId}/waitingList/{deviceId}}. Document ID is the entrant's device ID.
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

    /** No-arg constructor for Firestore deserialization. */
    public WaitingListEntry() {}

    /** Creates entry with given status and joinTimestamp = now. */
    public WaitingListEntry(String deviceId, Status status) {
        this.deviceId = deviceId;
        this.status = status.name();
        this.joinTimestamp = Timestamp.now();
    }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    /** Status string (e.g. {@link Status#PENDING}.name()). */
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getJoinTimestamp() { return joinTimestamp; }
    public void setJoinTimestamp(Timestamp joinTimestamp) { this.joinTimestamp = joinTimestamp; }

    public Long getInvitationSentMillis() { return invitationSentMillis; }
    public void setInvitationSentMillis(Long invitationSentMillis) {
        this.invitationSentMillis = invitationSentMillis;
    }
}