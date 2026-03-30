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
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * tests for Admin notification log control screen
 * User stories: Huayu
 */
public class AdminNotificationLogControlScreenTest {
    private String testNotificationId;
    private String adminDeviceId;
    @Rule
    public ActivityScenarioRule<AdminNotificationLogControlScreenActivity> rule =new ActivityScenarioRule<>(AdminNotificationLogControlScreenActivity.class);

    /** reference eventdetailsintenttest*/
    @Before
    public void createTest() throws Exception{
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        adminDeviceId = DeviceIdManager.getDeviceId(ApplicationProvider.getApplicationContext());
        Map<String, Object> admin = new HashMap<>();
        admin.put("adminId", adminDeviceId);
        admin.put("firstName", "123123");
        admin.put("lastName", "1231");
        admin.put("email", "admin@123.com");
        admin.put("phoneNumber", "111234567");
        Tasks.await(db.collection("admins").document(adminDeviceId).set(admin));

        testNotificationId = "test1";
        Map<String, Object> notification = new HashMap<>();
        notification.put("title", "123");
        notification.put("message", "123123");
        notification.put("timestampMillis", System.currentTimeMillis());
        Tasks.await(db.collection("notificationStorageAdmin").document(testNotificationId).set(notification));
    }

    /** reference eventdetailsintenttest*/
    @After
    public void deletepreconditions() throws Exception{
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (testNotificationId != null) {
            Tasks.await(db.collection("notificationStorageAdmin").document(testNotificationId).delete());
        }
    }

    /** US:back button that will navigate back to admin main panel screen */
    @Test
    public void testBackButtonNavigatestoAdminMainScreen(){
        onView(withId(R.id.back_button_notification_log)).check(matches(isDisplayed()));
        onView(withId(R.id.back_button_notification_log)).perform(click());
    }
    /** US: notification title is shown */
    @Test
    public void everythingdisplayed(){
        onView(withText("NOTIFICATION LOG")).check(matches(isDisplayed()));
    }

    /** US: check the notification list is shown */
    @Test
    public void testnotificationListIsDisplayed(){
        onView(withId(R.id.notification_log_list)).check(matches(isDisplayed()));
    }

    /** US: test delete button exists */
    @Test
    public void deletebuttontest() {
        onData(anything())
                .inAdapterView(withId(R.id.notification_log_list))
                .atPosition(0)
                .onChildView(withId(R.id.admin_event_control_delete_button))
                .check(matches(isDisplayed()));
    }

}