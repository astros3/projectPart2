package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import org.hamcrest.Matcher;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;
import java.util.Map;

/**
 * Intent/UI tests for the organizer final list screen .
 * Story: As an organizer I want to export a final list of entrants who enrolled for the event in CSV format.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerFinalListCsvExportIntentTest {

    private static final String EVENT_ID = "test_event_final_list_csv_export";

    private FirebaseFirestore db;
    private Context appContext;

    @Before
    public void setUp() throws Exception {
        appContext = ApplicationProvider.getApplicationContext();
        db = FirebaseFirestore.getInstance();
        EventEditActivity.setCurrentEventId(appContext, EVENT_ID);

        Map<String, Object> event = new HashMap<>();
        event.put("eventId", EVENT_ID);
        event.put("title", "Final List CSV Test Event");
        event.put("organizerId", "test_org_csv");
        event.put("organizerName", "Test Organizer");
        event.put("isPrivate", false);
        event.put("capacity", 10);
        event.put("waitingListLimit", 20);
        event.put("registrationStartMillis", System.currentTimeMillis() - 60_000L);
        event.put("registrationEndMillis", System.currentTimeMillis() + 86_400_000L);
        event.put("eventDateMillis", System.currentTimeMillis() + 7L * 86_400_000L);
        event.put("geolocationRequired", false);
        Tasks.await(db.collection("events").document(EVENT_ID).set(event));
    }

    @After
    public void tearDown() throws Exception {
        if (db == null) {
            return;
        }
        Tasks.await(db.collection("events").document(EVENT_ID).delete());
        EventEditActivity.setCurrentEventId(appContext, null);
    }

    @Test
    public void finalListScreen_showsExportCsvButtonAndList() {
        FragmentScenario.launchInContainer(
                FinalList.class,
                (Bundle) null,
                R.style.Theme_EventLottery,
                (FragmentFactory) null);

        onView(isRoot()).perform(waitFor(3000));
        onView(withText(R.string.final_list)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonExportCsv)).check(matches(isDisplayed()));
        onView(withId(R.id.listFinalEntrants)).check(matches(isDisplayed()));
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
