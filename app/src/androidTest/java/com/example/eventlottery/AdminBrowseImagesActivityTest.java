package com.example.eventlottery;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


@RunWith(AndroidJUnit4.class)
public class AdminBrowseImagesActivityTest {

    @Rule
    public ActivityScenarioRule<AdminBrowseImagesActivity> activityRule =
            new ActivityScenarioRule<>(AdminBrowseImagesActivity.class);

    @Test
    public void testImagesScreenHeaderAndGrid() {
        // Verify the title "IMAGES" is visible
        onView(withText("IMAGES")).check(matches(isDisplayed()));

        // Verify the GridView is present
        onView(withId(R.id.admin_images_grid)).check(matches(isDisplayed()));
    }
}