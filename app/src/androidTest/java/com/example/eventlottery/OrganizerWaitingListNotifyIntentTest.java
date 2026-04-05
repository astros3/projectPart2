package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

/**
 * As an organizer, send notifications to all entrants on the waiting list (PENDING).
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerWaitingListNotifyIntentTest {

    private static final String ORG_PREFS_NAME = "EventLotteryPrefs";
    private static final String KEY_ORG_CURRENT_EVENT_ID = "organizer_current_event_id";

    private static final String EVENT_ID = "test_event_waitlist_notify_intent";
    private static final String ENTRANT_ID = "test_entrant_waitlist_notify";

    private FirebaseFirestore db;
    private Context appContext;

    @Before
    public void setUp() throws Exception {
        appContext = ApplicationProvider.getApplicationContext();
        db = FirebaseFirestore.getInstance();
        setOrganizerCurrentEventIdSync(EVENT_ID);

        Map<String, Object> event = new HashMap<>();
        event.put("eventId", EVENT_ID);
        event.put("title", "Waitlist Notify Test");
        event.put("organizerId", "test_org_waitlist_notify");
        event.put("organizerName", "Test Organizer");
        event.put("isPrivate", false);
        event.put("capacity", 10);
        event.put("waitingListLimit", 20);
        event.put("registrationStartMillis", System.currentTimeMillis() - 60_000L);
        event.put("registrationEndMillis", System.currentTimeMillis() + 86_400_000L);
        event.put("eventDateMillis", System.currentTimeMillis() + 7L * 86_400_000L);
        event.put("geolocationRequired", false);
        Tasks.await(db.collection("events").document(EVENT_ID).set(event));

        Map<String, Object> user = new HashMap<>();
        user.put("deviceID", ENTRANT_ID);
        user.put("fullName", "Waitlist Notify User");
        user.put("email", ENTRANT_ID + "@test.com");
        user.put("role", "entrant");
        user.put("notificationsEnabled", true);
        Tasks.await(db.collection("users").document(ENTRANT_ID).set(user));

        Map<String, Object> waiting = new HashMap<>();
        waiting.put("deviceId", ENTRANT_ID);
        waiting.put("status", WaitingListEntry.Status.PENDING.name());
        Tasks.await(db.collection("events").document(EVENT_ID)
                .collection("waitingList").document(ENTRANT_ID).set(waiting));

        deleteNotificationsForEntrantEvent(ENTRANT_ID, EVENT_ID);
    }

    @After
    public void tearDown() throws Exception {
        if (db == null) {
            return;
        }
        deleteNotificationsForEntrantEvent(ENTRANT_ID, EVENT_ID);
        try {
            Tasks.await(db.collection("events").document(EVENT_ID)
                    .collection("waitingList").document(ENTRANT_ID).delete());
        } catch (Exception ignored) { /* */ }
        try {
            Tasks.await(db.collection("users").document(ENTRANT_ID).delete());
        } catch (Exception ignored) { /* */ }
        try {
            Tasks.await(db.collection("events").document(EVENT_ID).delete());
        } catch (Exception ignored) { /* */ }
        setOrganizerCurrentEventIdSync(null);
    }

    @Test
    public void waitingList_notifyButton_createsWaitingListUpdateNotificationForPending() throws Exception {
        FragmentScenario.launchInContainer(
                WaitingListFragment.class,
                (Bundle) null,
                R.style.Theme_EventLottery,
                (FragmentFactory) null);

        onView(withId(R.id.listWaitingEntrants)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonNotifyWaiting)).check(matches(isDisplayed()));

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        Thread.sleep(2500);

        onView(withId(R.id.buttonNotifyWaiting)).perform(click());

        waitUntilNotificationExists(ENTRANT_ID, EVENT_ID, NotificationHelper.TYPE_WAITING_LIST_UPDATE, 20, 400);
    }

    private void waitUntilNotificationExists(String deviceId, String eventId, String type, int attempts, long sleepMs)
            throws Exception {
        for (int i = 0; i < attempts; i++) {
            QuerySnapshot snap = Tasks.await(
                    db.collection("users").document(deviceId)
                            .collection("notifications")
                            .whereEqualTo("eventId", eventId)
                            .whereEqualTo("type", type)
                            .get());
            if (snap != null && !snap.isEmpty()) {
                return;
            }
            Thread.sleep(sleepMs);
        }
        throw new AssertionError("Expected notification type=" + type + " for eventId=" + eventId);
    }

    private void deleteNotificationsForEntrantEvent(String deviceId, String eventId) throws Exception {
        QuerySnapshot snap = Tasks.await(
                db.collection("users").document(deviceId)
                        .collection("notifications")
                        .whereEqualTo("eventId", eventId)
                        .get());
        if (snap == null || snap.isEmpty()) {
            return;
        }
        for (DocumentSnapshot doc : snap.getDocuments()) {
            Tasks.await(doc.getReference().delete());
        }
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
