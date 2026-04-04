package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.containsString;

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
import com.google.firebase.firestore.QuerySnapshot;

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
 * Intent/UI tests for organizers commenting on their events via .
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerEventCommentsIntentTest {

    private static final String EVENT_ID = "test_event_organizer_comments_intent";
    /** Distinct body so assertions do not collide with other data. */
    private static final String POST_BODY = "IntentTestOrgCommentUnique42";

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
        event.put("title", "Organizer Comments Intent Event");
        event.put("description", "Test description for comments");
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
        if (organizerDeviceId != null) {
            try {
                Tasks.await(db.collection("organizers").document(organizerDeviceId).delete());
            } catch (Exception ignored) { /* */ }
        }
    }

    private Intent organizerEventDetailsIntent() {
        Intent i = new Intent(appContext, EventDetailsActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, EVENT_ID);
        i.putExtra(EventDetailsActivity.EXTRA_VIEW_AS_ENTRANT, false);
        return i;
    }

    @Test
    public void organizerEventDetails_showsCommentInputPostButtonAndList() {
        try (ActivityScenario<EventDetailsActivity> scenario =
                     ActivityScenario.launch(organizerEventDetailsIntent())) {
            onView(isRoot()).perform(waitFor(4000));
            onView(withId(R.id.editComment)).perform(scrollTo());
            onView(withId(R.id.editComment)).check(matches(isDisplayed()));
            onView(withId(R.id.buttonPostComment)).perform(scrollTo());
            onView(withId(R.id.buttonPostComment)).check(matches(isDisplayed()));
            onView(withId(R.id.listComments)).perform(scrollTo());
            onView(withId(R.id.listComments)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void organizer_canPostComment_showsInListWithOrganizerLabel() {
        try (ActivityScenario<EventDetailsActivity> scenario =
                     ActivityScenario.launch(organizerEventDetailsIntent())) {
            onView(isRoot()).perform(waitFor(4000));
            onView(withId(R.id.editComment)).perform(scrollTo(), replaceText(POST_BODY), closeSoftKeyboard());
            onView(withId(R.id.buttonPostComment)).perform(scrollTo(), click());
            onView(isRoot()).perform(waitFor(5000));
            onView(withText(containsString(POST_BODY))).check(matches(isDisplayed()));
            onView(withText(containsString("(Organizer)"))).check(matches(isDisplayed()));
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
}
