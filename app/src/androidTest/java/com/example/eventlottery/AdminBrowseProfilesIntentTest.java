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

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class AdminBrowseProfilesIntentTest {

    private String testUserId;
    private String adminDeviceId;
    private ActivityScenario<AdminBrowseProfilesActivity> scenario;

    @Before
    public void setUp() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        adminDeviceId = DeviceIdManager.getDeviceId(ApplicationProvider.getApplicationContext());
        Map<String, Object> admin = new HashMap<>();
        admin.put("adminId", adminDeviceId);
        admin.put("firstName", "Test");
        admin.put("lastName", "Admin");
        admin.put("email", "admin@test.com");
        admin.put("phoneNumber", "1234567890");
        Tasks.await(db.collection("admins").document(adminDeviceId).set(admin));

        testUserId = "browseProfilesTestUser";
        Map<String, Object> user = new HashMap<>();
        user.put("deviceID", testUserId);
        user.put("fullName", "Person 1");
        user.put("email", "person1@test.com");
        user.put("phoneNumber", "9876543210");
        user.put("role", "Entrant");
        Tasks.await(db.collection("users").document(testUserId).set(user));

        scenario = ActivityScenario.launch(AdminBrowseProfilesActivity.class);
        Thread.sleep(3000);
    }

    @After
    public void tearDown() throws Exception {
        if (scenario != null) {
            scenario.close();
        }
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (testUserId != null) {
            Tasks.await(db.collection("users").document(testUserId).delete());
        }
        if (adminDeviceId != null) {
            Tasks.await(db.collection("admins").document(adminDeviceId).delete());
        }
    }

    @Test
    public void testSearchFunctionality() throws InterruptedException {
        onView(withId(R.id.search_input_bar)).perform(typeText("Person 1"));
        onView(withId(R.id.search_icon)).perform(click());
        Thread.sleep(2000);

        onData(anything())
                .inAdapterView(withId(R.id.profile_list_view))
                .atPosition(0)
                .onChildView(withId(R.id.profile_name))
                .check(matches(withText("Person 1")));
    }

    @Test
    public void testDeleteButtonExists() throws InterruptedException {
        Thread.sleep(2000);
        onData(anything())
                .inAdapterView(withId(R.id.profile_list_view))
                .atPosition(0)
                .onChildView(withId(R.id.delete_profile_button))
                .check(matches(isDisplayed()));
    }
}
