package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.RGBLuminanceSource;
import com.google.zxing.Result;
import com.google.zxing.common.HybridBinarizer;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Intent tests for QR code screen (organizer).
 * US 02.01.01 - As an organizer I want to create a new event and generate a unique promotional QR code.
 * US 1.06.01 - As an entrant I want to view event details by scanning the promotional QR code (scan flow launches EventDetailsActivity).
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class QRCodeIntentTest {

    private String deviceId;
    private String createdEventId;
    /** Optional second event created inside a test; removed in {@code @After}. */
    private String secondEventId;

    @Before
    public void createTestOrganizerAndEvent() throws Exception {
        Context ctx = ApplicationProvider.getApplicationContext();
        deviceId = DeviceIdManager.getDeviceId(ctx);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> organizer = new HashMap<>();
        organizer.put("organizerId", deviceId);
        organizer.put("firstName", "Test");
        organizer.put("lastName", "Organizer");
        organizer.put("role", "organizer");
        Tasks.await(db.collection("organizers").document(deviceId).set(organizer));

        createdEventId = db.collection("events").document().getId();
        Tasks.await(db.collection("events").document(createdEventId).set(
                buildPublicEventMap(deviceId, createdEventId)));

        secondEventId = null;
    }

    @After
    public void deleteTestEventAndOrganizer() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (secondEventId != null) {
            Tasks.await(db.collection("events").document(secondEventId).delete());
        }
        if (createdEventId != null) {
            Tasks.await(db.collection("events").document(createdEventId).delete());
        }
        if (deviceId != null) {
            Tasks.await(db.collection("organizers").document(deviceId).delete());
        }
    }

    private static Map<String, Object> buildPublicEventMap(String organizerDeviceId, String eventDocId) {
        long now = System.currentTimeMillis();
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", eventDocId);
        event.put("title", "Test Event for QR Test");
        event.put("description", "Description");
        event.put("location", "Edmonton");
        event.put("organizerId", organizerDeviceId);
        event.put("organizerName", "Test Organizer");
        event.put("capacity", 10);
        event.put("waitingListLimit", 0);
        event.put("registrationStartMillis", now - TimeUnit.DAYS.toMillis(1));
        event.put("registrationEndMillis", now + TimeUnit.DAYS.toMillis(7));
        event.put("eventDateMillis", now + TimeUnit.DAYS.toMillis(14));
        event.put("geolocationRequired", false);
        event.put("isPrivate", false);
        event.put("price", 0.0);
        event.put("selectionCriteria", new ArrayList<String>());
        event.put("promoCode", "777 888");
        return event;
    }

    private void launchQRCodeActivity() {
        launchQRCodeActivityForEvent(createdEventId);
    }

    private static void launchQRCodeActivityForEvent(String eventId) {
        Intent intent = QRCodeActivity.newIntent(ApplicationProvider.getApplicationContext(), eventId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ActivityScenario.launch(intent);
    }

    @Test
    public void testQRCodeScreenHasToolbarAndBackButton() {
        launchQRCodeActivity();
        onView(withId(R.id.toolbar_qr_code)).check(matches(isDisplayed()));
        onView(withId(R.id.back_button)).check(matches(isDisplayed()));
    }

    @Test
    public void testQRCodeImageAndPromoCodeInputAreInLayout() {
        launchQRCodeActivity();
        onView(withId(R.id.qr_code_image)).check(matches(isDisplayed()));
        onView(withId(R.id.input_promo_code)).check(matches(isDisplayed()));
    }

    /**
     * US 02.01.01: Promotional QR encodes the app deep link so scanning opens {@link EventDetailsActivity} for this event.
     */
    @Test
    public void promotionalQr_decodesToEventDeepLinkForFirestoreEventId() {
        launchQRCodeActivity();
        onView(isRoot()).perform(waitFor(3000));

        String expected = "eventlottery://event/" + createdEventId;
        assertEquals(expected, decodeQrPayloadFromDisplayedImage());
    }

    /**
     * US 02.01.01: Two public events yield different QR payloads (unique per event id).
     */
    @Test
    public void promotionalQr_payloadIsUniquePerEvent() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        secondEventId = db.collection("events").document().getId();
        Tasks.await(db.collection("events").document(secondEventId).set(buildPublicEventMap(deviceId, secondEventId)));

        String firstPayload;
        try (ActivityScenario<QRCodeActivity> ignored =
                     ActivityScenario.launch(qrIntent(createdEventId))) {
            onView(isRoot()).perform(waitFor(3000));
            firstPayload = decodeQrPayloadFromDisplayedImage();
        }

        String secondPayload;
        try (ActivityScenario<QRCodeActivity> ignored =
                     ActivityScenario.launch(qrIntent(secondEventId))) {
            onView(isRoot()).perform(waitFor(3000));
            secondPayload = decodeQrPayloadFromDisplayedImage();
        }

        assertEquals("eventlottery://event/" + createdEventId, firstPayload);
        assertEquals("eventlottery://event/" + secondEventId, secondPayload);
        assertNotEquals(firstPayload, secondPayload);
    }

    /**
     * US 02.01.01: Promo code from Firestore is shown for the organizer (paired with QR on the same screen).
     */
    @Test
    public void qrScreen_displaysStoredPromoCodeFromFirestore() {
        launchQRCodeActivity();
        onView(isRoot()).perform(waitFor(3000));
        onView(withId(R.id.input_promo_code)).check(matches(withText("777 888")));
    }

    private static Intent qrIntent(String eventId) {
        Intent intent = QRCodeActivity.newIntent(ApplicationProvider.getApplicationContext(), eventId);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    private static String decodeQrPayloadFromDisplayedImage() {
        final String[] holder = new String[1];
        onView(withId(R.id.qr_code_image)).perform(new ViewAction() {
            @Override
            public Matcher<View> getConstraints() {
                return isAssignableFrom(ImageView.class);
            }

            @Override
            public String getDescription() {
                return "Decode QR bitmap from ImageView";
            }

            @Override
            public void perform(UiController uiController, View view) {
                ImageView iv = (ImageView) view;
                Drawable d = iv.getDrawable();
                if (!(d instanceof BitmapDrawable)) {
                    throw new AssertionError("QR ImageView should hold a bitmap");
                }
                Bitmap bm = ((BitmapDrawable) d).getBitmap();
                try {
                    holder[0] = decodeQrFromBitmap(bm);
                } catch (Exception e) {
                    throw new AssertionError("Failed to decode QR: " + e.getMessage(), e);
                }
            }
        });
        return holder[0];
    }

    private static String decodeQrFromBitmap(Bitmap bitmap) throws Exception {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int[] pixels = new int[w * h];
        bitmap.getPixels(pixels, 0, w, 0, 0, w, h);
        RGBLuminanceSource source = new RGBLuminanceSource(w, h, pixels);
        BinaryBitmap binaryBitmap = new BinaryBitmap(new HybridBinarizer(source));
        Result result = new MultiFormatReader().decode(binaryBitmap);
        return result.getText();
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
