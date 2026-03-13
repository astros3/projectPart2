package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
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
 * Intent tests for create/edit event screen (organizer).
 * US 02.01.01 - Create event and generate QR; US 02.01.04 - Set registration period;
 * US 02.03.01 - Optionally limit waiting list; US 02.04.01 - Upload event poster.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EventEditIntentTest {

    /** Create mode: no event ID. */
    @Rule
    public androidx.test.ext.junit.rules.ActivityScenarioRule<EventEditActivity> rule =
            new ActivityScenarioRule<>(
                    EventEditActivity.newIntent(ApplicationProvider.getApplicationContext(), null)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

    @Test
    public void testEventEditScreenHasNameDescriptionAndLocation() {
        onView(withId(R.id.input_event_name)).check(matches(isDisplayed()));
        onView(withId(R.id.input_event_description)).check(matches(isDisplayed()));
        onView(withId(R.id.input_event_location)).check(matches(isDisplayed()));
    }

    @Test
    public void testRegistrationPeriodFieldsAreDisplayed() {
        onView(withId(R.id.input_registration_start)).check(matches(isDisplayed()));
        onView(withId(R.id.input_registration_end)).check(matches(isDisplayed()));
    }

    @Test
    public void testWaitingListLimitAndGeolocationSwitchAreDisplayed() {
        onView(withId(R.id.input_waiting_list_limit)).check(matches(isDisplayed()));
        onView(withId(R.id.switch_geolocation)).check(matches(isDisplayed()));
    }

    @Test
    public void testEventPosterPlaceholderAndConfirmButtonAreDisplayed() {
        onView(withId(R.id.event_image_container)).check(matches(isDisplayed()));
        onView(withId(R.id.event_poster_placeholder)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_confirm)).check(matches(isDisplayed()));
    }

    @Test
    public void testOrganizerCanEnterEventNameAndDescription() {
        onView(withId(R.id.input_event_name)).perform(typeText("Test Event"), closeSoftKeyboard());
        onView(withId(R.id.input_event_description)).perform(typeText("Description for test"), closeSoftKeyboard());
        onView(withId(R.id.input_event_name)).check(matches(isDisplayed()));
    }
}
