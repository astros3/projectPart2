package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;

import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class AdminBrowseProfilesIntentTest {

    @Rule
    public ActivityScenarioRule<AdminBrowseProfilesActivity> scenario =
            new ActivityScenarioRule<>(AdminBrowseProfilesActivity.class);

    @Test
    public void testSearchFunctionality() {
        // Type a name into the search bar created in the UI
        onView(withId(R.id.search_input_bar)).perform(typeText("Person 1"));
        onView(withId(R.id.search_icon)).perform(click());

        // Check if the list filters correctly
        onData(anything())
                .inAdapterView(withId(R.id.profile_list_view))
                .atPosition(0)
                .onChildView(withId(R.id.profile_name))
                .check(matches(withText("Person 1")));
    }

    @Test
    public void testDeleteButtonExists() {
        // Verify the 'X' button is visible in the first row
        onData(anything())
                .inAdapterView(withId(R.id.profile_list_view))
                .atPosition(0)
                .onChildView(withId(R.id.delete_profile_button))
                .check(matches(isDisplayed()));
    }

}