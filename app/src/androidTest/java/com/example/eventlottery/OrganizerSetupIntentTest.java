package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Intent tests for first-time organizer registration (setup).
 * Supports organizer flow required for US 02.01.01 (create event and QR code).
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerSetupIntentTest {

    @Rule
    public androidx.test.ext.junit.rules.ActivityScenarioRule<OrganizerSetupActivity> rule =
            new ActivityScenarioRule<>(
                    new Intent(ApplicationProvider.getApplicationContext(), OrganizerSetupActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

    @Test
    public void testOrganizerSetupScreenDisplaysForm() {
        onView(withId(R.id.edit_organizer_first_name)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_organizer_last_name)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_organizer_email)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_organizer_phone)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_save_organizer)).check(matches(isDisplayed()));
    }

    @Test
    public void testOrganizerCanEnterProfileAndSubmit() {
        onView(withId(R.id.edit_organizer_first_name)).perform(clearText(), typeText("Org"), closeSoftKeyboard());
        onView(withId(R.id.edit_organizer_last_name)).perform(clearText(), typeText("User"), closeSoftKeyboard());
        onView(withId(R.id.edit_organizer_email)).perform(clearText(), typeText("org@test.com"), closeSoftKeyboard());
        onView(withId(R.id.edit_organizer_phone)).perform(clearText(), typeText("7800000000"), closeSoftKeyboard());
        onView(withId(R.id.edit_organizer_first_name)).check(matches(withText("Org")));
        onView(withId(R.id.btn_save_organizer)).perform(click());
    }
}
