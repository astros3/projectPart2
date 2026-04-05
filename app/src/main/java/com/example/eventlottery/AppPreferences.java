package com.example.eventlottery;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Stores and retrieves app-level accessibility preferences (color blind mode, large text).
 * Settings are persisted across app restarts using SharedPreferences.
 */
public final class AppPreferences {

    private static final String PREFS_NAME = "app_accessibility_prefs";
    private static final String KEY_ACCESSIBILITY = "accessibility_mode";

    private AppPreferences() {}

    /**
     * Returns whether accessibility mode (color blind + large text) is currently enabled.
     *
     * @param ctx any Context
     * @return true if accessibility mode is on
     */
    public static boolean isAccessibilityMode(Context ctx) {
        return ctx.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getBoolean(KEY_ACCESSIBILITY, false);
    }

    /**
     * Enables or disables accessibility mode.
     *
     * @param ctx     any Context
     * @param enabled true to enable, false to disable
     */
    public static void setAccessibilityMode(Context ctx, boolean enabled) {
        ctx.getApplicationContext()
                .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(KEY_ACCESSIBILITY, enabled)
                .apply();
    }
}
