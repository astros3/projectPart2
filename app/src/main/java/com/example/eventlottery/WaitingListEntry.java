package com.example.eventlottery;

import com.google.firebase.Timestamp;

/**
 * Represents an entrant's place on an event's waiting list.
 * Stored in Firestore at events/{eventId}/waitingList/{deviceId}.
 */
public class WaitingListEntry {
    public enum Status { WAITING, PENDING, SELECTED, ACCEPTED, DECLINED, CANCELLED }

    private String deviceId;
    private String status; // enum name as string for Firestore
    private Timestamp joinTimestamp;

    public WaitingListEntry() {}

    public WaitingListEntry(String deviceId, Status status) {
        this.deviceId = deviceId;
        this.status = status.name();
        this.joinTimestamp = Timestamp.now();
    }

    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Timestamp getJoinTimestamp() { return joinTimestamp; }
    public void setJoinTimestamp(Timestamp joinTimestamp) { this.joinTimestamp = joinTimestamp; }
}
