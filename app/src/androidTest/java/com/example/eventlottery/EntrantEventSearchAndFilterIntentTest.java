package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.anything;

import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;

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
 * Entrant UI tests for:
 * - Keyword search
 * - Availability (registration open) filtering
 * - Keyword + filtering combined
 */
@RunWith(AndroidJUnit4.class)
public class EntrantEventSearchAndFilterIntentTest {

    private FirebaseFirestore db;

    private final String EVENT_OPEN_YOGA = "test_event_open_yoga";
    private final String EVENT_CLOSED_YOGA = "test_event_closed_yoga";
    private final String EVENT_OPEN_MUSIC = "test_event_open_music";

    @Before
    public void seedFirestore() throws Exception {
        db = FirebaseFirestore.getInstance();

        long now = System.currentTimeMillis();

        // Registration open: now is within [start,end]
        Tasks.await(db.collection("events").document(EVENT_OPEN_YOGA).set(eventDoc(
                "Yoga for Beginners",
                "Stretch and breathe. Great yoga session.",
                "Fitness",
                now - 60_000L,
                now + 86_400_000L
        )));

        // Registration closed: end is in the past
        Tasks.await(db.collection("events").document(EVENT_CLOSED_YOGA).set(eventDoc(
                "Advanced Yoga (Closed)",
                "Yoga but registration is closed.",
                "Fitness",
                now - 86_400_000L,
                now - 60_000L
        )));

        // Another open event not matching "yoga" keyword
        Tasks.await(db.collection("events").document(EVENT_OPEN_MUSIC).set(eventDoc(
                "Live Music Night",
                "Concert with local bands.",
                "Music",
                now - 60_000L,
                now + 86_400_000L
        )));
    }

    @After
    public void cleanupFirestore() throws Exception {
        if (db == null) return;
        Tasks.await(db.collection("events").document(EVENT_OPEN_YOGA).delete());
        Tasks.await(db.collection("events").document(EVENT_CLOSED_YOGA).delete());
        Tasks.await(db.collection("events").document(EVENT_OPEN_MUSIC).delete());
    }

    @Test
    public void keywordSearch_filtersByTitleOrDescription() {
        ActivityScenario.launch(EntrantMainScreenActivity.class);

        // Wait for Firestore load + adapter population
        onView(isRoot()).perform(waitFor(2000));

        // Open filter dialog
        onView(withId(R.id.filterimage)).perform(click());

        // Set keyword = yoga
        onView(withId(R.id.input_filter_keyword)).perform(replaceText("yoga"), closeSoftKeyboard());
        onView(withId(R.id.btn_filter_apply)).perform(click());

        onView(isRoot()).perform(waitFor(1200));

        // Should contain Yoga events, should not show Music
        onView(withText("Yoga for Beginners")).check(matches(isDisplayed()));
        onView(withText("Advanced Yoga (Closed)")).check(matches(isDisplayed()));
        onView(withText("Live Music Night")).check(doesNotExist());
    }

    @Test
    public void availabilityFilter_registrationOpenOnly_hidesClosedRegistrationEvents() {
        ActivityScenario.launch(EntrantMainScreenActivity.class);
        onView(isRoot()).perform(waitFor(2000));

        onView(withId(R.id.filterimage)).perform(click());

        // Toggle registration-open-only
        onView(withId(R.id.switch_registration_open)).perform(click());
        onView(withId(R.id.btn_filter_apply)).perform(click());

        onView(isRoot()).perform(waitFor(1200));

        // Closed event should be filtered out; open ones remain
        onView(withText("Advanced Yoga (Closed)")).check(doesNotExist());
        onView(withText("Yoga for Beginners")).check(matches(isDisplayed()));
        onView(withText("Live Music Night")).check(matches(isDisplayed()));
    }

    @Test
    public void keywordPlusAvailability_combinedNarrowsResults() {
        ActivityScenario.launch(EntrantMainScreenActivity.class);
        onView(isRoot()).perform(waitFor(2000));

        onView(withId(R.id.filterimage)).perform(click());

        onView(withId(R.id.input_filter_keyword)).perform(replaceText("yoga"), closeSoftKeyboard());
        onView(withId(R.id.switch_registration_open)).perform(click());
        onView(withId(R.id.btn_filter_apply)).perform(click());

        onView(isRoot()).perform(waitFor(1200));

        // Only open yoga should remain
        onView(withText("Yoga for Beginners")).check(matches(isDisplayed()));
        onView(withText("Advanced Yoga (Closed)")).check(doesNotExist());
        onView(withText("Live Music Night")).check(doesNotExist());
    }

    private static Map<String, Object> eventDoc(String title,
                                                String description,
                                                String eventType,
                                                long regStartMillis,
                                                long regEndMillis) {
        long now = System.currentTimeMillis();
        Map<String, Object> m = new HashMap<>();
        m.put("title", title);
        m.put("description", description);
        m.put("eventType", eventType);

        // Required for "availability" filter (Event.isRegistrationOpen uses these)
        m.put("registrationStartMillis", regStartMillis);
        m.put("registrationEndMillis", regEndMillis);

        // These keep parser/UI happy even if not strictly required
        m.put("eventDateMillis", now + 7L * 86_400_000L);
        m.put("capacity", 10);
        m.put("waitingListLimit", 50);
        m.put("organizerId", "test_org");
        m.put("organizerName", "Test Organizer");
        m.put("geolocationRequired", false);
        m.put("isPrivate", false);
        return m;
    }

    /** Simple Espresso wait helper (use sparingly). */
    private static ViewAction waitFor(long millis) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isRoot(); }
            @Override public String getDescription() { return "wait for " + millis + "ms"; }
            @Override public void perform(UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }
}