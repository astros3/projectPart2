package com.example.eventlottery;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Event Details screen for entrants.
 * Implements US 01.01.01 (join waiting list), US 01.01.02 (leave waiting list),
 * US 01.06.02 (sign up from event details).
 */
public class EventDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "event_id";
    /** When true, toolbar is white (entrant view); when false, black (organizer view). */
    public static final String EXTRA_VIEW_AS_ENTRANT = "view_as_entrant";

    private FirebaseFirestore db;
    private String eventId;
    private String deviceId;
    private boolean onWaitingList;

    private TextView titleView, organizerView, dateView, statusView, descriptionView, criteriaView, waitingListCountView;
    private MaterialButton joinLeaveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_details);

        eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "Invalid event", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        deviceId = DeviceIdManager.getDeviceId(this);

        setupToolbar();
        bindViews();
        loadEventAndWaitingStatus();
    }

    private void setupToolbar() {
        boolean viewAsEntrant = getIntent().getBooleanExtra(EXTRA_VIEW_AS_ENTRANT, true);
        Toolbar toolbar = findViewById(R.id.toolbar_event_details);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());

        @ColorInt int toolbarBg = viewAsEntrant
                ? ContextCompat.getColor(this, android.R.color.white)
                : ContextCompat.getColor(this, R.color.black);
        @ColorInt int toolbarContent = viewAsEntrant
                ? ContextCompat.getColor(this, R.color.toolbar_content_dark)
                : ContextCompat.getColor(this, android.R.color.white);

        toolbar.setBackgroundColor(toolbarBg);
        toolbar.setTitleTextColor(toolbarContent);
        Drawable navIcon = ContextCompat.getDrawable(this, R.drawable.ic_back_arrow);
        if (navIcon != null) {
            navIcon = navIcon.mutate();
            navIcon.setColorFilter(toolbarContent, PorterDuff.Mode.SRC_IN);
            toolbar.setNavigationIcon(navIcon);
        }
    }

    private void bindViews() {
        titleView = findViewById(R.id.event_title);
        organizerView = findViewById(R.id.event_organizer);
        dateView = findViewById(R.id.event_date);
        statusView = findViewById(R.id.event_status);
        descriptionView = findViewById(R.id.event_description);
        criteriaView = findViewById(R.id.event_selection_criteria);
        waitingListCountView = findViewById(R.id.event_waiting_list_count);
        joinLeaveButton = findViewById(R.id.btn_join_leave);
        joinLeaveButton.setOnClickListener(v -> onJoinLeaveClicked());
    }

    private void loadEventAndWaitingStatus() {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(this::onEventLoaded)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Could not load event", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void onEventLoaded(@NonNull DocumentSnapshot eventDoc) {
        if (!eventDoc.exists()) {
            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        Event event = eventDoc.toObject(Event.class);
        if (event != null) {
            event.setEventId(eventDoc.getId());
            bindEvent(event);
        }

        db.collection("events").document(eventId)
                .collection("waitingList").document(deviceId).get()
                .addOnSuccessListener(doc -> {
                    onWaitingList = doc != null && doc.exists();
                    updateStatusAndButton();
                });

        //waiting list count code
        db.collection("events").document(eventId)
                .collection("waitingList")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int count = querySnapshot.size();
                    waitingListCountView.setText("Waiting List: " + count);
                });
    }

    private void bindEvent(Event event) {
        titleView.setText(event.getTitle() != null ? event.getTitle() : getString(R.string.event_details));
        organizerView.setText(event.getOrganizerName() != null && !event.getOrganizerName().isEmpty()
                ? "By " + event.getOrganizerName() : "By Organizer");
        if (event.getEventDateMillis() > 0) {
            dateView.setText(formatDate(event.getEventDateMillis()));
            dateView.setVisibility(View.VISIBLE);
        } else {
            dateView.setVisibility(View.GONE);
        }
        descriptionView.setText(event.getDescription() != null && !event.getDescription().isEmpty()
                ? event.getDescription() : "Description description description......");
        List<String> criteria = event.getSelectionCriteria();
        if (criteria != null && !criteria.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (String line : criteria) {
                if (sb.length() > 0) sb.append("\n");
                sb.append("• ").append(line);
            }
            criteriaView.setText(sb.toString());
        }
        // Poster loading can be added later (e.g. Glide with event.getPosterUri())
    }

    private static String formatDate(long millis) {
        return new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(new Date(millis));
    }

    private void updateStatusAndButton() {
        if (onWaitingList) {
            statusView.setText(R.string.status_pending);
            joinLeaveButton.setText(R.string.leave_waiting_list);
        } else {
            statusView.setText(R.string.status_not_joined);
            joinLeaveButton.setText(R.string.request_to_join);
        }
    }

    private void onJoinLeaveClicked() {
        if (onWaitingList) {
            leaveWaitingList();
        } else {
            requestToJoin();
        }
    }

    private void requestToJoin() {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(eventDoc -> {
                    if (!eventDoc.exists()) {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Event event = eventDoc.toObject(Event.class);
                    if (event != null && !event.isRegistrationOpen()) {
                        Toast.makeText(this, "Registration is closed for this event", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    WaitingListEntry entry = new WaitingListEntry(deviceId, WaitingListEntry.Status.PENDING);
                    db.collection("events").document(eventId)
                            .collection("waitingList").document(deviceId)
                            .set(entry)
                            .addOnSuccessListener(aVoid -> {
                                onWaitingList = true;
                                updateStatusAndButton();
                                Toast.makeText(this, "You have joined the waiting list", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to join", Toast.LENGTH_SHORT).show());
                });
    }

    private void leaveWaitingList() {
        db.collection("events").document(eventId)
                .collection("waitingList").document(deviceId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    onWaitingList = false;
                    updateStatusAndButton();
                    Toast.makeText(this, "You have left the waiting list", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to leave", Toast.LENGTH_SHORT).show());
    }
}
