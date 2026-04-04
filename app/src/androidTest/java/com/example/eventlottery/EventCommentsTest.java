package com.example.eventlottery;

import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

//When an entrant types a valid comment and submits it, the comment shows up on the event page.


@RunWith(AndroidJUnit4.class)
public class EventCommentsTest {

    @Test
    public void postComment_displaysInCommentList() {
        String testComment = "hello";

        Intent intent = new Intent(
                ApplicationProvider.getApplicationContext(),
                EventDetailsActivity.class
        );

        intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, "testEvent123");

        try (ActivityScenario<EventDetailsActivity> scenario = ActivityScenario.launch(intent)) {

            onView(withId(R.id.editComment))
                    .perform(typeText(testComment), closeSoftKeyboard());

            onView(withId(R.id.buttonPostComment))
                    .perform(click());

            onView(withText(testComment))
                    .check(matches(isDisplayed()));
        }
    }
}