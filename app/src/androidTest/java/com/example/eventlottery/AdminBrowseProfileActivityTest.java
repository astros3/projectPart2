package com.example.eventlottery;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.replaceText;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import static org.hamcrest.CoreMatchers.anything;

import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.rules.ActivityScenarioRule;

import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * Intent tests for Admin profile control screen.
 * User stories: Huayu
 */
public class AdminBrowseProfileActivityTest {
    private String testUserId;
    private String adminDeviceId;
    @Rule
    public ActivityScenarioRule<AdminBrowseProfilesActivity> rule =
            new ActivityScenarioRule<>(AdminBrowseProfilesActivity.class);

    /** reference eventdetailsintenttest*/
    @Before
    public void createTest() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        adminDeviceId = DeviceIdManager.getDeviceId(ApplicationProvider.getApplicationContext());
        Map<String, Object> admin = new HashMap<>();
        admin.put("adminId", adminDeviceId);
        admin.put("firstName", "123123");
        admin.put("lastName", "1231");
        admin.put("email", "admin@123.com");
        admin.put("phoneNumber", "111234567");
        Tasks.await(db.collection("admins").document(adminDeviceId).set(admin));
        testUserId = "test1";
        Map<String, Object> user = new HashMap<>();
        user.put("deviceID", "test1");
        user.put("fullName", "test1");
        user.put("email", "test@test.com");
        user.put("phoneNumber", "111234567");
        user.put("role", "Entrant");
        Tasks.await(db.collection("users").document("test1").set(user));
    }
    /** reference eventdetailsintenttest*/
    @After
    public void deletepreconditions() throws Exception {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (testUserId != null) {
            Tasks.await(db.collection("users").document(testUserId).delete());
        }

    }
    /** US: back button that will navigate back to admin main panel screen. */
    @Test
    public void testBackButtonNavigatestoAdminMainScreen() {
        onView(withId(R.id.back_button)).check(matches(isDisplayed()));
        onView(withId(R.id.back_button)).perform(click());
    }

    /** US: search bar and confirm button is shown. */
    @Test
    public void testSearchIsDisplayed() {
        onView(withId(R.id.search_input_bar)).check(matches(isDisplayed()));
        onView(withId(R.id.search_icon)).check(matches(isDisplayed()));
    }

    /** US: complete search test */
    @Test
    public void testSearchButtoncompletetest() {
        onView(withId(R.id.search_input_bar)).perform(typeText("test1"));
        onView(withId(R.id.search_icon)).perform(click());
        onView(withText("test1")).check(matches(isDisplayed()));
    }

    /** US: check the profile list is shown. */
    @Test
    public void testprofileListIsDisplayed() {
        onView(withId(R.id.profile_list_view)).check(matches(isDisplayed()));
    }


    /** US: test search and view detail button. */
    //reference adminBrowseprofilesintenttest
    @Test
    public void testSearchFunctionality() {
        // Type a name into the search bar created in the UI
        onView(withId(R.id.search_input_bar)).perform(typeText("test1"));
        onView(withId(R.id.search_icon)).perform(click());

        // Check if the list filters correctly
        onData(anything())
                .inAdapterView(withId(R.id.profile_list_view))
                .atPosition(0)
                .onChildView(withId(R.id.profile_name))
                .check(matches(withText("test1")));
    }



    /** US: test search and detail button. */
    //reference AdminBrowseProfileIntentTest
    @Test
    public void testDeleteButtonExists() {
        // Verify the 'X' button is visible in the first row
        onData(anything())
                .inAdapterView(withId(R.id.profile_list_view))
                .atPosition(0)
                .onChildView(withId(R.id.delete_profile_button))
                .check(matches(isDisplayed()));
    }

}

