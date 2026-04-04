package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class OrganizerReplacementDrawFlowTest {

    private static final String ORG_PREFS_NAME = "EventLotteryPrefs";
    private static final String KEY_ORG_CURRENT_EVENT_ID = "organizer_current_event_id";

    private FirebaseFirestore db;
    private Context appContext;

    private final String EVENT_ID = "test_event_replacement_draw";
    private final String CANCELLED_ID = "test_cancelled_entrant";
    private final String PENDING_ID = "test_pending_entrant";

    @Before
    public void setUp() throws Exception {
        appContext = ApplicationProvider.getApplicationContext();
        db = FirebaseFirestore.getInstance();

        // Persist current event id synchronously so the fragment reads it before loadRedraw runs.
        setOrganizerCurrentEventIdSync(EVENT_ID);

        // Minimal event doc (so event exists; fragment mainly uses waitingList)
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", EVENT_ID);
        event.put("title", "Replacement Draw Test Event");
        event.put("organizerId", "test_org");
        event.put("organizerName", "Test Organizer");
        event.put("isPrivate", false);
        event.put("capacity", 1);
        event.put("waitingListLimit", 10);
        event.put("registrationStartMillis", System.currentTimeMillis() - 60_000L);
        event.put("registrationEndMillis", System.currentTimeMillis() + 86_400_000L);
        event.put("eventDateMillis", System.currentTimeMillis() + 7L * 86_400_000L);
        event.put("geolocationRequired", false);
        Tasks.await(db.collection("events").document(EVENT_ID).set(event));

        // Seed user docs so names can resolve (optional, but avoids "Unknown Entrant")
        Tasks.await(db.collection("users").document(CANCELLED_ID).set(userDoc(CANCELLED_ID, "Cancelled Entrant")));
        Tasks.await(db.collection("users").document(PENDING_ID).set(userDoc(PENDING_ID, "Pending Entrant")));

        // Seed waiting list:
        // - one cancelled/declined (the trigger condition)
        // - one pending (the pool to draw from). Only one pending => deterministic pick.
        Tasks.await(db.collection("events").document(EVENT_ID)
                .collection("waitingList").document(CANCELLED_ID)
                .set(waitingEntry(CANCELLED_ID, WaitingListEntry.Status.CANCELLED.name())));

        Tasks.await(db.collection("events").document(EVENT_ID)
                .collection("waitingList").document(PENDING_ID)
                .set(waitingEntry(PENDING_ID, WaitingListEntry.Status.PENDING.name())));
    }

    @After
    public void tearDown() throws Exception {
        if (db == null) return;

        Tasks.await(db.collection("events").document(EVENT_ID)
                .collection("waitingList").document(CANCELLED_ID).delete());
        Tasks.await(db.collection("events").document(EVENT_ID)
                .collection("waitingList").document(PENDING_ID).delete());

        Tasks.await(db.collection("users").document(CANCELLED_ID).delete());
        Tasks.await(db.collection("users").document(PENDING_ID).delete());

        Tasks.await(db.collection("events").document(EVENT_ID).delete());

        setOrganizerCurrentEventIdSync(null);
    }

    @Test
    public void redrawReplacement_promotesPendingToSelected() throws Exception {
        // Launch the Cancelled list UI (where "Redraw Replacement" lives)
        FragmentScenario.launchInContainer(
                CancelledListFragment.class,
                (Bundle) null,
                R.style.Theme_EventLottery,
                (FragmentFactory) null
        );

        // Ensure UI is present
        onView(withId(R.id.listCancelledEntrants)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonRedrawReplacement)).check(matches(isDisplayed()));

        // loadCancelledEntries() is async; empty list blocks redraw with a toast.
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        SystemClock.sleep(2500);

        // Click redraw
        onView(withId(R.id.buttonRedrawReplacement)).perform(click());

        // Verify Firestore updated the pending entrant to SELECTED (poll a bit for async update)
        String status = pollWaitingStatus(PENDING_ID, 10, 300);
        if (!WaitingListEntry.Status.SELECTED.name().equals(status)) {
            throw new AssertionError("Expected " + PENDING_ID + " to become SELECTED, but was: " + status);
        }
    }

    private Map<String, Object> userDoc(String deviceId, String fullName) {
        Map<String, Object> u = new HashMap<>();
        u.put("deviceID", deviceId);
        u.put("fullName", fullName);
        u.put("email", deviceId + "@test.com");
        u.put("role", "Entrant");
        u.put("notificationsEnabled", false); // avoid extra notifications interfering
        return u;
    }

    private Map<String, Object> waitingEntry(String deviceId, String status) {
        Map<String, Object> e = new HashMap<>();
        e.put("deviceId", deviceId);
        e.put("status", status);
        return e;
    }

    private String pollWaitingStatus(String deviceId, int attempts, long sleepMs) throws Exception {
        for (int i = 0; i < attempts; i++) {
            DocumentSnapshot doc = Tasks.await(db.collection("events").document(EVENT_ID)
                    .collection("waitingList").document(deviceId).get());
            String st = doc.getString("status");
            if (WaitingListEntry.Status.SELECTED.name().equals(st)) return st;
            Thread.sleep(sleepMs);
        }
        DocumentSnapshot doc = Tasks.await(db.collection("events").document(EVENT_ID)
                .collection("waitingList").document(deviceId).get());
        return doc.getString("status");
    }

    private void setOrganizerCurrentEventIdSync(String eventId) {
        Context storageCtx = appContext.getApplicationContext();
        SharedPreferences sp = storageCtx.getSharedPreferences(ORG_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        if (eventId == null || eventId.isEmpty()) {
            ed.remove(KEY_ORG_CURRENT_EVENT_ID);
        } else {
            ed.putString(KEY_ORG_CURRENT_EVENT_ID, eventId);
        }
        if (!ed.commit()) {
            throw new IllegalStateException("SharedPreferences commit failed");
        }
    }
}