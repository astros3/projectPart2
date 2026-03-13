package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
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
 * Intent tests for entrant profile (view and update).
 * US 1.02.02 - As an entrant I want to update information such as name, email and contact information on my profile.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EntrantProfileIntentTest {

    @Rule
    public androidx.test.ext.junit.rules.ActivityScenarioRule<EntrantProfileActivity> rule =
            new ActivityScenarioRule<>(
                    new Intent(ApplicationProvider.getApplicationContext(), EntrantProfileActivity.class)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

    @Test
    public void testProfileScreenDisplaysEditFieldsAndSaveButton() {
        onView(withId(R.id.edit_profile_name)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_profile_email)).check(matches(isDisplayed()));
        onView(withId(R.id.edit_profile_phone)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_save_changes)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_delete_profile)).check(matches(isDisplayed()));
    }

    @Test
    public void testEntrantCanUpdateProfileAndSave() {
        onView(withId(R.id.edit_profile_name)).perform(replaceText("Updated Name"), closeSoftKeyboard());
        onView(withId(R.id.edit_profile_email)).perform(replaceText("updated@test.com"), closeSoftKeyboard());
        onView(withId(R.id.edit_profile_name)).check(matches(withText("Updated Name")));
        onView(withId(R.id.btn_save_changes)).perform(click());
    }
}
