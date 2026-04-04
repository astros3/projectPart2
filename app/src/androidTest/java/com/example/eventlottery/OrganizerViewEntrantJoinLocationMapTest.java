package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isRoot;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.containsString;

import android.os.Bundle;
import android.view.View;

import androidx.fragment.app.FragmentFactory;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.espresso.UiController;
import androidx.test.espresso.ViewAction;

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
 * UI test for: Organizer can see on a map where an entrant joined from.
 * This verifies GeolocationFragment renders with an entrant deviceId and
 * displays the header + map container + address text.
 */
@RunWith(AndroidJUnit4.class)
public class OrganizerViewEntrantJoinLocationMapTest {

    private FirebaseFirestore db;
    private final String ENTRANT_ID = "test_geo_entrant_1";

    @Before
    public void seedEntrantLocation() throws Exception {
        db = FirebaseFirestore.getInstance();

        Map<String, Object> entrant = new HashMap<>();
        entrant.put("deviceID", ENTRANT_ID);
        entrant.put("fullName", "Geo Test Entrant");
        entrant.put("email", "geo@test.com");
        entrant.put("role", "Entrant");

        // What GeolocationFragment reads:
        entrant.put("latitude", 53.5461);
        entrant.put("longitude", -113.4938);
        entrant.put("locationAddress", "Edmonton");

        Tasks.await(db.collection("users").document(ENTRANT_ID).set(entrant));
    }

    @After
    public void cleanup() throws Exception {
        if (db == null) return;
        Tasks.await(db.collection("users").document(ENTRANT_ID).delete());
    }

    @Test
    public void geolocationFragment_showsEntrantLocationUi() {
        Bundle args = new Bundle();
        args.putString("deviceId", ENTRANT_ID);

        FragmentScenario<GeolocationFragment> scenario = FragmentScenario.launchInContainer(GeolocationFragment.class, args, R.style.Theme_EventLottery, (FragmentFactory) null);

        // Basic UI visible
        onView(withId(R.id.buttonBackGeo)).check(matches(isDisplayed()));
        onView(withId(R.id.textLocationOf)).check(matches(isDisplayed()));
        onView(withId(R.id.map)).check(matches(isDisplayed()));
        onView(withId(R.id.textLocationAddress)).check(matches(isDisplayed()));

        // Wait for Firestore -> UI binding, then assert by ID to avoid matcher ambiguity/flakiness.
        onView(isRoot()).perform(waitFor(800));
        onView(withId(R.id.textLocationOf))
                .check(matches(withText(containsString("Geo Test Entrant"))));
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