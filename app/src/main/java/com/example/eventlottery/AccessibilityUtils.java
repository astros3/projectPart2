package com.example.eventlottery;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;

/**
 * Utilities for adapting the UI when system-level touch exploration (TalkBack,
 * Switch Access, etc.) is active. All methods are safe to call at any time —
 * they are no-ops when accessibility is not enabled.
 *
 * <p>Layout guidelines for accessibility: use android:textSize in <b>sp</b> (scalable
 * pixels) for all user-visible text so font scale / system settings apply; reserve <b>dp</b> for
 * non-text sizing (icons, margins, minimum touch targets). Give every ImageView,
 * ImageButton, and meaningful graphic a non-empty android:contentDescription
 * (or a string resource reference in XML) so TalkBack can announce it.
 */
public final class AccessibilityUtils {

    private AccessibilityUtils() {}

    /** Returns true when the user has an accessibility service with touch exploration active. */
    public static boolean isTouchExplorationEnabled(Context context) {
        AccessibilityManager am =
                (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        return am != null && am.isTouchExplorationEnabled();
    }

    /**
     * Walks every view in the hierarchy rooted at the given root view and, for any
     * view that is clickable or focusable and smaller than 48 dp in either
     * dimension, raises its minimum size to 48 dp.
     *
     * Call this after the layout has been inflated (e.g. from onResume) so
     * that view dimensions are already known, but it also works before first
     * measure because setMinimumWidth/Height only sets a floor.
     */
    public static void enlargeSmallTouchTargets(Context context, View root) {
        if (!isTouchExplorationEnabled(context)) return;

        int minPx = dpToPx(context, 48);
        applyRecursively(root, minPx);
    }

    private static void applyRecursively(View view, int minPx) {
        if (view == null) return;

        if (view.isClickable() || view.isFocusable()) {
            ViewGroup.LayoutParams params = view.getLayoutParams();
            if (params != null) {
                boolean changed = false;
                // Only enlarge explicitly-set pixel sizes (positive values).
                // WRAP_CONTENT (-2) and MATCH_PARENT (-1) are left to the
                // setMinimumWidth/Height fallback below.
                if (params.width > 0 && params.width < minPx) {
                    params.width = minPx;
                    changed = true;
                }
                if (params.height > 0 && params.height < minPx) {
                    params.height = minPx;
                    changed = true;
                }
                if (changed) {
                    view.setLayoutParams(params);
                }
            }
            // Fallback for wrap_content views: set a floor the view reports to
            // its parent during measurement.
            if (view.getMinimumWidth() < minPx) view.setMinimumWidth(minPx);
            if (view.getMinimumHeight() < minPx) view.setMinimumHeight(minPx);
        }

        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applyRecursively(group.getChildAt(i), minPx);
            }
        }
    }

    private static int dpToPx(Context context, int dp) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return Math.round(dp * dm.density);
    }
}
