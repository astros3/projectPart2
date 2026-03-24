package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

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
 *
 * Creates a test organizer and event in Firestore in @Before so the test device is the event's organizer;
 * QRCodeActivity then loads and shows the QR screen. In @After deletes the event and organizer so the test is idempotent.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class QRCodeIntentTest {

    private String deviceId;
    private String createdEventId;

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
        long now = System.currentTimeMillis();
        Map<String, Object> event = new HashMap<>();
        event.put("title", "Test Event for QR Test");
        event.put("description", "Description");
        event.put("location", "Edmonton");
        event.put("organizerId", deviceId);
        event.put("organizerName", "Test Organizer");
        event.put("capacity", 10);
        event.put("waitingListLimit", 0);
        event.put("registrationStartMillis", now - TimeUnit.DAYS.toMillis(1));
        event.put("registrationEndMillis", now + TimeUnit.DAYS.toMillis(7));
        event.put("eventDateMillis", now + TimeUnit.DAYS.toMillis(14));
        event.put("geolocationRequired", false);
        event.put("price", 0.0);
        event.put("selectionCriteria", new ArrayList<String>());

        Tasks.await(db.collection("events").document(createdEventId).set(event));
    }

    @After
    public void deleteTestEventAndOrganizer() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (createdEventId != null) {
            Tasks.await(db.collection("events").document(createdEventId).delete());
        }
        if (deviceId != null) {
            Tasks.await(db.collection("organizers").document(deviceId).delete());
        }
    }

    private void launchQRCodeActivity() {
        Intent intent = QRCodeActivity.newIntent(ApplicationProvider.getApplicationContext(), createdEventId);
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
}
