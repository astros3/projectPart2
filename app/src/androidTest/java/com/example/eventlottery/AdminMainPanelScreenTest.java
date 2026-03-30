package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.anything;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * tests for Admin main panel screen
 */
public class AdminMainPanelScreenTest {
    private String testNotificationId;
    private static String adminDeviceId;
    @Rule
    public ActivityScenarioRule<AdminMainScreenActivity> rule =new ActivityScenarioRule<>(AdminMainScreenActivity.class);

    /** reference eventdetailsintenttest*/
    @BeforeClass
    public static void createTest() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        adminDeviceId = DeviceIdManager.getDeviceId(ApplicationProvider.getApplicationContext());
        Map<String, Object> admin = new HashMap<>();
        admin.put("adminId", adminDeviceId);
        admin.put("firstName", "123123");
        admin.put("lastName", "1231");
        admin.put("email", "admin@123.com");
        admin.put("phoneNumber", "111234567");
        Tasks.await(db.collection("admins").document(adminDeviceId).set(admin));
    }
    /** reference eventdetailsintenttest*/
    @AfterClass
    public static void deletepreconditions() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (adminDeviceId != null) {
            Tasks.await(db.collection("admins").document(adminDeviceId).delete());
        }
    }

    /** US: back button is shown and clickable*/
    @Test
    public void testbackbutton() {
        onView(withId(R.id.back_button)).check(matches(isDisplayed()));
        onView(withId(R.id.back_button)).perform(click());
    }
    /** US: button is shown and clickable*/
    @Test
    public void adminreviewbuttontest() {
        onView(withText("REVIEW EVENTS")).check(matches(isDisplayed()));
        onView(withId(R.id.admin_review_events_button)).check(matches(isDisplayed()));

        onView(withId(R.id.admin_review_events_button)).perform(click());
    }
    /** US: button is shown and clickable*/
    @Test
    public void adminprofilereviewbuttontest() {
        onView(withText("REVIEW PROFILES")).check(matches(isDisplayed()));
        onView(withId(R.id.admin_review_profile_button)).check(matches(isDisplayed()));

        onView(withId(R.id.admin_review_profile_button)).perform(click());
    }
    /** US: button is shown and clickable*/
    @Test
    public void adminreviewiamgebuttontest() {
        onView(withText("REVIEW IMAGES")).check(matches(isDisplayed()));
        onView(withId(R.id.admin_review_image_button)).check(matches(isDisplayed()));

        onView(withId(R.id.admin_review_image_button)).perform(click());
    }
    /** US: button is shown and clickable*/
    @Test
    public void adminreveiwnotificationbuttontest() {
        onView(withText("REVIEW NOTIFICATION LOG")).check(matches(isDisplayed()));
        onView(withId(R.id.admin_review_notification_log_button)).check(matches(isDisplayed()));

        onView(withId(R.id.admin_review_notification_log_button)).perform(click());
    }
    /** US: button is shown and clickable*/
    @Test
    public void adminownerprofilebuttontest() {
        onView(withText("ADMIN PROFILE")).check(matches(isDisplayed()));
        onView(withId(R.id.admin_profile_button)).check(matches(isDisplayed()));

        onView(withId(R.id.admin_profile_button)).perform(click());
    }


    /** US: all  buttons are shown*/
    @Test
    public void testAllMainButtonsAreDisplayed() {
        onView(withId(R.id.admin_review_events_button)).check(matches(isDisplayed()));
        onView(withId(R.id.admin_review_profile_button)).check(matches(isDisplayed()));
        onView(withId(R.id.admin_review_image_button)).check(matches(isDisplayed()));
        onView(withId(R.id.admin_review_notification_log_button)).check(matches(isDisplayed()));
        onView(withId(R.id.admin_profile_button)).check(matches(isDisplayed()));
    }




}
