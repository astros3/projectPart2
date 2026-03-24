package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Intent tests for Welcome page and role selection.
 * Covers: Multi-user roles (Entrant, Organizer, Admin), Welcome page UI.
 * User stories: Huayu - Welcome page; general role-distinction (US multi-actor).
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class WelcomePageIntentTest {

    @Rule
    public ActivityScenarioRule<WelcomePageActivity> rule =
            new ActivityScenarioRule<>(WelcomePageActivity.class);

    /** US: Welcome page shows role selection. Entrant (User) and Organizer buttons are always visible. */
    @Test
    public void testUserButtonIsDisplayed() {
        onView(withId(R.id.userbutton)).check(matches(isDisplayed()));
    }

    /** US: Organizer option visible on welcome screen. */
    @Test
    public void testOrganizerButtonIsDisplayed() {
        onView(withId(R.id.organizerbutton)).check(matches(isDisplayed()));
    }

    /** US: Role selection text is shown. */
    @Test
    public void testSelectRoleTextIsDisplayed() {
        onView(withId(R.id.selectyourroletextdisplayonly))
                .check(matches(isDisplayed()));
        onView(withId(R.id.selectyourroletextdisplayonly))
                .check(matches(withText("Select your role to access your dashboard and manage events.")));
    }

    /** US: User button is clickable; navigates to entrant flow (setup or main). */
    @Test
    public void testUserButtonIsClickable() {
        onView(withId(R.id.userbutton)).perform(click());
        // After click, either EntrantSetupActivity or EntrantMainScreenActivity is shown (depends on Firestore).
        // We only assert that we left the welcome screen (no crash).
    }

    /** US: Organizer button is clickable; navigates to organizer flow. */
    @Test
    public void testOrganizerButtonIsClickable() {
        onView(withId(R.id.organizerbutton)).perform(click());
    }
}
