package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.anything;

import android.content.Context;
import android.content.Intent;
import android.view.View;

import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Intent tests for notification preferences and private event invite functionality.
 * US 01.04.01 - Win notification shown in notifications screen.
 * US 01.04.02 - Loss notification shown in notifications screen.
 * US 01.04.03 - Notification toggle visible in profile screen.
 * US 01.05.05 - Selection criteria visible in event details screen.
 * US 01.05.06 - Private invite notification shown in notifications screen.
 * US 01.05.07 - Accept/Decline buttons shown when status is INVITED.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class NotificationAndPrivateEventIntentTest {

    private static final String TEST_EVENT_ID      = "test_event_notif_private";
    private static final String TEST_PRIVATE_EVENT_ID = "test_event_private_invite";

    private FirebaseFirestore db;
    private Context appContext;
    private String deviceId;

    @Before
    public void setUp() throws Exception {
        appContext = ApplicationProvider.getApplicationContext();
        db = FirebaseFirestore.getInstance();
        deviceId = DeviceIdManager.getDeviceId(appContext);

        EventEditActivity.setCurrentEventId(appContext, TEST_EVENT_ID);

        // Seed entrant profile with notifications enabled
        Map<String, Object> entrant = new HashMap<>();
        entrant.put("deviceID", deviceId);
        entrant.put("name", "Test Entrant");
        entrant.put("email", "test@test.com");
        entrant.put("role", "entrant");
        entrant.put("notificationsEnabled", true);
        Tasks.await(db.collection("users").document(deviceId).set(entrant));

        // Seed public test event with selection criteria (US 01.05.05)
        long now = System.currentTimeMillis();
        Map<String, Object> event = new HashMap<>();
        event.put("eventId", TEST_EVENT_ID);
        event.put("title", "Test Notification Event");
        event.put("description", "Test description");
        event.put("location", "Edmonton");
        event.put("organizerId", "test_organizer");
        event.put("organizerName", "Test Organizer");
        event.put("capacity", 10);
        event.put("waitingListLimit", 0);
        event.put("registrationStartMillis", now - TimeUnit.DAYS.toMillis(1));
        event.put("registrationEndMillis", now + TimeUnit.DAYS.toMillis(7));
        event.put("eventDateMillis", now + TimeUnit.DAYS.toMillis(14));
        event.put("geolocationRequired", false);
        event.put("isPrivate", false);
        event.put("price", 0.0);
        event.put("selectionCriteria", Arrays.asList("Must be 18+", "Beginner level only"));
        Tasks.await(db.collection("events").document(TEST_EVENT_ID).set(event));

        // Seed private test event (US 01.05.06, 01.05.07)
        Map<String, Object> privateEvent = new HashMap<>();
        privateEvent.put("eventId", TEST_PRIVATE_EVENT_ID);
        privateEvent.put("title", "Private Test Event");
        privateEvent.put("description", "Private event description");
        privateEvent.put("location", "Edmonton");
        privateEvent.put("organizerId", "test_organizer");
        privateEvent.put("organizerName", "Test Organizer");
        privateEvent.put("capacity", 5);
        privateEvent.put("waitingListLimit", 0);
        privateEvent.put("registrationStartMillis", now - TimeUnit.DAYS.toMillis(1));
        privateEvent.put("registrationEndMillis", now + TimeUnit.DAYS.toMillis(7));
        privateEvent.put("eventDateMillis", now + TimeUnit.DAYS.toMillis(14));
        privateEvent.put("geolocationRequired", false);
        privateEvent.put("isPrivate", true);
        privateEvent.put("price", 0.0);
        privateEvent.put("selectionCriteria", new ArrayList<String>());
        Tasks.await(db.collection("events").document(TEST_PRIVATE_EVENT_ID).set(privateEvent));

        // Clean old notifications
        deleteNotificationsForEvent(TEST_EVENT_ID);
        deleteNotificationsForEvent(TEST_PRIVATE_EVENT_ID);
    }

    @After
    public void tearDown() throws Exception {
        if (db == null) return;

        deleteNotificationsForEvent(TEST_EVENT_ID);
        deleteNotificationsForEvent(TEST_PRIVATE_EVENT_ID);

        Tasks.await(db.collection("events").document(TEST_EVENT_ID)
                .collection("waitingList").document(deviceId).delete());
        Tasks.await(db.collection("events").document(TEST_PRIVATE_EVENT_ID)
                .collection("waitingList").document(deviceId).delete());
        Tasks.await(db.collection("events").document(TEST_EVENT_ID).delete());
        Tasks.await(db.collection("events").document(TEST_PRIVATE_EVENT_ID).delete());
        Tasks.await(db.collection("users").document(deviceId).delete());
    }

    // ─── US 01.04.01 — Win notification shown in notifications screen ──────────

    /**
     * US 01.04.01: After organizer sends a lottery win notification,
     * entrant sees it in the notifications screen.
     */
    @Test
    public void lotteryWin_notificationAppearsInNotificationsScreen() throws Exception {
        // Seed a win notification directly (simulates LotteryDraw sending it)
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", NotificationHelper.TYPE_LOTTERY_WON);
        notification.put("title", NotificationHelper.LOTTERY_WIN_TITLE);
        notification.put("message", NotificationHelper.LOTTERY_WIN_MESSAGE);
        notification.put("eventId", TEST_EVENT_ID);
        notification.put("timestampMillis", System.currentTimeMillis());
        notification.put("read", false);
        Tasks.await(db.collection("users").document(deviceId)
                .collection("notifications").add(notification));

        ActivityScenario.launch(EntrantNotificationsActivity.class);
        onView(isRoot()).perform(waitFor(1500));

        onData(anything())
                .inAdapterView(withId(R.id.list_notifications))
                .atPosition(0)
                .onChildView(withId(R.id.text_notification_title))
                .check(matches(withText(NotificationHelper.LOTTERY_WIN_TITLE)));
    }

    // ─── US 01.04.02 — Loss notification shown in notifications screen ─────────

    /**
     * US 01.04.02: After lottery draw, entrant who was not selected
     * sees a loss notification in the notifications screen.
     */
    @Test
    public void lotteryLoss_notificationAppearsInNotificationsScreen() throws Exception {
        // Seed a loss notification directly
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", NotificationHelper.TYPE_LOTTERY_LOST);
        notification.put("title", "Lottery result");
        notification.put("message",
                "Unfortunately, you were not selected in this draw. " +
                        "You may still get a chance if someone declines their invitation.");
        notification.put("eventId", TEST_EVENT_ID);
        notification.put("timestampMillis", System.currentTimeMillis());
        notification.put("read", false);
        Tasks.await(db.collection("users").document(deviceId)
                .collection("notifications").add(notification));

        ActivityScenario.launch(EntrantNotificationsActivity.class);
        onView(isRoot()).perform(waitFor(1500));

        onData(anything())
                .inAdapterView(withId(R.id.list_notifications))
                .atPosition(0)
                .onChildView(withId(R.id.text_notification_title))
                .check(matches(withText("Lottery result")));
    }

    // ─── US 01.04.03 — Notification toggle visible in profile screen ──────────

    /**
     * US 01.04.03: Notification toggle switch is visible in the profile screen
     * so entrants can opt out of notifications.
     */
    @Test
    public void profileScreen_notificationToggleIsVisible() {
        Intent intent = new Intent(appContext, EntrantProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ActivityScenario.launch(intent);

        onView(withId(R.id.switch_notifications)).check(matches(isDisplayed()));
    }

    /**
     * US 01.04.03: Entrant can tap the notification toggle to opt out.
     * Toggle should still be displayed after being clicked.
     */
    @Test
    public void profileScreen_notificationToggleCanBeTapped() {
        Intent intent = new Intent(appContext, EntrantProfileActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        ActivityScenario.launch(intent);

        onView(withId(R.id.switch_notifications)).check(matches(isDisplayed()));
        onView(withId(R.id.switch_notifications)).perform(click());
        onView(withId(R.id.switch_notifications)).check(matches(isDisplayed()));
    }

    // ─── US 01.05.05 — Selection criteria visible in event details ────────────

    /**
     * US 01.05.05: Selection criteria field is visible in the event details screen.
     */
    @Test
    public void eventDetails_selectionCriteriaIsVisible() {
        Intent intent = new Intent(appContext, EventDetailsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, TEST_EVENT_ID);
        intent.putExtra(EventDetailsActivity.EXTRA_VIEW_AS_ENTRANT, true);
        ActivityScenario.launch(intent);

        onView(isRoot()).perform(waitFor(1500));
        onView(withId(R.id.event_selection_criteria)).check(matches(isDisplayed()));
    }

    // ─── US 01.05.06 — Private invite notification shown ─────────────────────

    /**
     * US 01.05.06: After organizer invites entrant to private event,
     * entrant sees private invite notification in notifications screen.
     */
    @Test
    public void privateInvite_notificationAppearsInNotificationsScreen() throws Exception {
        // Seed a private invite notification
        Map<String, Object> notification = new HashMap<>();
        notification.put("type", NotificationHelper.TYPE_PRIVATE_INVITE);
        notification.put("title", NotificationHelper.PRIVATE_INVITE_TITLE);
        notification.put("message", NotificationHelper.PRIVATE_INVITE_MESSAGE);
        notification.put("eventId", TEST_PRIVATE_EVENT_ID);
        notification.put("timestampMillis", System.currentTimeMillis());
        notification.put("read", false);
        Tasks.await(db.collection("users").document(deviceId)
                .collection("notifications").add(notification));

        ActivityScenario.launch(EntrantNotificationsActivity.class);
        onView(isRoot()).perform(waitFor(1500));

        onData(anything())
                .inAdapterView(withId(R.id.list_notifications))
                .atPosition(0)
                .onChildView(withId(R.id.text_notification_title))
                .check(matches(withText(NotificationHelper.PRIVATE_INVITE_TITLE)));
    }

    // ─── US 01.05.07 — Accept/Decline buttons shown when INVITED ─────────────

    /**
     * US 01.05.07: When entrant's waiting list status is INVITED,
     * the invitation buttons container is visible in event details screen.
     */
    @Test
    public void eventDetails_whenStatusIsInvited_invitationButtonsAreVisible()
            throws Exception {
        // Seed waiting list entry with INVITED status
        Map<String, Object> entry = new HashMap<>();
        entry.put("deviceId", deviceId);
        entry.put("status", WaitingListEntry.Status.INVITED.name());
        Tasks.await(db.collection("events").document(TEST_PRIVATE_EVENT_ID)
                .collection("waitingList").document(deviceId).set(entry));

        Intent intent = new Intent(appContext, EventDetailsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, TEST_PRIVATE_EVENT_ID);
        intent.putExtra(EventDetailsActivity.EXTRA_VIEW_AS_ENTRANT, true);
        ActivityScenario.launch(intent);

        onView(isRoot()).perform(waitFor(1500));
        onView(withId(R.id.invitation_buttons_container)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_accept_invitation)).check(matches(isDisplayed()));
        onView(withId(R.id.btn_decline_invitation)).check(matches(isDisplayed()));
    }

    /**
     * US 01.05.07: Accept button shows correct text when status is INVITED.
     */
    @Test
    public void eventDetails_whenStatusIsInvited_acceptButtonShowsCorrectText()
            throws Exception {
        // Seed waiting list entry with INVITED status
        Map<String, Object> entry = new HashMap<>();
        entry.put("deviceId", deviceId);
        entry.put("status", WaitingListEntry.Status.INVITED.name());
        Tasks.await(db.collection("events").document(TEST_PRIVATE_EVENT_ID)
                .collection("waitingList").document(deviceId).set(entry));

        Intent intent = new Intent(appContext, EventDetailsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, TEST_PRIVATE_EVENT_ID);
        intent.putExtra(EventDetailsActivity.EXTRA_VIEW_AS_ENTRANT, true);
        ActivityScenario.launch(intent);

        onView(isRoot()).perform(waitFor(1500));
        onView(withId(R.id.btn_accept_invitation))
                .check(matches(withText("Accept Invitation")));
        onView(withId(R.id.btn_decline_invitation))
                .check(matches(withText("Decline Invitation")));
    }

    // ─── Helpers ──────────────────────────────────────────────────────────────

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