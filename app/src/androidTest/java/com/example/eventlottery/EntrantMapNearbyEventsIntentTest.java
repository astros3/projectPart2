package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static org.junit.Assume.assumeTrue;
import android.content.Intent;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.rule.GrantPermissionRule;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

/**
 * Wow Factor-1: Map Nearby Events Functionality
 *
 * Verifies:
 * - Map screen launches and is visible
 * - Passing filter "tags" (eventType) via EventFilterCriteria does not crash
 * - Clustering strategy is enabled (indirect: activity wires ClusterManager listeners)
 *
 */
@RunWith(AndroidJUnit4.class)
public class EntrantMapNearbyEventsIntentTest {

    private FirebaseFirestore db;

    private final String EVENT_FITNESS = "test_map_event_fitness";
    private final String EVENT_MUSIC = "test_map_event_music";

    /**
     * Prevent runtime permission dialogs (which pause the activity and can cause
     * NoActivityResumedException during Espresso assertions).
     */
    @Rule
    public GrantPermissionRule grantLocation =
            GrantPermissionRule.grant(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            );

    /**
     * Google Maps UI tests require Google Play services on the device/emulator.
     * If the current test device image doesn't include/enable Play services,
     * Map activities can finish/crash before Espresso can match views.
     */
    private void assumePlayServicesAvailable() {
        int status = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(ApplicationProvider.getApplicationContext());
        assumeTrue("Google Play services not available on this device/emulator", status == ConnectionResult.SUCCESS);
    }

    @Before
    public void seedEventsWithCoordinates() throws Exception {
        db = FirebaseFirestore.getInstance();
        long now = System.currentTimeMillis();

        // Event "Fitness" in Edmonton-ish
        Tasks.await(db.collection("events").document(EVENT_FITNESS).set(eventDoc(
                "Nearby Fitness Event",
                "Tag: Fitness",
                "Fitness",
                53.5461, -113.4938,
                now - 60_000L, now + 86_400_000L
        )));

        // Event "Music" nearby too
        Tasks.await(db.collection("events").document(EVENT_MUSIC).set(eventDoc(
                "Nearby Music Event",
                "Tag: Music",
                "Music",
                53.5444, -113.4909,
                now - 60_000L, now + 86_400_000L
        )));
    }

    @After
    public void cleanup() throws Exception {
        if (db == null) return;
        Tasks.await(db.collection("events").document(EVENT_FITNESS).delete());
        Tasks.await(db.collection("events").document(EVENT_MUSIC).delete());
    }

    @Test
    public void mapLaunches_withNoFilter() {
        assumePlayServicesAvailable();
        Intent i = new Intent(ApplicationProvider.getApplicationContext(), EntrantMapActivity.class);
        i.putExtra(EntrantMapActivity.EXTRA_FILTER, EventFilterCriteria.empty());

        ActivityScenario.launch(i);
        // Let the Activity inflate its layout before we assert.
        onView(isRoot()).perform(waitFor(500));

        onView(withId(R.id.toolbar_map)).check(matches(isDisplayed()));
        onView(withText(R.string.entrant_map_title)).check(matches(isDisplayed()));
        onView(withId(R.id.map)).check(matches(isDisplayed()));
    }

    @Test
    public void mapLaunches_withTagFilter_eventTypeFitness() {
        assumePlayServicesAvailable();
        EventFilterCriteria c = EventFilterCriteria.empty();
        c.setEventType("Fitness"); // "tag" filter
        Intent i = new Intent(ApplicationProvider.getApplicationContext(), EntrantMapActivity.class);
        i.putExtra(EntrantMapActivity.EXTRA_FILTER, c);

        ActivityScenario.launch(i);

        onView(withId(R.id.toolbar_map)).check(matches(isDisplayed()));
        onView(withId(R.id.map)).check(matches(isDisplayed()));

        // Give map/Firestore time to load and cluster; test passes if no crash.
        onView(isRoot()).perform(waitFor(2000));
    }

    @Test
    public void mapLaunches_withDifferentTagFilter_eventTypeMusic() {
        assumePlayServicesAvailable();
        EventFilterCriteria c = EventFilterCriteria.empty();
        c.setEventType("Music");
        Intent i = new Intent(ApplicationProvider.getApplicationContext(), EntrantMapActivity.class);
        i.putExtra(EntrantMapActivity.EXTRA_FILTER, c);

        ActivityScenario.launch(i);

        onView(withId(R.id.toolbar_map)).check(matches(isDisplayed()));
        onView(withId(R.id.map)).check(matches(isDisplayed()));
        onView(isRoot()).perform(waitFor(2000));
    }

    private static Map<String, Object> eventDoc(String title,
                                                String description,
                                                String eventType,
                                                double lat,
                                                double lng,
                                                long regStartMillis,
                                                long regEndMillis) {
        long now = System.currentTimeMillis();
        Map<String, Object> m = new HashMap<>();
        m.put("title", title);
        m.put("description", description);
        m.put("eventType", eventType);

        m.put("latitude", lat);
        m.put("longitude", lng);

        m.put("registrationStartMillis", regStartMillis);
        m.put("registrationEndMillis", regEndMillis);

        m.put("eventDateMillis", now + 7L * 86_400_000L);
        m.put("capacity", 10);
        m.put("waitingListLimit", 50);
        m.put("organizerId", "test_org");
        m.put("organizerName", "Test Organizer");
        m.put("geolocationRequired", false);
        m.put("isPrivate", false);
        return m;
    }

    private static ViewAction waitFor(long millis) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return androidx.test.espresso.matcher.ViewMatchers.isRoot(); }
            @Override public String getDescription() { return "wait for " + millis + "ms"; }
            @Override public void perform(UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }
}