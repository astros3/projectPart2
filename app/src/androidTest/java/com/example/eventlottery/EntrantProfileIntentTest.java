package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Purpose: UI/Intent test to verify that an entrant can successfully enter
 * their personal information and submit the form.
 * Pattern: Instrumented Testing (Espresso).
 * Outstanding Issues: Currently only verifies UI interaction;
 * needs Intent stubbing to verify transition to the next Activity.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EntrantProfileIntentTest {

    @Rule
    public ActivityScenarioRule<EntrantProfileActivity> activityRule =
            new ActivityScenarioRule<>(EntrantProfileActivity.class);

    /**
     * Tests the full registration flow for an Entrant.
     * Verified Requirement: "As an entrant, I want to provide my personal information
     * such as name, email and optional phone number".
     */
    @Test
    public void testEntrantRegistrationFlow() {
        // 1. Type First Name
        onView(withId(R.id.edit_first_name))
                .perform(typeText("Donald"), closeSoftKeyboard());

        // 2. Type Last Name
        onView(withId(R.id.edit_last_name))
                .perform(typeText("Duck"), closeSoftKeyboard());

        // 3. Type Phone Number (Optional)
        onView(withId(R.id.edit_phone))
                .perform(typeText("1234567890"), closeSoftKeyboard());

        // 4. Type Email
        onView(withId(R.id.edit_email))
                .perform(typeText("donald@gmail.com"), closeSoftKeyboard());

        // 5. Click the "Enter" button
        onView(withId(R.id.btn_enter))
                .perform(click());

        // Verification: Check if the fields are correctly populated before submission
        onView(withId(R.id.edit_first_name)).check(matches(withText("Donald")));
        onView(withId(R.id.edit_email)).check(matches(withText("donald@gmail.com")));
    }

    @Test
    public void testUpdateProfile() {
        // 1. Initial Save
        onView(withId(R.id.edit_first_name)).perform(replaceText("Original Name"), closeSoftKeyboard());
        onView(withId(R.id.btn_enter)).perform(click());

        // 2. Change the name (Update)
        onView(withId(R.id.edit_first_name)).perform(replaceText("Updated Name"), closeSoftKeyboard());
        onView(withId(R.id.btn_enter)).perform(click());

        // 3. Verify the field holds the new value
        onView(withId(R.id.edit_first_name)).check(matches(withText("Updated Name")));
    }
}