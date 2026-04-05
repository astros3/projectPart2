package com.example.eventlottery;

import android.widget.FrameLayout;

import androidx.fragment.app.FragmentActivity;
import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

// This test verifies that selected entrants are displayed with a visible delete button,
// ensuring that organizers have the ability to cancel selected entrants through the UI.

@RunWith(AndroidJUnit4.class)
public class SelectedEntryAdapterTest {

    @Test
    public void selectedEntry_deleteButtonDisplayed() {
        ActivityScenario<MainActivity> scenario =
                ActivityScenario.launch(MainActivity.class);

        scenario.onActivity(activity -> {
            ArrayList<WaitingListEntry> entries = new ArrayList<>();
            WaitingListEntry entry = new WaitingListEntry("device123", WaitingListEntry.Status.SELECTED);
            entries.add(entry);

            SelectedEntryAdapter adapter = new SelectedEntryAdapter(
                    activity,
                    entries,
                    selectedEntry -> { }
            );

            FrameLayout parent = new FrameLayout(activity);

            android.view.View rowView = adapter.getView(0, null, parent);

            android.widget.TextView textEntrantName = rowView.findViewById(R.id.textEntrantName);
            android.widget.ImageView buttonDelete = rowView.findViewById(R.id.buttonDelete);

            assertNotNull(textEntrantName);
            assertNotNull(buttonDelete);
            assertEquals(android.view.View.VISIBLE, buttonDelete.getVisibility());
        });
    }
}