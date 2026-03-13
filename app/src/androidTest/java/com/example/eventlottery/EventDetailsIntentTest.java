package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Intent tests for event details screen (entrant view).
 * US 1.01.01 - Join waiting list; US 1.01.02 - Leave waiting list;
 * US 1.06.01 - View event details by scanning QR; US 1.06.02 - Sign up from event details;
 * US 1.05.04 - Know how many total entrants are on the waiting list;
 * US 1.05.02/1.05.03 - Accept/Decline invitation when selected.
 *
 * Uses a real event ID from Firestore so the activity loads and stays open for assertions.
 * Set TEST_EVENT_ID to an existing event document ID in your Firestore "events" collection.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EventDetailsIntentTest {

    /** Replace with a real event document ID from your Firestore "events" collection. */
    private static final String TEST_EVENT_ID = "REPLACE_WITH_YOUR_EVENT_ID";

    private static Intent eventDetailsIntent(String eventId) {
        Intent i = new Intent(ApplicationProvider.getApplicationContext(), EventDetailsActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        i.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, eventId);
        i.putExtra(EventDetailsActivity.EXTRA_VIEW_AS_ENTRANT, true);
        return i;
    }

    @Rule
    public androidx.test.ext.junit.rules.ActivityScenarioRule<EventDetailsActivity> rule =
            new ActivityScenarioRule<>(eventDetailsIntent(TEST_EVENT_ID));

    @Test
    public void testEventDetailsScreenHasTitleAndJoinLeaveAndWaitingCount() {
        onView(withId(R.id.toolbar_event_details)).check(matches(isDisplayed()));
        onView(withId(R.id.event_title)).check(matches(isDisplayed()));
        onView(withId(R.id.event_waiting_list_count)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_join_leave)).check(matches(isDisplayed()));
    }

    @Test
    public void testEventDetailsShowsStatusAndDescriptionAndCriteria() {
        onView(withId(R.id.event_status)).check(matches(isDisplayed()));
        onView(withId(R.id.event_description)).check(matches(isDisplayed()));
        onView(withId(R.id.event_selection_criteria)).check(matches(isDisplayed()));
    }

    /** Accept/Decline buttons exist in layout (may be GONE until status is SELECTED). */
    @Test
    public void testAcceptAndDeclineInvitationButtonsExistInLayout() {
        onView(withId(R.id.btn_accept_invitation)).check(matches(withId(R.id.btn_accept_invitation)));
        onView(withId(R.id.btn_decline_invitation)).check(matches(withId(R.id.btn_decline_invitation)));
    }
}
