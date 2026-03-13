package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Intent tests for create/edit event screen (organizer).
 * US 02.01.01 - Create event and generate QR; US 02.01.04 - Set registration period;
 * US 02.03.01 - Optionally limit waiting list; US 02.04.01 - Upload event poster.
 *
 * Creates a test organizer and event in Firestore in @Before, launches EventEditActivity
 * in edit mode for each test, and deletes the event in @After. No hardcoded event ID needed.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EventEditIntentTest {

    private String deviceId;
    private String createdEventId;

    @Before
    public void createTestOrganizerAndEvent() throws Exception {
        Context ctx = ApplicationProvider.getApplicationContext();
        deviceId = DeviceIdManager.getDeviceId(ctx);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Ensure test device is an organizer so edit form is shown
        Map<String, Object> organizer = new HashMap<>();
        organizer.put("organizerId", deviceId);
        organizer.put("firstName", "Test");
        organizer.put("lastName", "Organizer");
        organizer.put("role", "organizer");
        Tasks.await(db.collection("organizers").document(deviceId).set(organizer));

        // Create a test event owned by this organizer
        createdEventId = db.collection("events").document().getId();
        long now = System.currentTimeMillis();
        Map<String, Object> event = new HashMap<>();
        event.put("title", "Test Event for UI Test");
        event.put("description", "Description");
        event.put("location", "Edmonton");
        event.put("organizerId", deviceId);
        event.put("organizerName", "Test Organizer");
        event.put("capacity", 10);
        event.put("waitingListLimit", 0);
        event.put("registrationStartMillis", now - TimeUnit.DAYS.toMillis(1));
        event.put("registrationEndMillis", now + TimeUnit.DAYS.toMillis(7));
        event.put("eventDateMillis", now + TimeUnit.DAYS.toMillis(14));
        event.put("geolocationRequired", false);
        event.put("price", 0.0);
        event.put("selectionCriteria", new ArrayList<String>());

        Tasks.await(db.collection("events").document(createdEventId).set(event));
    }

    @After
    public void deleteTestEvent() throws Exception {
        if (createdEventId != null) {
            Tasks.await(
                    FirebaseFirestore.getInstance()
                            .collection("events")
                            .document(createdEventId)
                            .delete()
            );
        }
    }

    private Intent createEditIntent() {
        Intent intent = EventEditActivity.newIntent(ApplicationProvider.getApplicationContext(), createdEventId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    @Test
    public void testEventEditScreenHasNameDescriptionAndLocation() {
        try (ActivityScenario<EventEditActivity> scenario = ActivityScenario.launch(createEditIntent())) {
            onView(withId(R.id.input_event_name)).check(matches(isDisplayed()));
            onView(withId(R.id.input_event_description)).check(matches(isDisplayed()));
            onView(withId(R.id.input_event_location)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testRegistrationPeriodFieldsAreDisplayed() {
        try (ActivityScenario<EventEditActivity> scenario = ActivityScenario.launch(createEditIntent())) {
            onView(withId(R.id.input_registration_start)).check(matches(isDisplayed()));
            onView(withId(R.id.input_registration_end)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testWaitingListLimitAndGeolocationSwitchAreDisplayed() {
        try (ActivityScenario<EventEditActivity> scenario = ActivityScenario.launch(createEditIntent())) {
            onView(withId(R.id.input_waiting_list_limit)).check(matches(isDisplayed()));
            onView(withId(R.id.switch_geolocation)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testEventPosterPlaceholderAndConfirmButtonAreDisplayed() {
        try (ActivityScenario<EventEditActivity> scenario = ActivityScenario.launch(createEditIntent())) {
            onView(withId(R.id.event_image_container)).check(matches(isDisplayed()));
            onView(withId(R.id.event_poster_placeholder)).check(matches(isDisplayed()));
            onView(withId(R.id.btn_confirm)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testOrganizerCanEnterEventNameAndDescription() {
        try (ActivityScenario<EventEditActivity> scenario = ActivityScenario.launch(createEditIntent())) {
            onView(withId(R.id.input_event_name)).perform(typeText(" Test"), closeSoftKeyboard());
            onView(withId(R.id.input_event_description)).perform(typeText(" More description"), closeSoftKeyboard());
            onView(withId(R.id.input_event_name)).check(matches(isDisplayed()));
        }
    }
}
