package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

/**
 * Intent tests for admin event control screen.
 * US 03.04.01 - As an administrator, I want to be able to browse events.
 * US 03.01.01 - As an administrator, I want to be able to remove events.
 *
 * Grants the test device admin access in Firestore before launching the activity, and revokes it in @After.
 * No need to manually add the device to the admins collection for tests to pass.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AdminIntentTest {

    private String deviceId;

    @Before
    public void grantTestDeviceAdminAccess() throws Exception {
        Context ctx = ApplicationProvider.getApplicationContext();
        deviceId = DeviceIdManager.getDeviceId(ctx);
        Map<String, Object> adminDoc = new HashMap<>();
        adminDoc.put("test", true);
        Tasks.await(
                FirebaseFirestore.getInstance()
                        .collection("admins")
                        .document(deviceId)
                        .set(adminDoc)
        );
    }

    @After
    public void revokeTestDeviceAdminAccess() throws Exception {
        if (deviceId != null) {
            Tasks.await(
                    FirebaseFirestore.getInstance()
                            .collection("admins")
                            .document(deviceId)
                            .delete()
            );
        }
    }

    private void launchAdminActivity() {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), AdminEventControlScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ActivityScenario.launch(intent);
    }

    @Test
    public void testAdminScreenHasBackButtonAndEventList() {
        launchAdminActivity();
        onView(withId(R.id.back_button)).check(matches(isDisplayed()));
        onView(withId(R.id.Events_history)).check(matches(isDisplayed()));
    }

    @Test
    public void testAdminScreenHasSearchBarAndSearchButton() {
        launchAdminActivity();
        onView(withId(R.id.admin_event_search_inputbar)).check(matches(isDisplayed()));
        onView(withId(R.id.search_button)).check(matches(isDisplayed()));
    }

    @Test
    public void testAdminCanUseSearchInput() {
        launchAdminActivity();
        onView(withId(R.id.admin_event_search_inputbar)).perform(typeText("test"));
        onView(withId(R.id.search_button)).perform(click());
    }
}
