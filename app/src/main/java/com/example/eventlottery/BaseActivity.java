package com.example.eventlottery;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Common base for all Activities in the app.
 * When accessibility mode is enabled via the Welcome Page toggle, this base class applies:
 * - 1.3x font scaling (via attachBaseContext)
 * - A deuteranopia-friendly color matrix filter on the window (via onResume)
 */
public abstract class BaseActivity extends AppCompatActivity {

    /** Font scale multiplier applied when accessibility mode is enabled. */
    static final float ACCESSIBILITY_FONT_SCALE = 1.3f;

    /**
     * Deuteranopia-friendly 4x5 color matrix applied as a window hardware-layer paint
     * when accessibility mode is on. Rows are R, G, B, A output channels.
     */
    static final float[] ACCESSIBILITY_COLOR_MATRIX = {
        0.625f, 0.375f, 0f,    0f, 0f,
        0.7f,   0.3f,   0f,    0f, 0f,
        0f,     0.3f,   0.7f,  0f, 0f,
        0f,     0f,     0f,    1f, 0f
    };

    /** No-arg constructor required by the Android Activity lifecycle. */
    public BaseActivity() {}

    @Override
    protected void attachBaseContext(Context newBase) {
        if (AppPreferences.isAccessibilityMode(newBase)) {
            Configuration config = new Configuration(newBase.getResources().getConfiguration());
            config.fontScale = ACCESSIBILITY_FONT_SCALE;
            super.attachBaseContext(newBase.createConfigurationContext(config));
        } else {
            super.attachBaseContext(newBase);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        View decorView = getWindow().getDecorView();
        if (AppPreferences.isAccessibilityMode(this)) {
            Paint paint = new Paint();
            paint.setColorFilter(new ColorMatrixColorFilter(new ColorMatrix(ACCESSIBILITY_COLOR_MATRIX)));
            decorView.setLayerType(View.LAYER_TYPE_HARDWARE, paint);
        } else {
            decorView.setLayerType(View.LAYER_TYPE_NONE, null);
        }
    }
}
