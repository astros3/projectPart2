package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * When an entrant types a valid comment and submits it, the comment shows up on the event page.
 *
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EventCommentsTest {

    private static final String EVENT_ID = "test_event_event_comments";
    private static final String TEST_ORGANIZER_ID = "test-organizer-event-comments";

    private FirebaseFirestore db;

    @Before
    public void setUp() throws Exception {
        db = FirebaseFirestore.getInstance();
        long now = System.currentTimeMillis();
        Map<String, Object> event = new HashMap<>();
        event.put("title", "Event Comments Test");
        event.put("description", "Description");
        event.put("location", "Edmonton");
        event.put("organizerId", TEST_ORGANIZER_ID);
        event.put("organizerName", "Test Organizer");
        event.put("capacity", 10);
        event.put("waitingListLimit", 10);
        event.put("registrationStartMillis", now - TimeUnit.DAYS.toMillis(1));
        event.put("registrationEndMillis", now + TimeUnit.DAYS.toMillis(7));
        event.put("eventDateMillis", now + TimeUnit.DAYS.toMillis(14));
        event.put("geolocationRequired", false);
        event.put("isPrivate", false);
        event.put("price", 0.0);
        event.put("selectionCriteria", new ArrayList<String>());
        Tasks.await(db.collection("events").document(EVENT_ID).set(event));
    }

    @After
    public void tearDown() throws Exception {
        if (db == null) {
            return;
        }
        try {
            QuerySnapshot snap = Tasks.await(
                    db.collection("events").document(EVENT_ID).collection("comments").get());
            for (DocumentSnapshot d : snap.getDocuments()) {
                try {
                    Tasks.await(d.getReference().delete());
                } catch (Exception ignored) { /* */ }
            }
        } catch (Exception ignored) { /* */ }
        try {
            Tasks.await(db.collection("events").document(EVENT_ID).delete());
        } catch (Exception ignored) { /* */ }
    }

    @Test
    public void postComment_displaysInCommentList() throws Exception {
        String testComment = "hello_event_comment_test";

        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                EventDetailsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, EVENT_ID);
        intent.putExtra(EventDetailsActivity.EXTRA_VIEW_AS_ENTRANT, true);

        try (ActivityScenario<EventDetailsActivity> scenario = ActivityScenario.launch(intent)) {
            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            Thread.sleep(2500);

            // Comment controls are inside a ScrollView below the fold — must scroll before typing.
            onView(withId(R.id.editComment))
                    .perform(scrollTo(), click(), typeText(testComment), closeSoftKeyboard());

            onView(withId(R.id.buttonPostComment))
                    .perform(scrollTo(), click());

            InstrumentationRegistry.getInstrumentation().waitForIdleSync();
            Thread.sleep(3000);

            onView(withText(testComment))
                    .check(matches(isDisplayed()));
        }
    }
}
