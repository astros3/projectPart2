package com.example.eventlottery;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.Manifest;
import android.content.Intent;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Intent/UI regression tests for accessibility-related behavior.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AccessibilityIntentTest {

    @Rule
    public GrantPermissionRule grantNotificationPermission =
            GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS);

    private static Intent welcomeIntent() {
        Intent i = new Intent(ApplicationProvider.getApplicationContext(), WelcomePageActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return i;
    }

    @Test
    public void welcomePage_displaysRoleTilesWithVisibleLabels() {
        try (ActivityScenario<WelcomePageActivity> scenario =
                     ActivityScenario.launch(welcomeIntent())) {
            scenario.moveToState(Lifecycle.State.RESUMED);
            waitForUiSettleMs(1500);
            scenario.onActivity(activity -> {
                TextView rolePrompt = activity.findViewById(R.id.selectyourroletextdisplayonly);
                assertNotNull(rolePrompt);
                assertVisible(rolePrompt);
                CharSequence prompt = rolePrompt.getText();
                assertNotNull(prompt);
                assertTrue(prompt.toString().contains("Select your role"));

                View userTile = activity.findViewById(R.id.userbutton);
                assertNotNull(userTile);
                assertVisible(userTile);
                assertTrue(subtreeContainsText(userTile, "User"));

                View orgTile = activity.findViewById(R.id.organizerbutton);
                assertNotNull(orgTile);
                assertVisible(orgTile);
                assertTrue(subtreeContainsText(orgTile, "Organizer"));
            });
        }
    }

    @Test
    public void welcomePage_enlargeSmallTouchTargetsDoesNotCrash() {
        try (ActivityScenario<WelcomePageActivity> scenario =
                     ActivityScenario.launch(welcomeIntent())) {
            scenario.moveToState(Lifecycle.State.RESUMED);
            waitForUiSettleMs(1500);
            scenario.onActivity(activity -> {
                View root = activity.getWindow().getDecorView().getRootView();
                AccessibilityUtils.enlargeSmallTouchTargets(activity, root);
            });
        }
    }

    private static void assertVisible(View v) {
        assertTrue("View should be visible", v.getVisibility() == View.VISIBLE);
    }

    private static boolean subtreeContainsText(View view, String substring) {
        if (view instanceof TextView) {
            CharSequence t = ((TextView) view).getText();
            if (t != null && t.toString().contains(substring)) {
                return true;
            }
        }
        if (view instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) view;
            for (int i = 0; i < g.getChildCount(); i++) {
                if (subtreeContainsText(g.getChildAt(i), substring)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void waitForUiSettleMs(long millis) {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        SystemClock.sleep(millis);
    }
}
