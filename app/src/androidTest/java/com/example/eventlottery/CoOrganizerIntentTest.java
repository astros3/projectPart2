package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.Timestamp;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Intent/UI tests for assigning co-organizers via .
 * Co-organizers are stored on the event as ; assignment also removes the
 * user from the event waiting list so they are not in the entrant pool (see app implementation).
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class CoOrganizerIntentTest {

    private static final String EVENT_ID = "test_event_co_organizer_intent";
    /** Must differ from {@link DeviceIdManager} id (organizer is excluded from the user list). */
    private static final String CO_ORG_USER_ID = "test_co_org_intent_entrant";
    private static final String CO_ORG_FULL_NAME = "CoOrgIntentUser UniqueSearchName";

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
        event.put("title", "Co-Organizer Intent Event");
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
        event.put("isPrivate", false);
        event.put("price", 0.0);
        event.put("selectionCriteria", new ArrayList<String>());
        event.put("coOrganizerIds", new ArrayList<String>());
        Tasks.await(db.collection("events").document(EVENT_ID).set(event));

        Map<String, Object> user = new HashMap<>();
        user.put("deviceID", CO_ORG_USER_ID);
        user.put("fullName", CO_ORG_FULL_NAME);
        user.put("email", "coorg.intent.user@test.com");
        user.put("phone", "7805550199");
        user.put("role", "entrant");
        user.put("notificationsEnabled", true);
        Tasks.await(db.collection("users").document(CO_ORG_USER_ID).set(user));
    }

    @After
    public void tearDown() throws Exception {
        if (db == null) {
            return;
        }
        try {
            Tasks.await(db.collection("events").document(EVENT_ID)
                    .collection("waitingList").document(CO_ORG_USER_ID).delete());
        } catch (Exception ignored) { /* */ }
        try {
            Tasks.await(db.collection("events").document(EVENT_ID).delete());
        } catch (Exception ignored) { /* */ }
        try {
            Tasks.await(db.collection("users").document(CO_ORG_USER_ID).delete());
        } catch (Exception ignored) { /* */ }
        if (organizerDeviceId != null) {
            try {
                Tasks.await(db.collection("organizers").document(organizerDeviceId).delete());
            } catch (Exception ignored) { /* */ }
        }
    }

    private Intent managementIntent() {
        Intent i = CoOrganizerManagementActivity.newIntent(appContext, EVENT_ID);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return i;
    }

    @Test
    public void coOrganizerManagement_showsTitleSearchAndUserRow() {
        try (ActivityScenario<CoOrganizerManagementActivity> scenario =
                     ActivityScenario.launch(managementIntent())) {
            onView(isRoot()).perform(waitFor(5000));
            onView(withText(appContext.getString(R.string.co_organizer_title)))
                    .check(matches(isDisplayed()));
            onView(withId(R.id.input_search_users)).check(matches(isDisplayed()));
            onView(withId(R.id.input_search_users))
                    .perform(replaceText("UniqueSearchName"), closeSoftKeyboard());
            onView(isRoot()).perform(waitFor(800));
            onView(withId(R.id.recycler_users)).check(matches(isDisplayed()));
            onView(withText(CO_ORG_FULL_NAME)).check(matches(isDisplayed()));
            // Filter to one row so "Assign" is not ambiguous when many users exist on the device.
            onView(withText(appContext.getString(R.string.assign_co_organizer)))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void organizer_assignCoOrganizer_updatesEventAndShowsRemove() throws Exception {
        try (ActivityScenario<CoOrganizerManagementActivity> scenario =
                     ActivityScenario.launch(managementIntent())) {
            onView(isRoot()).perform(waitFor(5000));
            onView(withId(R.id.input_search_users))
                    .perform(replaceText("UniqueSearchName"), closeSoftKeyboard());
            onView(isRoot()).perform(waitFor(800));
            onView(withText(appContext.getString(R.string.assign_co_organizer))).perform(click());
            onView(isRoot()).perform(waitFor(5000));

            DocumentSnapshot ev = Tasks.await(db.collection("events").document(EVENT_ID).get());
            assertTrue(ev.exists());
            @SuppressWarnings("unchecked")
            List<String> ids = (List<String>) ev.get("coOrganizerIds");
            assertNotNull(ids);
            assertTrue(ids.contains(CO_ORG_USER_ID));

            onView(withText(appContext.getString(R.string.remove_co_organizer)))
                    .check(matches(isDisplayed()));
            onView(withText(appContext.getString(R.string.co_organizer_badge)))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void assignCoOrganizer_removesUserFromWaitingList() throws Exception {
        Map<String, Object> wl = new HashMap<>();
        wl.put("deviceId", CO_ORG_USER_ID);
        wl.put("status", "PENDING");
        wl.put("joinTimestamp", Timestamp.now());
        Tasks.await(db.collection("events").document(EVENT_ID)
                .collection("waitingList").document(CO_ORG_USER_ID).set(wl));

        DocumentSnapshot before = Tasks.await(db.collection("events").document(EVENT_ID)
                .collection("waitingList").document(CO_ORG_USER_ID).get());
        assertTrue(before.exists());

        try (ActivityScenario<CoOrganizerManagementActivity> scenario =
                     ActivityScenario.launch(managementIntent())) {
            onView(isRoot()).perform(waitFor(5000));
            onView(withId(R.id.input_search_users))
                    .perform(replaceText("UniqueSearchName"), closeSoftKeyboard());
            onView(isRoot()).perform(waitFor(800));
            onView(withText(appContext.getString(R.string.assign_co_organizer))).perform(click());
            onView(isRoot()).perform(waitFor(6000));
        }

        DocumentSnapshot after = Tasks.await(db.collection("events").document(EVENT_ID)
                .collection("waitingList").document(CO_ORG_USER_ID).get());
        assertFalse(after.exists());
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
