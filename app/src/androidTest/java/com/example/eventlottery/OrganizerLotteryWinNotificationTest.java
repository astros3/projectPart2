package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.anything;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class OrganizerLotteryWinNotificationTest {

    private FirebaseFirestore db;
    private Context appContext;

    private String deviceId; // will act as the chosen entrant
    private final String EVENT_ID = "test_event_lottery_win_notif";

    @Before
    public void setUp() throws Exception {
        appContext = ApplicationProvider.getApplicationContext();
        db = FirebaseFirestore.getInstance();
        deviceId = DeviceIdManager.getDeviceId(appContext);

        // Current event for organizer flows (LotteryDraw reads this)
        EventEditActivity.setCurrentEventId(appContext, EVENT_ID);

        // Seed event doc (minimal fields; parser/UI won’t be used here, but keep it sane)
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", EVENT_ID);
        event.put("title", "Lottery Win Notif Test Event");
        event.put("organizerId", "test_org");
        event.put("organizerName", "Test Organizer");
        event.put("isPrivate", false);
        event.put("capacity", 1);
        event.put("waitingListLimit", 10);
        event.put("registrationStartMillis", System.currentTimeMillis() - 60_000L);
        event.put("registrationEndMillis", System.currentTimeMillis() + 86_400_000L);
        event.put("eventDateMillis", System.currentTimeMillis() + 7L * 86_400_000L);
        event.put("geolocationRequired", false);
        Tasks.await(db.collection("events").document(EVENT_ID).set(event));

        // Seed entrant profile for THIS deviceId with notifications enabled
        Map<String, Object> entrant = new HashMap<>();
        entrant.put("deviceID", deviceId);
        entrant.put("fullName", "Lottery Winner (Test)");
        entrant.put("email", "winner@test.com");
        entrant.put("role", "Entrant");
        entrant.put("notificationsEnabled", true); // REQUIRED: NotificationHelper checks this
        Tasks.await(db.collection("users").document(deviceId).set(entrant));

        // Seed waiting list entry as PENDING for this deviceId
        // IMPORTANT: LotteryDraw expects WaitingListEntry.getDeviceId() to be non-null
        Map<String, Object> waiting = new HashMap<>();
        waiting.put("deviceId", deviceId);
        waiting.put("status", WaitingListEntry.Status.PENDING.name());
        Tasks.await(db.collection("events").document(EVENT_ID)
                .collection("waitingList").document(deviceId)
                .set(waiting));

        // Clean any old notifications for this event (in case a previous run left data)
        deleteNotificationsForEvent(EVENT_ID);
    }

    @After
    public void tearDown() throws Exception {
        if (db == null) return;

        deleteNotificationsForEvent(EVENT_ID);

        Tasks.await(db.collection("events").document(EVENT_ID)
                .collection("waitingList").document(deviceId).delete());

        // Keep your real user profile? If you don’t want tests touching it, comment this out.
        Tasks.await(db.collection("users").document(deviceId).delete());

        Tasks.await(db.collection("events").document(EVENT_ID).delete());
    }

    @Test
    public void organizerDraw_sendsLotteryWinNotification_andEntrantSeesItInUi() throws Exception {
        // 1) Organizer runs LotteryDraw UI: draw 1 winner
        FragmentScenario.launchInContainer(
                LotteryDraw.class,
                (Bundle) null,
                R.style.Theme_EventLottery,
                (FragmentFactory) null
        );

        onView(withId(R.id.editTextNumber)).perform(replaceText("1"), closeSoftKeyboard());
        onView(withId(R.id.buttonDraw)).perform(click());

        // 2) Verify notification doc exists in Firestore (poll because async)
        waitUntilNotificationExists(EVENT_ID, 12, 400);

        // 3) Entrant opens notifications UI and sees the win title
        ActivityScenario.launch(EntrantNotificationsActivity.class);

        // Give it a moment to load Firestore + render list
        onView(isRoot()).perform(waitFor(1200));

        // Avoid AmbiguousViewMatcherException when multiple rows share the same title:
        // assert the first row's title is the expected win title.
        onData(anything())
                .inAdapterView(withId(R.id.list_notifications))
                .atPosition(0)
                .onChildView(withId(R.id.text_notification_title))
                .check(matches(withText(NotificationHelper.LOTTERY_WIN_TITLE)));
    }

    private void waitUntilNotificationExists(String eventId, int attempts, long sleepMs) throws Exception {
        for (int i = 0; i < attempts; i++) {
            QuerySnapshot snap = Tasks.await(
                    db.collection("users").document(deviceId)
                            .collection("notifications")
                            .whereEqualTo("eventId", eventId)
                            .whereEqualTo("type", NotificationHelper.TYPE_LOTTERY_WON)
                            .get()
            );
            if (snap != null && !snap.isEmpty()) return;
            Thread.sleep(sleepMs);
        }
        throw new AssertionError("Expected LOTTERY_WON notification for eventId=" + eventId + " but none found.");
    }

    private void deleteNotificationsForEvent(String eventId) throws Exception {
        QuerySnapshot snap = Tasks.await(
                db.collection("users").document(deviceId)
                        .collection("notifications")
                        .whereEqualTo("eventId", eventId)
                        .get()
        );
        if (snap == null || snap.isEmpty()) return;

        for (DocumentSnapshot doc : snap.getDocuments()) {
            Tasks.await(doc.getReference().delete());
        }
    }

    private static ViewAction waitFor(long millis) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isRoot(); }
            @Override public String getDescription() { return "wait for " + millis + "ms"; }
            @Override public void perform(UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }
}