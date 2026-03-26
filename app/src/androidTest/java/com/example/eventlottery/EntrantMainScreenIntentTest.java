package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Intent tests for entrant main screen (event list).
 * US 1.01.03 - As an entrant, I want to be able to see a list of events that I can join the waiting list for.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EntrantMainScreenIntentTest {

    @Rule
    public androidx.test.ext.junit.rules.ActivityScenarioRule<EntrantMainScreenActivity> rule =
            new ActivityScenarioRule<>(
                    new Intent(ApplicationProvider.getApplicationContext(), EntrantMainScreenActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

    @Test
    public void testEventListAndSummaryAreDisplayed() {
        onView(withId(R.id.Events)).check(matches(isDisplayed()));
        onView(withId(R.id.total_number)).check(matches(isDisplayed()));
        onView(withId(R.id.pending_number)).check(matches(isDisplayed()));
        onView(withId(R.id.win_number)).check(matches(isDisplayed()));
        onView(withId(R.id.invitation_number)).check(matches(isDisplayed()));
    }

    @Test
    public void testNavigationBarIsDisplayed() {
        onView(withId(R.id.navigation_scan_button)).check(matches(isDisplayed()));
        onView(withId(R.id.navigation_map_button)).check(matches(isDisplayed()));
        onView(withId(R.id.navigation_history_button)).check(matches(isDisplayed()));
        onView(withId(R.id.navigation_profile_button)).check(matches(isDisplayed()));
    }

    @Test
    public void testBackToWelcomeIsDisplayed() {
        onView(withId(R.id.btn_back)).check(matches(isDisplayed()));
    }
}
