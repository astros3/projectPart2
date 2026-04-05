package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Intent/UI tests for.
 * US 02.01.03 — Organizer invites specific entrants to a private event’s waiting list by
 * searching via name, phone, and/or email.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerInviteEntrantIntentTest {

    private static final String EVENT_ID = "test_event_organizer_invite_intent";
    /** Must differ from DeviceIdManager id (organizer is excluded from the list). */
    private static final String ENTRANT_MATCH_ID = "test_invite_intent_match_user";
    private static final String ENTRANT_OTHER_ID = "test_invite_intent_other_user";

    private static final String NAME_MATCH = "Quinn SearchableNameX";
    private static final String NAME_OTHER = "Xavier Unrelated";
    private static final String EMAIL_MATCH = "quinn.search.x@test.com";
    private static final String PHONE_MATCH = "7805550142";

    private Context appContext;
    private FirebaseFirestore db;
    private String organizerDeviceId;

    @Before
    public void setUp() throws Exception {
        appContext = ApplicationProvider.getApplicationContext();
        db = FirebaseFirestore.getInstance();
        organizerDeviceId = DeviceIdManager.getDeviceId(appContext);

        Map<String, Object> organizer = new HashMap<>();
        organizer.put("organizerId", organizerDeviceId);
        organizer.put("firstName", "Test");
        organizer.put("lastName", "Organizer");
        organizer.put("role", "organizer");
        Tasks.await(db.collection("organizers").document(organizerDeviceId).set(organizer));

        long now = System.currentTimeMillis();
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", EVENT_ID);
        event.put("title", "Private Invite Intent Event");
        event.put("description", "Test");
        event.put("location", "Edmonton");
        event.put("organizerId", organizerDeviceId);
        event.put("organizerName", "Test Organizer");
        event.put("capacity", 10);
        event.put("waitingListLimit", 20);
        event.put("registrationStartMillis", now - TimeUnit.DAYS.toMillis(1));
        event.put("registrationEndMillis", now + TimeUnit.DAYS.toMillis(7));
        event.put("eventDateMillis", now + TimeUnit.DAYS.toMillis(14));
        event.put("geolocationRequired", false);
        event.put("isPrivate", true);
        event.put("price", 0.0);
        event.put("selectionCriteria", new ArrayList<String>());
        Tasks.await(db.collection("events").document(EVENT_ID).set(event));

        Tasks.await(db.collection("users").document(ENTRANT_MATCH_ID).set(
                userDoc(ENTRANT_MATCH_ID, NAME_MATCH, EMAIL_MATCH, PHONE_MATCH)));
        Tasks.await(db.collection("users").document(ENTRANT_OTHER_ID).set(
                userDoc(ENTRANT_OTHER_ID, NAME_OTHER, "other.person@test.com", "1112223344")));
    }

    @After
    public void tearDown() throws Exception {
        if (db == null) {
            return;
        }
        try {
            Tasks.await(db.collection("events").document(EVENT_ID)
                    .collection("waitingList").document(ENTRANT_MATCH_ID).delete());
        } catch (Exception ignored) { /* */ }
        try {
            Tasks.await(db.collection("events").document(EVENT_ID)
                    .collection("waitingList").document(ENTRANT_OTHER_ID).delete());
        } catch (Exception ignored) { /* */ }
        try {
            Tasks.await(db.collection("events").document(EVENT_ID).delete());
        } catch (Exception ignored) { /* */ }
        try {
            Tasks.await(db.collection("users").document(ENTRANT_MATCH_ID).delete());
        } catch (Exception ignored) { /* */ }
        try {
            Tasks.await(db.collection("users").document(ENTRANT_OTHER_ID).delete());
        } catch (Exception ignored) { /* */ }
        if (organizerDeviceId != null) {
            try {
                Tasks.await(db.collection("organizers").document(organizerDeviceId).delete());
            } catch (Exception ignored) { /* */ }
        }
    }

    private Intent inviteIntent() {
        Intent i = OrganizerInviteEntrantActivity.newIntent(appContext, EVENT_ID);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return i;
    }

    @Test
    public void inviteEntrantScreen_showsToolbarSearchFieldAndRecycler() {
        try (ActivityScenario<OrganizerInviteEntrantActivity> scenario =
                     ActivityScenario.launch(inviteIntent())) {
            onView(isRoot()).perform(waitFor(4000));
            onView(withText("Invite Entrants")).check(matches(isDisplayed()));
            onView(withId(R.id.back_button_invite)).check(matches(isDisplayed()));
            onView(withId(R.id.input_search_entrant)).check(matches(isDisplayed()));
            onView(withId(R.id.recycler_invite_entrants)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void inviteEntrant_searchByName_filtersToMatchingEntrant() {
        try (ActivityScenario<OrganizerInviteEntrantActivity> scenario =
                     ActivityScenario.launch(inviteIntent())) {
            onView(isRoot()).perform(waitFor(4000));
            onView(withId(R.id.input_search_entrant))
                    .perform(replaceText("SearchableNameX"), closeSoftKeyboard());
            onView(isRoot()).perform(waitFor(600));
            onView(withText(NAME_MATCH)).check(matches(isDisplayed()));
            onView(withText(NAME_OTHER)).check(doesNotExist());
        }
    }

    @Test
    public void inviteEntrant_searchByEmail_filtersToMatchingEntrant() {
        try (ActivityScenario<OrganizerInviteEntrantActivity> scenario =
                     ActivityScenario.launch(inviteIntent())) {
            onView(isRoot()).perform(waitFor(4000));
            onView(withId(R.id.input_search_entrant)).perform(clearText());
            onView(withId(R.id.input_search_entrant))
                    .perform(replaceText("quinn.search.x"), closeSoftKeyboard());
            onView(isRoot()).perform(waitFor(600));
            onView(withText(NAME_MATCH)).check(matches(isDisplayed()));
            onView(withText(NAME_OTHER)).check(doesNotExist());
        }
    }

    @Test
    public void inviteEntrant_searchByPhone_filtersToMatchingEntrant() {
        try (ActivityScenario<OrganizerInviteEntrantActivity> scenario =
                     ActivityScenario.launch(inviteIntent())) {
            onView(isRoot()).perform(waitFor(4000));
            onView(withId(R.id.input_search_entrant)).perform(clearText());
            onView(withId(R.id.input_search_entrant))
                    .perform(replaceText("780555"), closeSoftKeyboard());
            onView(isRoot()).perform(waitFor(600));
            onView(withText(NAME_MATCH)).check(matches(isDisplayed()));
            onView(withText(NAME_OTHER)).check(doesNotExist());
        }
    }

    private static Map<String, Object> userDoc(String deviceId, String fullName, String email, String phone) {
        Map<String, Object> u = new HashMap<>();
        u.put("deviceID", deviceId);
        u.put("fullName", fullName);
        u.put("email", email);
        u.put("phone", phone);
        u.put("role", "entrant");
        u.put("notificationsEnabled", true);
        return u;
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
