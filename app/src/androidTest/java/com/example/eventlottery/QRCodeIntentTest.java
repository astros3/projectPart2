package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Intent tests for QR code screen (organizer).
 * US 02.01.01 - As an organizer I want to create a new event and generate a unique promotional QR code.
 * US 1.06.01 - As an entrant I want to view event details by scanning the promotional QR code (scan flow launches EventDetailsActivity).
 * Note: Activity verifies current user is event organizer; with a dummy eventId it may finish. Tests verify layout when shown.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class QRCodeIntentTest {

    private static Intent qrIntent(String eventId) {
        Intent i = QRCodeActivity.newIntent(ApplicationProvider.getApplicationContext(), eventId);
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return i;
    }

    @Rule
    public androidx.test.ext.junit.rules.ActivityScenarioRule<QRCodeActivity> rule =
            new ActivityScenarioRule<>(qrIntent("test-qr-event-dummy"));

    @Test
    public void testQRCodeScreenHasToolbarAndBackButton() {
        onView(withId(R.id.toolbar_qr_code)).check(matches(isDisplayed()));
        onView(withId(R.id.back_button)).check(matches(isDisplayed()));
    }

    @Test
    public void testQRCodeImageAndPromoCodeInputAreInLayout() {
        onView(withId(R.id.qr_code_image)).check(matches(isDisplayed()));
        onView(withId(R.id.input_promo_code)).check(matches(isDisplayed()));
    }
}
