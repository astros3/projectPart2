package com.example.eventlottery;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.Manifest;
import android.content.Intent;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.lifecycle.Lifecycle;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.rule.GrantPermissionRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Intent/UI tests for accessibility mode:
 * - Welcome page toggle visibility and persistence
 * - Font scale applied when mode is on
 * - Color matrix hardware layer applied to window decor view
 * - UI role tiles still readable in both modes
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class AccessibilityIntentTest {

    @Rule
    public GrantPermissionRule grantNotificationPermission =
            GrantPermissionRule.grant(Manifest.permission.POST_NOTIFICATIONS);

    @Before
    public void resetPreference() {
        AppPreferences.setAccessibilityMode(
                ApplicationProvider.getApplicationContext(), false);
    }

    @After
    public void cleanUpPreference() {
        AppPreferences.setAccessibilityMode(
                ApplicationProvider.getApplicationContext(), false);
    }

    // ── Switch visibility ─────────────────────────────────────────────────────

    @Test
    public void welcomePage_accessibilitySwitchIsVisible() {
        try (ActivityScenario<WelcomePageActivity> s = ActivityScenario.launch(welcomeIntent())) {
            s.moveToState(Lifecycle.State.RESUMED);
            settle();
            s.onActivity(activity -> {
                View switchView = activity.findViewById(R.id.switchAccessibility);
                assertNotNull("Accessibility switch must exist", switchView);
                assertEquals(View.VISIBLE, switchView.getVisibility());
            });
        }
    }

    @Test
    public void welcomePage_accessibilitySwitchLabel_isVisible() {
        try (ActivityScenario<WelcomePageActivity> s = ActivityScenario.launch(welcomeIntent())) {
            s.moveToState(Lifecycle.State.RESUMED);
            settle();
            s.onActivity(activity -> {
                View row = activity.findViewById(R.id.accessibilityToggleRow);
                assertNotNull(row);
                assertTrue("Toggle row must contain 'Accessibility'",
                        subtreeContainsText(row, "Accessibility"));
            });
        }
    }

    // ── Switch state reflects saved preference ────────────────────────────────

    @Test
    public void welcomePage_switchUnchecked_whenModeIsOff() {
        AppPreferences.setAccessibilityMode(
                ApplicationProvider.getApplicationContext(), false);
        try (ActivityScenario<WelcomePageActivity> s = ActivityScenario.launch(welcomeIntent())) {
            s.moveToState(Lifecycle.State.RESUMED);
            settle();
            s.onActivity(activity -> {
                SwitchCompat sw = activity.findViewById(R.id.switchAccessibility);
                assertNotNull(sw);
                assertTrue("Switch should be OFF when preference is false", !sw.isChecked());
            });
        }
    }

    @Test
    public void welcomePage_switchChecked_whenModeIsOn() {
        AppPreferences.setAccessibilityMode(
                ApplicationProvider.getApplicationContext(), true);
        try (ActivityScenario<WelcomePageActivity> s = ActivityScenario.launch(welcomeIntent())) {
            s.moveToState(Lifecycle.State.RESUMED);
            settle();
            s.onActivity(activity -> {
                SwitchCompat sw = activity.findViewById(R.id.switchAccessibility);
                assertNotNull(sw);
                assertTrue("Switch should be ON when preference is true", sw.isChecked());
            });
        }
    }

    // ── Color filter layer applied ────────────────────────────────────────────

    @Test
    public void welcomePage_accessibilityOff_noHardwareLayer() {
        AppPreferences.setAccessibilityMode(
                ApplicationProvider.getApplicationContext(), false);
        try (ActivityScenario<WelcomePageActivity> s = ActivityScenario.launch(welcomeIntent())) {
            s.moveToState(Lifecycle.State.RESUMED);
            settle();
            s.onActivity(activity -> {
                View decor = activity.getWindow().getDecorView();
                assertEquals("No hardware layer when accessibility is off",
                        View.LAYER_TYPE_NONE, decor.getLayerType());
            });
        }
    }

    @Test
    public void welcomePage_accessibilityOn_hardwareLayerApplied() {
        AppPreferences.setAccessibilityMode(
                ApplicationProvider.getApplicationContext(), true);
        try (ActivityScenario<WelcomePageActivity> s = ActivityScenario.launch(welcomeIntent())) {
            s.moveToState(Lifecycle.State.RESUMED);
            settle();
            s.onActivity(activity -> {
                View decor = activity.getWindow().getDecorView();
                assertEquals("Hardware layer must be set when accessibility is on",
                        View.LAYER_TYPE_HARDWARE, decor.getLayerType());
            });
        }
    }

    // ── Font scale ────────────────────────────────────────────────────────────

    @Test
    public void welcomePage_accessibilityOff_defaultFontScale() {
        AppPreferences.setAccessibilityMode(
                ApplicationProvider.getApplicationContext(), false);
        try (ActivityScenario<WelcomePageActivity> s = ActivityScenario.launch(welcomeIntent())) {
            s.moveToState(Lifecycle.State.RESUMED);
            settle();
            s.onActivity(activity -> {
                float scale = activity.getResources().getConfiguration().fontScale;
                assertTrue("Font scale should be near 1.0 when accessibility is off",
                        scale < BaseActivity.ACCESSIBILITY_FONT_SCALE);
            });
        }
    }

    @Test
    public void welcomePage_accessibilityOn_largerFontScale() {
        AppPreferences.setAccessibilityMode(
                ApplicationProvider.getApplicationContext(), true);
        try (ActivityScenario<WelcomePageActivity> s = ActivityScenario.launch(welcomeIntent())) {
            s.moveToState(Lifecycle.State.RESUMED);
            settle();
            s.onActivity(activity -> {
                float scale = activity.getResources().getConfiguration().fontScale;
                assertEquals("Font scale should equal ACCESSIBILITY_FONT_SCALE",
                        BaseActivity.ACCESSIBILITY_FONT_SCALE, scale, 0.05f);
            });
        }
    }

    // ── Role tiles remain readable in both modes ──────────────────────────────

    @Test
    public void welcomePage_accessibilityOff_roleTilesVisible() {
        AppPreferences.setAccessibilityMode(
                ApplicationProvider.getApplicationContext(), false);
        assertRoleTilesVisible();
    }

    @Test
    public void welcomePage_accessibilityOn_roleTilesStillVisible() {
        AppPreferences.setAccessibilityMode(
                ApplicationProvider.getApplicationContext(), true);
        assertRoleTilesVisible();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private void assertRoleTilesVisible() {
        try (ActivityScenario<WelcomePageActivity> s = ActivityScenario.launch(welcomeIntent())) {
            s.moveToState(Lifecycle.State.RESUMED);
            settle();
            s.onActivity(activity -> {
                View userTile = activity.findViewById(R.id.userbutton);
                assertNotNull(userTile);
                assertEquals(View.VISIBLE, userTile.getVisibility());
                assertTrue(subtreeContainsText(userTile, "User"));

                View orgTile = activity.findViewById(R.id.organizerbutton);
                assertNotNull(orgTile);
                assertEquals(View.VISIBLE, orgTile.getVisibility());
                assertTrue(subtreeContainsText(orgTile, "Organizer"));
            });
        }
    }

    private static Intent welcomeIntent() {
        Intent i = new Intent(ApplicationProvider.getApplicationContext(),
                WelcomePageActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return i;
    }

    private static boolean subtreeContainsText(View view, String substring) {
        if (view instanceof TextView) {
            CharSequence t = ((TextView) view).getText();
            if (t != null && t.toString().contains(substring)) return true;
        }
        if (view instanceof ViewGroup) {
            ViewGroup g = (ViewGroup) view;
            for (int i = 0; i < g.getChildCount(); i++) {
                if (subtreeContainsText(g.getChildAt(i), substring)) return true;
            }
        }
        return false;
    }

    private static void settle() {
        InstrumentationRegistry.getInstrumentation().waitForIdleSync();
        SystemClock.sleep(1500);
    }
}
