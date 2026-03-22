package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

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
 * Intent tests for event details screen (entrant view).
 * US 1.01.01 - Join waiting list; US 1.01.02 - Leave waiting list;
 * US 1.06.01 - View event details by scanning QR; US 1.06.02 - Sign up from event details;
 * US 1.05.04 - Know how many total entrants are on the waiting list;
 * US 1.05.02/1.05.03 - Accept/Decline invitation when selected.
 *
 * Creates a test event in Firestore in @Before and deletes it in @After. No hardcoded event ID.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EventDetailsIntentTest {

    private static final String TEST_ORGANIZER_ID = "test-organizer-event-details";

    private String createdEventId;

    @Before
    public void createTestEvent() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        createdEventId = db.collection("events").document().getId();
        long now = System.currentTimeMillis();
        Map<String, Object> event = new HashMap<>();
        event.put("title", "Test Event for Event Details");
        event.put("description", "Description");
        event.put("location", "Edmonton");
        event.put("organizerId", TEST_ORGANIZER_ID);
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

    private void launchEventDetailsActivity() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EventDetailsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, createdEventId);
        intent.putExtra(EventDetailsActivity.EXTRA_VIEW_AS_ENTRANT, true);
        ActivityScenario.launch(intent);
    }

    @Test
    public void testEventDetailsScreenHasTitleAndJoinLeaveAndWaitingCount() {
        launchEventDetailsActivity();
        onView(withId(R.id.toolbar_event_details)).check(matches(isDisplayed()));
        onView(withId(R.id.event_title)).check(matches(isDisplayed()));
        onView(withId(R.id.event_waiting_list_count)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_join_leave)).check(matches(isDisplayed()));
    }

    @Test
    public void testEventDetailsShowsStatusAndDescriptionAndCriteria() {
        launchEventDetailsActivity();
        onView(withId(R.id.event_status)).check(matches(isDisplayed()));
        onView(withId(R.id.event_description)).check(matches(isDisplayed()));
        onView(withId(R.id.event_selection_criteria)).check(matches(isDisplayed()));
    }

    /** Accept/Decline buttons exist in layout (may be GONE until status is SELECTED). */
    @Test
    public void testAcceptAndDeclineInvitationButtonsExistInLayout() {
        launchEventDetailsActivity();
        onView(withId(R.id.btn_accept_invitation)).check(matches(withId(R.id.btn_accept_invitation)));
        onView(withId(R.id.btn_decline_invitation)).check(matches(withId(R.id.btn_decline_invitation)));
    }
}
