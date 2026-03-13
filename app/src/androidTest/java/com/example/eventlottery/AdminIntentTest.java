package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
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
 * Intent tests for admin event control screen.
 * US 03.04.01 - As an administrator, I want to be able to browse events.
 * US 03.01.01 - As an administrator, I want to be able to remove events.
 * Note: Access is restricted to devices in Firestore "admins" collection. If not admin, activity finishes; tests verify layout when access is granted.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminIntentTest {

    @Rule
    public androidx.test.ext.junit.rules.ActivityScenarioRule<AdminEventControlScreenActivity> rule =
            new ActivityScenarioRule<>(
                    new Intent(ApplicationProvider.getApplicationContext(), AdminEventControlScreenActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

    @Test
    public void testAdminScreenHasBackButtonAndEventList() {
        onView(withId(R.id.back_button)).check(matches(isDisplayed()));
        onView(withId(R.id.Events_history)).check(matches(isDisplayed()));
    }

    @Test
    public void testAdminScreenHasSearchBarAndSearchButton() {
        onView(withId(R.id.admin_event_search_inputbar)).check(matches(isDisplayed()));
        onView(withId(R.id.search_button)).check(matches(isDisplayed()));
    }

    @Test
    public void testAdminCanUseSearchInput() {
        onView(withId(R.id.admin_event_search_inputbar)).perform(typeText("test"));
        onView(withId(R.id.search_button)).perform(click());
    }
}
