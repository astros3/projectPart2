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
 * Intent tests for first-time entrant registration (setup).
 * US 1.02.01 - As an entrant, I want to provide my personal information such as name, email and optional phone number.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EntrantSetupIntentTest {

    @Rule
    public androidx.test.ext.junit.rules.ActivityScenarioRule<EntrantSetupActivity> rule =
            new ActivityScenarioRule<>(
                    new Intent(ApplicationProvider.getApplicationContext(), EntrantSetupActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

    @Test
    public void testEntrantSetupScreenDisplaysForm() {
        onView(withId(R.id.edit_first_name)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_last_name)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_phone)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_email)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_enter)).check(matches(isDisplayed()));
    }

    @Test
    public void testEntrantCanEnterPersonalInfoAndSubmit() {
        onView(withId(R.id.edit_first_name)).perform(clearText(), typeText("Jane"), closeSoftKeyboard());
        onView(withId(R.id.edit_last_name)).perform(clearText(), typeText("Doe"), closeSoftKeyboard());
        onView(withId(R.id.edit_phone)).perform(clearText(), typeText("7801234567"), closeSoftKeyboard());
        onView(withId(R.id.edit_email)).perform(clearText(), typeText("jane.doe@test.com"), closeSoftKeyboard());

        onView(withId(R.id.edit_first_name)).check(matches(withText("Jane")));
        onView(withId(R.id.edit_email)).check(matches(withText("jane.doe@test.com")));

        onView(withId(R.id.btn_enter)).perform(click());
        // Submission may navigate to EntrantMainScreenActivity (Firestore success) or show error.
    }
}
