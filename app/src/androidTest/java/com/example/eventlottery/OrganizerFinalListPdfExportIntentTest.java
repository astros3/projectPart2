package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import android.content.Context;
import android.content.SharedPreferences;
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
 * Intent/UI tests for the PDF export button on the organizer final list screen.
 * Story: As an organizer I want to export a final list of accepted entrants as a PDF.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class OrganizerFinalListPdfExportIntentTest {

    private static final String ORG_PREFS_NAME = "EventLotteryPrefs";
    private static final String KEY_ORG_CURRENT_EVENT_ID = "organizer_current_event_id";
    private static final String EVENT_ID = "test_event_final_list_pdf_export";

    private FirebaseFirestore db;
    private Context appContext;

    @Before
    public void setUp() throws Exception {
        appContext = ApplicationProvider.getApplicationContext();
        db = FirebaseFirestore.getInstance();
        setOrganizerCurrentEventId(EVENT_ID);

        Map<String, Object> event = new HashMap<>();
        event.put("eventId", EVENT_ID);
        event.put("title", "Final List PDF Test Event");
        event.put("organizerId", "test_org_pdf");
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
        if (db != null) {
            Tasks.await(db.collection("events").document(EVENT_ID).delete());
        }
        setOrganizerCurrentEventId(null);
    }

    /** The "Export PDF" button is rendered and visible in the final list fragment. */
    @Test
    public void finalListScreen_showsExportPdfButton() {
        launchFinalList();
        onView(withId(R.id.buttonExportPdf)).check(matches(isDisplayed()));
    }

    /** The "Export CSV" button is still rendered alongside the new PDF button. */
    @Test
    public void finalListScreen_showsBothExportButtons() {
        launchFinalList();
        onView(withId(R.id.buttonExportCsv)).check(matches(isDisplayed()));
        onView(withId(R.id.buttonExportPdf)).check(matches(isDisplayed()));
    }

    /** The toolbar title "Final List" is now unobstructed by export buttons. */
    @Test
    public void finalListScreen_titleIsDisplayed() {
        launchFinalList();
        onView(withText(R.string.final_list)).check(matches(isDisplayed()));
    }

    /** Tapping Export PDF on an empty list shows a toast (no crash). */
    @Test
    public void exportPdfButton_emptyList_doesNotCrash() {
        launchFinalList();
        onView(isRoot()).perform(waitFor(3000));
        onView(withId(R.id.buttonExportPdf)).perform(click());
        // If we reach here without crashing the test passes.
        onView(withId(R.id.buttonExportPdf)).check(matches(isDisplayed()));
    }

    /** Tapping Export CSV on an empty list still shows a toast (no crash). */
    @Test
    public void exportCsvButton_emptyList_doesNotCrash() {
        launchFinalList();
        onView(isRoot()).perform(waitFor(3000));
        onView(withId(R.id.buttonExportCsv)).perform(click());
        onView(withId(R.id.buttonExportCsv)).check(matches(isDisplayed()));
    }

    /** The entrant list view is present and fills the space above the button row. */
    @Test
    public void finalListScreen_listViewIsDisplayed() {
        launchFinalList();
        onView(withId(R.id.listFinalEntrants)).check(matches(isDisplayed()));
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private static void launchFinalList() {
        FragmentScenario.launchInContainer(
                FinalList.class,
                (Bundle) null,
                R.style.Theme_EventLottery,
                (FragmentFactory) null);
        onView(isRoot()).perform(waitFor(2000));
    }

    private void setOrganizerCurrentEventId(String eventId) {
        SharedPreferences sp = appContext.getApplicationContext()
                .getSharedPreferences(ORG_PREFS_NAME, Context.MODE_PRIVATE);
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

    private static ViewAction waitFor(long millis) {
        return new ViewAction() {
            @Override public Matcher<View> getConstraints() { return isRoot(); }
            @Override public String getDescription() { return "wait " + millis + "ms"; }
            @Override public void perform(UiController uiController, View view) {
                uiController.loopMainThreadForAtLeast(millis);
            }
        };
    }
}
