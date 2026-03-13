package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Intent tests for entrant history screen.
 * US 1.02.03 - As an entrant, I want to have a history of events I have registered for, whether I was selected or not.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EntrantHistoryIntentTest {

    @Rule
    public androidx.test.ext.junit.rules.ActivityScenarioRule<EntrantHistoryScreenActivity> rule =
            new ActivityScenarioRule<>(
                    new Intent(ApplicationProvider.getApplicationContext(), EntrantHistoryScreenActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

    @Test
    public void testHistoryScreenDisplaysEventList() {
        onView(withId(R.id.Events_history)).check(matches(isDisplayed()));
    }

    @Test
    public void testHistoryScreenHasNavigationBar() {
        onView(withId(R.id.navigation_home_button)).check(matches(isDisplayed()));
        onView(withId(R.id.navigation_history_button)).check(matches(isDisplayed()));
        onView(withId(R.id.navigation_profile_button)).check(matches(isDisplayed()));
    }
}
