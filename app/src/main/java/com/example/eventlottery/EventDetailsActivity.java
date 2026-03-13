package com.example.eventlottery;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.ImageView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Event details and join/leave waiting list (US 01.01.01, 01.01.02, 01.06.02). Enforces
 * registration window and optional waiting list limit before allowing join.
 */
public class EventDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "event_id";
    /** When true, toolbar is white (entrant view); when false, black (organizer view). */
    public static final String EXTRA_VIEW_AS_ENTRANT = "view_as_entrant";

    private FirebaseFirestore db;
    private String eventId;
    private String deviceId;
    private boolean onWaitingList;
    private String waitingListStatus;

    private TextView titleView, organizerView, dateView, statusView, descriptionView, criteriaView, waitingListCountView;
    private ImageView posterView;
    private MaterialButton joinLeaveButton;
    private LinearLayout invitationButtonsContainer;
    private MaterialButton acceptInvitationButton;
    private MaterialButton declineInvitationButton;

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
        posterView = findViewById(R.id.event_poster);
        joinLeaveButton = findViewById(R.id.btn_join_leave);
        joinLeaveButton.setOnClickListener(v -> onJoinLeaveClicked());
        invitationButtonsContainer = findViewById(R.id.invitation_buttons_container);
        acceptInvitationButton = findViewById(R.id.btn_accept_invitation);
        declineInvitationButton = findViewById(R.id.btn_decline_invitation);
        acceptInvitationButton.setOnClickListener(v -> updateInvitationStatus(WaitingListEntry.Status.ACCEPTED));
        declineInvitationButton.setOnClickListener(v -> updateInvitationStatus(WaitingListEntry.Status.DECLINED));
    }

    private void loadEventAndWaitingStatus() {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(this::onEventLoaded)
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Failed to load event", e);  // full stack trace
                    e.printStackTrace(); // prints stack trace in Logcat

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
                .collection("waitingList")
                .document(deviceId)
                .get()
                .addOnSuccessListener(doc -> {
                    onWaitingList = doc != null && doc.exists();

                    if (onWaitingList) {
                        waitingListStatus = doc.getString("status");
                    } else {
                        waitingListStatus = null;
                    }

                    updateStatusAndButton();
                });

        refreshWaitingListCount();
    }

    private void refreshWaitingListCount() {
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
        String posterUri = event.getPosterUri();
        if (posterUri != null && !posterUri.isEmpty()) {
            Glide.with(this).load(posterUri).centerCrop().into(posterView);
        } else {
            posterView.setImageDrawable(null);
        }
    }

    private static String formatDate(long millis) {
        return new SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(new Date(millis));
    }

    private void updateStatusAndButton() {
        if (!onWaitingList) {
            statusView.setText(R.string.status_not_joined);
            joinLeaveButton.setVisibility(View.VISIBLE);
            if (invitationButtonsContainer != null) invitationButtonsContainer.setVisibility(View.GONE);
            joinLeaveButton.setText(R.string.request_to_join);
            return;
        }

        if (waitingListStatus == null || waitingListStatus.isEmpty()) {
            statusView.setText(R.string.status_pending);
            joinLeaveButton.setVisibility(View.VISIBLE);
            if (invitationButtonsContainer != null) invitationButtonsContainer.setVisibility(View.GONE);
            joinLeaveButton.setText(R.string.leave_waiting_list);
            return;
        }

        switch (waitingListStatus) {
            case "PENDING":
                statusView.setText(R.string.status_pending);
                joinLeaveButton.setVisibility(View.VISIBLE);
                invitationButtonsContainer.setVisibility(View.GONE);
                joinLeaveButton.setText(R.string.leave_waiting_list);
                break;

            case "SELECTED":
                statusView.setText("Selected");
                joinLeaveButton.setVisibility(View.GONE);
                invitationButtonsContainer.setVisibility(View.VISIBLE);
                break;

            case "ACCEPTED":
                statusView.setText("Accepted");
                joinLeaveButton.setVisibility(View.VISIBLE);
                invitationButtonsContainer.setVisibility(View.GONE);
                joinLeaveButton.setText(R.string.leave_selected_list);
                break;

            case "DECLINED":
                statusView.setText("Declined");
                joinLeaveButton.setVisibility(View.VISIBLE);
                invitationButtonsContainer.setVisibility(View.GONE);
                joinLeaveButton.setText(R.string.request_to_join);
                break;

            case "CANCELLED":
                statusView.setText("Cancelled");
                joinLeaveButton.setVisibility(View.VISIBLE);
                invitationButtonsContainer.setVisibility(View.GONE);
                joinLeaveButton.setText(R.string.request_to_join);
                break;

            case "WAITING":
                statusView.setText("Waiting");
                joinLeaveButton.setVisibility(View.VISIBLE);
                invitationButtonsContainer.setVisibility(View.GONE);
                joinLeaveButton.setText(R.string.leave_waiting_list);
                break;

            default:
                statusView.setText(waitingListStatus);
                joinLeaveButton.setVisibility(View.VISIBLE);
                invitationButtonsContainer.setVisibility(View.GONE);
                joinLeaveButton.setText(R.string.leave_waiting_list);
                break;
        }
    }

    /** Updates invitation status to ACCEPTED or DECLINED when current status is SELECTED. */
    private void updateInvitationStatus(WaitingListEntry.Status newStatus) {
        if (eventId == null || eventId.trim().isEmpty()) {
            Toast.makeText(this, "No event selected", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "Invitation record not found", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    WaitingListEntry entry = documentSnapshot.toObject(WaitingListEntry.class);
                    if (entry == null) {
                        Toast.makeText(this, "Could not read invitation", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    String currentStatus = entry.getStatus();
                    if (!WaitingListEntry.Status.SELECTED.name().equals(currentStatus)) {
                        Toast.makeText(this, "This invitation is no longer active", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    db.collection("events")
                            .document(eventId)
                            .collection("waitingList")
                            .document(deviceId)
                            .update("status", newStatus.name())
                            .addOnSuccessListener(unused -> {
                                String message = (newStatus == WaitingListEntry.Status.ACCEPTED)
                                        ? "Registration confirmed!"
                                        : "Invitation declined.";
                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                                waitingListStatus = newStatus.name();
                                updateStatusAndButton();
                                refreshWaitingListCount();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to update invitation", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load invitation", Toast.LENGTH_SHORT).show());
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
                    if (event == null) {
                        Toast.makeText(this, "Could not load event", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!event.isRegistrationOpen()) {
                        Toast.makeText(this, "Registration is closed for this event", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int limit = event.getWaitingListLimit();
                    if (limit > 0) {
                        // Enforce optional waiting list limit: check current count before allowing join
                        db.collection("events").document(eventId).collection("waitingList").get()
                                .addOnSuccessListener(waitingSnapshot -> {
                                    int currentCount = waitingSnapshot.size();
                                    if (currentCount >= limit) {
                                        Toast.makeText(this, getString(R.string.waiting_list_full), Toast.LENGTH_SHORT).show();
                                        return;
                                    }
                                    addToWaitingList();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Failed to check waiting list", Toast.LENGTH_SHORT).show());
                    } else {
                        addToWaitingList();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load event", Toast.LENGTH_SHORT).show());
    }

    /** Adds the current user to the event waiting list. Call after registration open and limit checks. */
    private void addToWaitingList() {
        WaitingListEntry entry =
                new WaitingListEntry(deviceId, WaitingListEntry.Status.PENDING);

        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .document(deviceId)
                .set(entry)
                .addOnSuccessListener(aVoid -> {
                    onWaitingList = true;
                    waitingListStatus = WaitingListEntry.Status.PENDING.name();
                    updateStatusAndButton();
                    refreshWaitingListCount();
                    Toast.makeText(this, "You have joined the waiting list", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to join", Toast.LENGTH_SHORT).show());
    }

    private void leaveWaitingList() {
        db.collection("events").document(eventId)
                .collection("waitingList").document(deviceId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    onWaitingList = false;
                    waitingListStatus = null;
                    updateStatusAndButton();
                    refreshWaitingListCount();
                    Toast.makeText(this, "You have left the waiting list", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to leave", Toast.LENGTH_SHORT).show());
    }
}
