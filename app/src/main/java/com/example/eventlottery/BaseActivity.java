package com.example.eventlottery;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Common base for all Activities in the app.
 *
 * Behaviour added here:
 *  - When the system has touch exploration enabled (TalkBack, Switch Access, etc.)
 *    every clickable/focusable view in the window is given a minimum touch target
 *    of 48 dp × 48 dp, matching the Material / WCAG recommendation.
 *    This runs in onResume so it re-evaluates whenever the user returns to the
 *    screen (e.g. after toggling accessibility in system settings).
 */
public abstract class BaseActivity extends AppCompatActivity {

    @Override
    protected void onResume() {
        super.onResume();
        applyAccessibilityEnhancements();
    }

    private void applyAccessibilityEnhancements() {
        View root = getWindow().getDecorView().getRootView();
        AccessibilityUtils.enlargeSmallTouchTargets(this, root);
    }
}
