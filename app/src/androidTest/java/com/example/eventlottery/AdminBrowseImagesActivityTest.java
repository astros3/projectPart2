package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onData;
import androidx.test.runner.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static androidx.test.espresso.action.ViewActions.click;

import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;

import org.hamcrest.Matcher;

import java.util.HashMap;
import java.util.Map;


@RunWith(AndroidJUnit4.class)
public class AdminBrowseImagesActivityTest {

    private final String testEventId = "admin_images_intent_test_event";

    @Before
    public void seedImageEvent() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", testEventId);
        event.put("title", "Image Intent Test Event");
        event.put("posterUri", "https://example.com/poster.jpg");
        event.put("organizerId", "test_org");
        Tasks.await(db.collection("events").document(testEventId).set(event));
    }

    @After
    public void cleanImageEvent() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Tasks.await(db.collection("events").document(testEventId).delete());
    }

    @Test
    public void testImagesScreenHeaderAndGrid() {
        try (ActivityScenario<AdminBrowseImagesActivity> ignored = ActivityScenario.launch(AdminBrowseImagesActivity.class)) {
            onView(withText("IMAGES")).check(matches(isDisplayed()));
            onView(withId(R.id.admin_images_grid)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void testAdminCanSeeDeleteButtonForUploadedImage() {
        try (ActivityScenario<AdminBrowseImagesActivity> ignored = ActivityScenario.launch(AdminBrowseImagesActivity.class)) {
            onView(isRoot()).perform(waitFor(1200));
            onData(CoreMatchers.anything())
                    .inAdapterView(withId(R.id.admin_images_grid))
                    .atPosition(0)
                    .onChildView(withId(R.id.delete_image_button))
                    .check(matches(isDisplayed()));
        }
    }

    @Test
    public void testAdminCanRemoveUploadedImageFromGrid() {
        try (ActivityScenario<AdminBrowseImagesActivity> ignored = ActivityScenario.launch(AdminBrowseImagesActivity.class)) {
            onView(isRoot()).perform(waitFor(1200));
            onData(CoreMatchers.anything())
                    .inAdapterView(withId(R.id.admin_images_grid))
                    .atPosition(0)
                    .onChildView(withId(R.id.delete_image_button))
                    .perform(click());
            onView(isRoot()).perform(waitFor(1200));
            onData(CoreMatchers.anything())
                    .inAdapterView(withId(R.id.admin_images_grid))
                    .atPosition(0)
                    .check(doesNotExist());
        }
    }

    private static ViewAction waitFor(long millis) {
        return new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isRoot();
            }

            @Override
            public String getDescription() {
                return "wait for " + millis + "ms";
            }

            @Override
            public void perform(UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }
}