package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.RootMatchers.isDialog;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertFalse;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.rule.GrantPermissionRule;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Intent tests for entrant self-service profile deletion.
 * Story: As an entrant, I want to delete my profile if I no longer wish to use the app.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class EntrantProfileDeleteIntentTest {

    @Rule
    public GrantPermissionRule grantPostNotifications =
            GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS);

    private Context appContext;
    private FirebaseFirestore db;
    private String deviceId;

    @Before
    public void setUp() {
        appContext = ApplicationProvider.getApplicationContext();
        db = FirebaseFirestore.getInstance();
        deviceId = DeviceIdManager.getDeviceId(appContext);
    }

    @After
    public void tearDown() throws Exception {
        if (db == null || deviceId == null) {
            return;
        }
        try {
            Tasks.await(db.collection("users").document(deviceId).delete());
        } catch (Exception ignored) {
            // Best-effort cleanup after tests (doc may already be deleted by the app).
        }
    }

    private void launchEntrantProfile() {
        Intent intent = new Intent(appContext, EntrantProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ActivityScenario.launch(intent);
    }

    @Test
    public void deleteProfile_showsConfirmationDialog() {
        launchEntrantProfile();

        onView(withId(R.id.btn_delete_profile)).perform(click());

        onView(withText(R.string.delete_profile_title))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
        onView(withText(R.string.delete_profile_message))
                .inRoot(isDialog())
                .check(matches(isDisplayed()));
    }

    @Test
    public void deleteProfile_cancelDismissesDialog_profileStillVisible() {
        launchEntrantProfile();

        onView(withId(R.id.btn_delete_profile)).perform(click());
        onView(withText(android.R.string.cancel)).inRoot(isDialog()).perform(click());

        onView(withId(R.id.edit_profile_name)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_delete_profile)).check(matches(isDisplayed()));
    }

    @Test
    public void deleteProfile_confirm_removesFirestoreUserAndReturnsToWelcome() throws Exception {
        Entrant entrant = new Entrant(
                deviceId,
                "Delete Profile Test",
                "delete.profile.test@test.com",
                "",
                "entrant");
        Tasks.await(db.collection("users").document(deviceId).set(entrant));

        launchEntrantProfile();

        onView(withId(R.id.btn_delete_profile)).perform(click());
        onView(withText(android.R.string.yes)).inRoot(isDialog()).perform(click());

        onView(isRoot()).perform(waitFor(4000));

        onView(withId(R.id.userbutton)).check(matches(isDisplayed()));

        DocumentSnapshot snap = Tasks.await(db.collection("users").document(deviceId).get());
        assertFalse(snap.exists());
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
