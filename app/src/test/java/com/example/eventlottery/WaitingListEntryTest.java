package com.example.eventlottery;

import com.google.firebase.Timestamp;

import org.junit.Test;

import static com.example.eventlottery.TestData.ENTRANT_DEVICE_ID;
import static org.junit.Assert.*;

/**
 * Unit tests for {@link WaitingListEntry} model.
 */
public class WaitingListEntryTest {

    /** Status strings are stored in Firestore and compared in app; enum names must stay stable. */
    @Test
    public void statusEnum_hasExpectedValues() {
        assertEquals("WAITING", WaitingListEntry.Status.WAITING.name());
        assertEquals("PENDING", WaitingListEntry.Status.PENDING.name());
        assertEquals("SELECTED", WaitingListEntry.Status.SELECTED.name());
        assertEquals("ACCEPTED", WaitingListEntry.Status.ACCEPTED.name());
        assertEquals("DECLINED", WaitingListEntry.Status.DECLINED.name());
        assertEquals("CANCELLED", WaitingListEntry.Status.CANCELLED.name());
    }

    /** Firestore loads with no-arg constructor then setters; all fields must be settable. */
    @Test
    public void noArgConstructor_allowsSetters() {
        WaitingListEntry e = new WaitingListEntry();
        e.setDeviceId(ENTRANT_DEVICE_ID);
        e.setStatus(WaitingListEntry.Status.PENDING.name());
        e.setJoinTimestamp(Timestamp.now());

        assertEquals(ENTRANT_DEVICE_ID, e.getDeviceId());
        assertEquals("PENDING", e.getStatus());
        assertNotNull(e.getJoinTimestamp());
    }

    @Test
    public void fullConstructor_setsDeviceIdAndStatus() {
        WaitingListEntry e = new WaitingListEntry(ENTRANT_DEVICE_ID, WaitingListEntry.Status.PENDING);
        assertEquals(ENTRANT_DEVICE_ID, e.getDeviceId());
        assertEquals("PENDING", e.getStatus());
        assertNotNull(e.getJoinTimestamp());
    }

    @Test
    public void fullConstructor_eachStatusStoredAsName() {
        for (WaitingListEntry.Status s : WaitingListEntry.Status.values()) {
            WaitingListEntry e = new WaitingListEntry("dev", s);
            assertEquals("dev", e.getDeviceId());
            assertEquals(s.name(), e.getStatus());
            assertNotNull(e.getJoinTimestamp());
        }
    }

    /** Accept/decline and other flows update status via setter; must persist. */
    @Test
    public void setStatus_updatesStatus() {
        WaitingListEntry e = new WaitingListEntry(ENTRANT_DEVICE_ID, WaitingListEntry.Status.PENDING);
        e.setStatus(WaitingListEntry.Status.ACCEPTED.name());
        assertEquals("ACCEPTED", e.getStatus());
    }
}
