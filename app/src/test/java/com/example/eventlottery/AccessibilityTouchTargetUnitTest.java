package com.example.eventlottery;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for accessibility mode constants defined in BaseActivity.
 * These do not require an Android device or emulator.
 */
public class AccessibilityTouchTargetUnitTest {

    // ── Font scale ────────────────────────────────────────────────────────────

    @Test
    public void accessibilityFontScale_isGreaterThanOne() {
        assertTrue("Font scale must enlarge text",
                BaseActivity.ACCESSIBILITY_FONT_SCALE > 1.0f);
    }

    @Test
    public void accessibilityFontScale_isExpected1_3() {
        assertEquals(1.3f, BaseActivity.ACCESSIBILITY_FONT_SCALE, 0.001f);
    }

    // ── Color matrix ──────────────────────────────────────────────────────────

    @Test
    public void accessibilityColorMatrix_hasTwentyElements() {
        assertEquals("4×5 color matrix must have 20 floats",
                20, BaseActivity.ACCESSIBILITY_COLOR_MATRIX.length);
    }

    @Test
    public void accessibilityColorMatrix_alphaChannelUnmodified() {
        // Row 3 (alpha): [0, 0, 0, 1, 0] — alpha must pass through unchanged
        float[] m = BaseActivity.ACCESSIBILITY_COLOR_MATRIX;
        assertEquals(0f, m[15], 0.001f); // A from R
        assertEquals(0f, m[16], 0.001f); // A from G
        assertEquals(0f, m[17], 0.001f); // A from B
        assertEquals(1f, m[18], 0.001f); // A from A (identity)
        assertEquals(0f, m[19], 0.001f); // A offset
    }

    @Test
    public void accessibilityColorMatrix_redRowSumsToOne() {
        // R_out = 0.625*R + 0.375*G — coefficients must sum to 1 to preserve luminance
        float[] m = BaseActivity.ACCESSIBILITY_COLOR_MATRIX;
        float sum = m[0] + m[1] + m[2]; // R, G, B coefficients of the red output row
        assertEquals(1.0f, sum, 0.01f);
    }

    @Test
    public void accessibilityColorMatrix_blueRowSumsToOne() {
        // B_out = 0*R + 0.3*G + 0.7*B
        float[] m = BaseActivity.ACCESSIBILITY_COLOR_MATRIX;
        float sum = m[10] + m[11] + m[12];
        assertEquals(1.0f, sum, 0.01f);
    }

    @Test
    public void accessibilityColorMatrix_noTranslationOffset() {
        // Column 4 of every RGB row should be 0 (no additive offset)
        float[] m = BaseActivity.ACCESSIBILITY_COLOR_MATRIX;
        assertEquals(0f, m[4],  0.001f); // R offset
        assertEquals(0f, m[9],  0.001f); // G offset
        assertEquals(0f, m[14], 0.001f); // B offset
    }
}
