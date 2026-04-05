package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.intent.Intents.intending;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static androidx.test.espresso.intent.matcher.IntentMatchers.hasType;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.allOf;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Base64;
import android.view.View;

import androidx.core.content.FileProvider;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.espresso.intent.Intents;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Intent tests for create/edit event screen (organizer) — single screen, shared Firestore setup.
 * US 02.01.01 - Create event and generate QR; US 02.01.04 - Set registration period;
 * US 02.03.01 - Optionally limit waiting list; US 02.04.01 - Event poster;
 * Geolocation - enable/disable requirement for the event.
 * Creates a test organizer and event in Firestore in @Before, launches EventEditActivity
 * in edit mode for each test. In @After deletes the event and the organizer doc so the test is idempotent.
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
    public void deleteTestEventAndOrganizer() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (createdEventId != null) {
            Tasks.await(db.collection("events").document(createdEventId).delete());
        }
        if (deviceId != null) {
            Tasks.await(db.collection("organizers").document(deviceId).delete());
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
            onView(withId(R.id.input_waiting_list_limit)).perform(scrollTo());
            onView(withId(R.id.input_waiting_list_limit)).check(matches(isDisplayed()));
            onView(withId(R.id.switch_geolocation)).perform(scrollTo());
            onView(withId(R.id.switch_geolocation)).check(matches(isDisplayed()));
        }
    }

    /**
     * Opens edit for the seeded event (geolocationRequired false), waits for Firestore/form load,
     * asserts the switch is off, taps it, asserts on.
     */
    @Test
    public void organizer_canEnableGeolocationRequirement() {
        try (ActivityScenario<EventEditActivity> scenario = ActivityScenario.launch(createEditIntent())) {
            onView(isRoot()).perform(waitFor(2500));
            onView(withId(R.id.switch_geolocation)).perform(scrollTo());
            onView(withId(R.id.switch_geolocation)).check(matches(not(isChecked())));
            onView(withId(R.id.switch_geolocation)).perform(click());
            onView(withId(R.id.switch_geolocation)).check(matches(isChecked()));
        }
    }

    /**
     * Updates the same event with geolocationRequired true, launches edit, waits,
     * asserts switch on, taps, asserts off.
     */
    @Test
    public void organizer_canDisableGeolocationRequirement() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Tasks.await(db.collection("events").document(createdEventId)
                .update("geolocationRequired", true));

        try (ActivityScenario<EventEditActivity> scenario = ActivityScenario.launch(createEditIntent())) {
            onView(isRoot()).perform(waitFor(2500));
            onView(withId(R.id.switch_geolocation)).perform(scrollTo());
            onView(withId(R.id.switch_geolocation)).check(matches(isChecked()));
            onView(withId(R.id.switch_geolocation)).perform(click());
            onView(withId(R.id.switch_geolocation)).check(matches(not(isChecked())));
        }
    }

    @Test
    public void testEventPosterPlaceholderAndConfirmButtonAreDisplayed() {
        try (ActivityScenario<EventEditActivity> scenario = ActivityScenario.launch(createEditIntent())) {
            onView(withId(R.id.event_image_container)).check(matches(isDisplayed()));
            onView(withId(R.id.event_poster_placeholder)).check(matches(isDisplayed()));
            onView(withId(R.id.btn_confirm)).perform(scrollTo());
            onView(withId(R.id.btn_confirm)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void editEvent_whenPosterStored_showsRemovePosterControl() throws Exception {
        Context ctx = ApplicationProvider.getApplicationContext();
        String posterUri = "android.resource://" + ctx.getPackageName() + "/" + R.drawable.logo;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Tasks.await(db.collection("events").document(createdEventId).update("posterUri", posterUri));

        try (ActivityScenario<EventEditActivity> scenario = ActivityScenario.launch(createEditIntent())) {
            onView(isRoot()).perform(waitFor(3500));
            onView(withId(R.id.event_image_container)).perform(scrollTo());
            onView(withId(R.id.btn_remove_poster)).check(matches(isDisplayed()));
            onView(withId(R.id.event_image_placeholder_text)).check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void organizer_canRemovePoster_showsPlaceholderAgain() throws Exception {
        Context ctx = ApplicationProvider.getApplicationContext();
        String posterUri = "android.resource://" + ctx.getPackageName() + "/" + R.drawable.logo;
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Tasks.await(db.collection("events").document(createdEventId).update("posterUri", posterUri));

        try (ActivityScenario<EventEditActivity> scenario = ActivityScenario.launch(createEditIntent())) {
            onView(isRoot()).perform(waitFor(3500));
            onView(withId(R.id.event_image_container)).perform(scrollTo());
            onView(withId(R.id.btn_remove_poster)).perform(click());
            onView(withId(R.id.event_image_placeholder_text)).check(matches(isDisplayed()));
            onView(withId(R.id.btn_remove_poster)).check(matches(not(isDisplayed())));
        }
    }

    @Test
    public void organizer_canPickNewPoster_stubbedImageIntent() throws Exception {
        Intents.init();
        try {
            Context ctx = ApplicationProvider.getApplicationContext();
            Uri imageUri = writeMinimalPngToCacheAndGetUri(ctx);
            Intent resultData = new Intent();
            resultData.setData(imageUri);
            resultData.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Instrumentation.ActivityResult stubResult =
                    new Instrumentation.ActivityResult(Activity.RESULT_OK, resultData);
            intending(allOf(
                    hasAction(Intent.ACTION_GET_CONTENT),
                    hasType("image/*"))).respondWith(stubResult);

            try (ActivityScenario<EventEditActivity> scenario = ActivityScenario.launch(createEditIntent())) {
                onView(isRoot()).perform(waitFor(2500));
                onView(withId(R.id.event_image_container)).perform(scrollTo());
                onView(withId(R.id.event_image_container)).perform(click());
                onView(isRoot()).perform(waitFor(800));
                onView(withId(R.id.btn_remove_poster)).check(matches(isDisplayed()));
                onView(withId(R.id.event_image_placeholder_text)).check(matches(not(isDisplayed())));
            }
        } finally {
            Intents.release();
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

    private static Uri writeMinimalPngToCacheAndGetUri(Context ctx) throws IOException {
        byte[] png = Base64.decode(
                "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==",
                Base64.DEFAULT);
        File f = new File(ctx.getCacheDir(), "intent_test_poster.png");
        try (FileOutputStream out = new FileOutputStream(f)) {
            out.write(png);
        }
        return FileProvider.getUriForFile(ctx, ctx.getPackageName() + ".fileprovider", f);
    }
}
