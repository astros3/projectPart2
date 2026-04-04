package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.scrollTo;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Intent/UI tests for private events (US 02.01.02): not on entrant event listing (unless invited),
 * no promotional QR (organizer menu shows invite instead), and private flag on edit screen.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerPrivateEventIntentTest {

    private static final String EVENT_EDIT = "test_private_event_edit";
    private static final String EVENT_NAV = "test_private_event_nav";
    private static final String EVENT_LIST_PUBLIC = "test_private_event_list_pub";
    private static final String EVENT_LIST_PRIVATE = "test_private_event_list_priv";

    private static final String TITLE_PUBLIC = "US020102_Public_Visible_On_List";
    private static final String TITLE_PRIVATE = "US020102_Private_Hidden_From_List";

    /** Same as {@link EventEditActivity} prefs — must match for {@code getCurrentEventId}. */
    private static final String ORG_PREFS_NAME = "EventLotteryPrefs";
    private static final String KEY_ORG_CURRENT_EVENT_ID = "organizer_current_event_id";

    private Context appContext;
    private FirebaseFirestore db;
    private String deviceId;

    @Before
    public void setUp() throws Exception {
        appContext = ApplicationProvider.getApplicationContext();
        db = FirebaseFirestore.getInstance();
        deviceId = DeviceIdManager.getDeviceId(appContext);

        Map<String, Object> organizer = new HashMap<>();
        organizer.put("organizerId", deviceId);
        organizer.put("firstName", "Test");
        organizer.put("lastName", "Organizer");
        organizer.put("role", "organizer");
        Tasks.await(db.collection("organizers").document(deviceId).set(organizer));
    }

    @After
    public void tearDown() throws Exception {
        if (db == null) {
            return;
        }
        for (String id : new String[] {EVENT_EDIT, EVENT_NAV, EVENT_LIST_PUBLIC, EVENT_LIST_PRIVATE}) {
            try {
                Tasks.await(db.collection("events").document(id).delete());
            } catch (Exception ignored) {
                // Document may already be gone.
            }
        }
        if (deviceId != null) {
            try {
                Tasks.await(db.collection("organizers").document(deviceId).delete());
            } catch (Exception ignored) {
                // ignore
            }
        }
        if (appContext != null) {
            setOrganizerCurrentEventIdSync(null);
        }
    }

    @Test
    public void eventEdit_privateEventSwitch_isDisplayed() throws Exception {
        Tasks.await(db.collection("events").document(EVENT_EDIT).set(
                eventDoc(EVENT_EDIT, "Edit Private Switch", false)));

        Intent intent = EventEditActivity.newIntent(appContext, EVENT_EDIT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        try (ActivityScenario<EventEditActivity> scenario = ActivityScenario.launch(intent)) {
            onView(isRoot()).perform(waitFor(3000));
            onView(withId(R.id.switch_private_event)).perform(scrollTo());
            onView(withId(R.id.switch_private_event)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void eventEdit_whenEventIsPrivate_switchIsOn() throws Exception {
        Map<String, Object> doc = eventDoc(EVENT_EDIT, "Private Event Loaded", true);
        Tasks.await(db.collection("events").document(EVENT_EDIT).set(doc));

        Intent intent = EventEditActivity.newIntent(appContext, EVENT_EDIT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        try (ActivityScenario<EventEditActivity> scenario = ActivityScenario.launch(intent)) {
            onView(isRoot()).perform(waitFor(3500));
            onView(withId(R.id.switch_private_event)).perform(scrollTo());
            onView(withId(R.id.switch_private_event)).check(matches(isChecked()));
        }
    }

    @Test
    public void organizerNavigation_privateEvent_hidesQr_showsInviteEntrants() throws Exception {
        Tasks.await(db.collection("events").document(EVENT_NAV).set(
                eventDoc(EVENT_NAV, "Nav Private Event", true)));
        // EventEditActivity.setCurrentEventId uses apply() (async). The fragment reads prefs in
        // onViewCreated; if the write is not visible yet, eventId is empty and QR never hides.
        setOrganizerCurrentEventIdSync(EVENT_NAV);

        FragmentScenario.launchInContainer(
                OrganizerNavigationFragment.class,
                (Bundle) null,
                R.style.Theme_EventLottery,
                (FragmentFactory) null);

        onView(isRoot()).perform(waitFor(5000));
        onView(withId(R.id.buttonQR)).check(matches(not(isDisplayed())));
        onView(withId(R.id.buttonInviteEntrants)).check(matches(isDisplayed()));
    }

    @Test
    public void entrantEventList_privateEventHidden_publicEventVisible() throws Exception {
        Map<String, Object> pub = eventDoc(EVENT_LIST_PUBLIC, TITLE_PUBLIC, false);
        Map<String, Object> priv = eventDoc(EVENT_LIST_PRIVATE, TITLE_PRIVATE, true);
        Tasks.await(db.collection("events").document(EVENT_LIST_PUBLIC).set(pub));
        Tasks.await(db.collection("events").document(EVENT_LIST_PRIVATE).set(priv));

        Intent intent = new Intent(appContext, EntrantMainScreenActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        try (ActivityScenario<EntrantMainScreenActivity> scenario = ActivityScenario.launch(intent)) {
            onView(isRoot()).perform(waitFor(6000));
            onView(withText(TITLE_PRIVATE)).check(doesNotExist());
            // ListView may not lay out off-screen rows; onData() scrolls to the matching adapter item.
            onData(allOf(instanceOf(Event.class), eventWithTitle(TITLE_PUBLIC)))
                    .inAdapterView(withId(R.id.Events))
                    .onChildView(withId(R.id.event_name_input))
                    .check(matches(withText(TITLE_PUBLIC)));
        }
    }

    private static TypeSafeMatcher<Object> eventWithTitle(final String title) {
        return new TypeSafeMatcher<Object>() {
            @Override
            protected boolean matchesSafely(Object item) {
                return item instanceof Event && title.equals(((Event) item).getTitle());
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("Event with title ").appendValue(title);
            }
        };
    }

    /**
     * Persists organizer “current event” synchronously so fragment tests see the id immediately.
     */
    private void setOrganizerCurrentEventIdSync(String eventId) {
        Context storageCtx = appContext.getApplicationContext();
        SharedPreferences sp = storageCtx.getSharedPreferences(ORG_PREFS_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor ed = sp.edit();
        if (eventId == null || eventId.isEmpty()) {
            ed.remove(KEY_ORG_CURRENT_EVENT_ID);
        } else {
            ed.putString(KEY_ORG_CURRENT_EVENT_ID, eventId);
        }
        if (!ed.commit()) {
            throw new IllegalStateException("SharedPreferences commit failed");
        }
    }

    private Map<String, Object> eventDoc(String eventId, String title, boolean isPrivate) {
        long now = System.currentTimeMillis();
        Map<String, Object> m = new HashMap<>();
        m.put("eventId", eventId);
        m.put("title", title);
        m.put("description", "Test description");
        m.put("location", "Edmonton");
        m.put("organizerId", deviceId);
        m.put("organizerName", "Test Organizer");
        m.put("capacity", 10);
        m.put("waitingListLimit", 20);
        m.put("registrationStartMillis", now - TimeUnit.DAYS.toMillis(1));
        m.put("registrationEndMillis", now + TimeUnit.DAYS.toMillis(7));
        m.put("eventDateMillis", now + TimeUnit.DAYS.toMillis(14));
        m.put("geolocationRequired", false);
        m.put("isPrivate", isPrivate);
        m.put("private", isPrivate);
        m.put("price", 0.0);
        m.put("selectionCriteria", new ArrayList<String>());
        return m;
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
