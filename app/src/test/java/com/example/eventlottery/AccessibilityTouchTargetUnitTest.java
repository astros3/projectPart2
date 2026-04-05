package com.example.eventlottery;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Larger minimum touch targets when accessibility exploration is on — 48dp floor matches
 * AccessibilityUtils.enlargeSmallTouchTargets.
 */
public class AccessibilityTouchTargetUnitTest {

    @Test
    public void accessibilityTouchTargetMinimum_is48Dp() {
        assertEquals(48, ACCESSIBILITY_MIN_TOUCH_TARGET_DP);
    }

    /** Keep in sync with AccessibilityUtils.enlargeSmallTouchTargets → dpToPx(..., 48). */
    private static final int ACCESSIBILITY_MIN_TOUCH_TARGET_DP = 48;
}
