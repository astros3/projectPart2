package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.Manifest;
import android.content.Context;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

/**
 * US 03.09.01: Admin can also act as organizer and entrant.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class WelcomePageAdminMultiRoleIntentTest {

    private String deviceId;

    @Rule
    public GrantPermissionRule grantPostNotifications =
            GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS);

    @Before
    public void setUp() throws Exception {
        Context context = ApplicationProvider.getApplicationContext();
        deviceId = DeviceIdManager.getDeviceId(context);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> adminDoc = new HashMap<>();
        adminDoc.put("name", "Admin Multi Role Tester");
        Tasks.await(db.collection("admins").document(deviceId).set(adminDoc));
    }

    @After
    public void tearDown() throws Exception {
        if (deviceId != null) {
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            Tasks.await(db.collection("admins").document(deviceId).delete());
            Tasks.await(db.collection("users").document(deviceId).delete());
            Tasks.await(db.collection("organizers").document(deviceId).delete());
        }
    }

    @Test
    public void adminSeesAdminButton_andStillSeesEntrantAndOrganizerButtons() {
        try (ActivityScenario<WelcomePageActivity> ignored = ActivityScenario.launch(WelcomePageActivity.class)) {
            onView(withId(R.id.adminbutton)).check(matches(isDisplayed()));
            onView(withId(R.id.userbutton)).check(matches(isDisplayed()));
            onView(withId(R.id.organizerbutton)).check(matches(isDisplayed()));
        }
    }
}
