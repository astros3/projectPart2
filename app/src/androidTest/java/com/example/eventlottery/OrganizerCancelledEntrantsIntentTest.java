package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

/**
 * Intent/UI tests for the organizer cancelled entrants list .
 * Story: As an organizer I want to see a list of all the cancelled entrants.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerCancelledEntrantsIntentTest {

    private static final String EVENT_ID = "test_event_cancelled_entrants_us";
    private static final String ENTRANT_CANCELLED = "test_entrant_cancelled_list_a";
    private static final String ENTRANT_DECLINED = "test_entrant_cancelled_list_b";
    private static final String ENTRANT_PENDING = "test_entrant_cancelled_list_c";

    private FirebaseFirestore db;
    private Context appContext;

    @Before
    public void setUp() throws Exception {
        appContext = ApplicationProvider.getApplicationContext();
        db = FirebaseFirestore.getInstance();
        EventEditActivity.setCurrentEventId(appContext, EVENT_ID);

        Map<String, Object> event = new HashMap<>();
        event.put("eventId", EVENT_ID);
        event.put("title", "Cancelled List Test Event");
        event.put("organizerId", "test_org_cancelled");
        event.put("organizerName", "Test Organizer");
        event.put("isPrivate", false);
        event.put("capacity", 10);
        event.put("waitingListLimit", 20);
        event.put("registrationStartMillis", System.currentTimeMillis() - 60_000L);
        event.put("registrationEndMillis", System.currentTimeMillis() + 86_400_000L);
        event.put("eventDateMillis", System.currentTimeMillis() + 7L * 86_400_000L);
        event.put("geolocationRequired", false);
        Tasks.await(db.collection("events").document(EVENT_ID).set(event));

        Tasks.await(db.collection("users").document(ENTRANT_CANCELLED).set(
                userDoc(ENTRANT_CANCELLED, "Alice Cancelled")));
        Tasks.await(db.collection("users").document(ENTRANT_DECLINED).set(
                userDoc(ENTRANT_DECLINED, "Bob Declined")));
        Tasks.await(db.collection("users").document(ENTRANT_PENDING).set(
                userDoc(ENTRANT_PENDING, "Carl Pending")));

        Tasks.await(db.collection("events").document(EVENT_ID)
                .collection("waitingList").document(ENTRANT_CANCELLED)
                .set(waitingEntry(ENTRANT_CANCELLED, WaitingListEntry.Status.CANCELLED.name())));
        Tasks.await(db.collection("events").document(EVENT_ID)
                .collection("waitingList").document(ENTRANT_DECLINED)
                .set(waitingEntry(ENTRANT_DECLINED, WaitingListEntry.Status.DECLINED.name())));
        Tasks.await(db.collection("events").document(EVENT_ID)
                .collection("waitingList").document(ENTRANT_PENDING)
                .set(waitingEntry(ENTRANT_PENDING, WaitingListEntry.Status.PENDING.name())));
    }

    @After
    public void tearDown() throws Exception {
        if (db == null) {
            return;
        }
        Tasks.await(db.collection("events").document(EVENT_ID)
                .collection("waitingList").document(ENTRANT_CANCELLED).delete());
        Tasks.await(db.collection("events").document(EVENT_ID)
                .collection("waitingList").document(ENTRANT_DECLINED).delete());
        Tasks.await(db.collection("events").document(EVENT_ID)
                .collection("waitingList").document(ENTRANT_PENDING).delete());
        Tasks.await(db.collection("users").document(ENTRANT_CANCELLED).delete());
        Tasks.await(db.collection("users").document(ENTRANT_DECLINED).delete());
        Tasks.await(db.collection("users").document(ENTRANT_PENDING).delete());
        Tasks.await(db.collection("events").document(EVENT_ID).delete());
        EventEditActivity.setCurrentEventId(appContext, null);
    }

    @Test
    public void cancelledEntrantsScreen_showsTitleAndList() {
        FragmentScenario.launchInContainer(
                CancelledListFragment.class,
                (Bundle) null,
                R.style.Theme_EventLottery,
                (FragmentFactory) null);

        onView(isRoot()).perform(waitFor(3000));
        onView(withText(R.string.cancelled_entrants)).check(matches(isDisplayed()));
        onView(withId(R.id.listCancelledEntrants)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonBack)).check(matches(isDisplayed()));
    }

    @Test
    public void cancelledEntrantsList_showsCancelledAndDeclinedNames_notPending() {
        FragmentScenario.launchInContainer(
                CancelledListFragment.class,
                (Bundle) null,
                R.style.Theme_EventLottery,
                (FragmentFactory) null);

        onView(isRoot()).perform(waitFor(3500));

        onView(withText("Alice Cancelled")).check(matches(isDisplayed()));
        onView(withText("Bob Declined")).check(matches(isDisplayed()));
        onView(withText("Carl Pending")).check(doesNotExist());
    }

    private static Map<String, Object> userDoc(String deviceId, String fullName) {
        Map<String, Object> u = new HashMap<>();
        u.put("deviceID", deviceId);
        u.put("fullName", fullName);
        u.put("email", deviceId + "@test.com");
        u.put("role", "entrant");
        u.put("notificationsEnabled", false);
        return u;
    }

    private static Map<String, Object> waitingEntry(String deviceId, String status) {
        Map<String, Object> e = new HashMap<>();
        e.put("deviceId", deviceId);
        e.put("status", status);
        return e;
    }

    private static ViewAction waitFor(long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "wait for " + millis + "ms";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }
}
