package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.Matchers.anything;

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

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

/**
 * As an organizer, cancel a selected entrant who has not completed signup (sets waiting list to CANCELLED).
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerSelectedListCancelIntentTest {

    private static final String ORG_PREFS_NAME = "EventLotteryPrefs";
    private static final String KEY_ORG_CURRENT_EVENT_ID = "organizer_current_event_id";

    private static final String EVENT_ID = "test_event_selected_cancel_intent";
    private static final String ENTRANT_ID = "test_entrant_selected_cancel";

    private FirebaseFirestore db;
    private Context appContext;

    @Before
    public void setUp() throws Exception {
        appContext = ApplicationProvider.getApplicationContext();
        db = FirebaseFirestore.getInstance();
        setOrganizerCurrentEventIdSync(EVENT_ID);

        Map<String, Object> event = new HashMap<>();
        event.put("eventId", EVENT_ID);
        event.put("title", "Selected Cancel Test");
        event.put("organizerId", "test_org_selected_cancel");
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
        user.put("fullName", "Zebra Selected Cancel User");
        user.put("email", ENTRANT_ID + "@test.com");
        user.put("role", "entrant");
        user.put("notificationsEnabled", false);
        Tasks.await(db.collection("users").document(ENTRANT_ID).set(user));

        Map<String, Object> waiting = new HashMap<>();
        waiting.put("deviceId", ENTRANT_ID);
        waiting.put("status", WaitingListEntry.Status.SELECTED.name());
        Tasks.await(db.collection("events").document(EVENT_ID)
                .collection("waitingList").document(ENTRANT_ID).set(waiting));
    }

    @After
    public void tearDown() throws Exception {
        if (db == null) {
            return;
        }
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
    public void selectedList_cancelButton_setsWaitingListStatusCancelled() throws Exception {
        FragmentScenario.launchInContainer(
                SelectedList.class,
                (Bundle) null,
                R.style.Theme_EventLottery,
                (FragmentFactory) null);

        onView(withId(R.id.listSelectedEntrants)).check(matches(isDisplayed()));

        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        Thread.sleep(3000);

        onData(anything())
                .inAdapterView(withId(R.id.listSelectedEntrants))
                .atPosition(0)
                .onChildView(withId(R.id.buttonDelete))
                .perform(click());

        String status = pollWaitingStatus(15, 400);
        Assert.assertEquals(
                WaitingListEntry.Status.CANCELLED.name(),
                status);
    }

    private String pollWaitingStatus(int attempts, long sleepMs) throws Exception {
        for (int i = 0; i < attempts; i++) {
            DocumentSnapshot doc = Tasks.await(db.collection("events").document(EVENT_ID)
                    .collection("waitingList").document(ENTRANT_ID).get());
            String st = doc.getString("status");
            if (WaitingListEntry.Status.CANCELLED.name().equals(st)) {
                return st;
            }
            Thread.sleep(sleepMs);
        }
        DocumentSnapshot doc = Tasks.await(db.collection("events").document(EVENT_ID)
                .collection("waitingList").document(ENTRANT_ID).get());
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
