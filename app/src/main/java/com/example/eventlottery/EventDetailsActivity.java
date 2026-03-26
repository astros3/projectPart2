package com.example.eventlottery;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Event details and join/leave waiting list (US 01.01.01, 01.01.02, 01.06.02). Enforces
 * registration window and optional waiting list limit before allowing join.
 * When event requires geolocation, captures entrant location once when joining.
 * Handles private event invitations: accept joins waiting list as PENDING,
 * decline sets status to DECLINED (US 01.05.06, 01.05.07).
 */
public class EventDetailsActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "event_id";
    /** When true, toolbar is white (entrant view); when false, black (organizer view). */
    public static final String EXTRA_VIEW_AS_ENTRANT = "view_as_entrant";

    private static final String TAG = "EventDetails";
    private static final int REQUEST_LOCATION_FOR_JOIN = 1001;

    private FirebaseFirestore db;
    private FusedLocationProviderClient fusedLocationClient;
    private String eventId;
    private String deviceId;
    private boolean onWaitingList;
    private String waitingListStatus;
    private Event pendingJoinEvent;

    private TextView titleView, organizerView, dateView, statusView,
            descriptionView, criteriaView, waitingListCountView;
    private ImageView posterView;
    private MaterialButton joinLeaveButton;
    private ListView commentsListView;
    private EditText commentInput;
    private Button postCommentButton;
    private LinearLayout invitationButtonsContainer;
    private MaterialButton acceptInvitationButton;
    private MaterialButton declineInvitationButton;
    private boolean organizermode = false;
    private final ArrayList<String> comments = new ArrayList<>();
    private final ArrayList<String> commentIds = new ArrayList<>();
    private ArrayAdapter<String> commentsAdapter;

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
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
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
        titleView            = findViewById(R.id.event_title);
        organizerView        = findViewById(R.id.event_organizer);
        dateView             = findViewById(R.id.event_date);
        statusView           = findViewById(R.id.event_status);
        descriptionView      = findViewById(R.id.event_description);
        criteriaView         = findViewById(R.id.event_selection_criteria);
        waitingListCountView = findViewById(R.id.event_waiting_list_count);
        posterView           = findViewById(R.id.event_poster);
        commentsListView     = findViewById(R.id.listComments);
        commentInput         = findViewById(R.id.editComment);
        postCommentButton    = findViewById(R.id.buttonPostComment);
        postCommentButton.setOnClickListener(v -> postComment());

        joinLeaveButton = findViewById(R.id.btn_join_leave);
        joinLeaveButton.setOnClickListener(v -> onJoinLeaveClicked());

        commentsAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, comments);
        commentsListView.setAdapter(commentsAdapter);

        invitationButtonsContainer = findViewById(R.id.invitation_buttons_container);
        acceptInvitationButton     = findViewById(R.id.btn_accept_invitation);
        declineInvitationButton    = findViewById(R.id.btn_decline_invitation);
        acceptInvitationButton.setOnClickListener(v ->
                updateInvitationStatus(WaitingListEntry.Status.ACCEPTED));
        declineInvitationButton.setOnClickListener(v ->
                updateInvitationStatus(WaitingListEntry.Status.DECLINED));

        commentsListView.setOnItemClickListener((parent, view, position, id) -> {
            if (organizermode) {
                String selectedCommentId = commentIds.get(position);
                new androidx.fragment.app.DialogFragment() {
                    @Override
                    public android.app.Dialog onCreateDialog(Bundle savedInstanceState) {
                        androidx.appcompat.app.AlertDialog.Builder builder =
                                new androidx.appcompat.app.AlertDialog.Builder(getActivity());
                        builder.setMessage("Do you want to delete this comment?(No Reverse)")
                                .setPositiveButton("Yes", (dialog, id) ->
                                        organizerdeleteComment(selectedCommentId))
                                .setNegativeButton("No", (dialog, id) ->
                                        dialog.dismiss());
                        return builder.create();
                    }
                }.show(getSupportFragmentManager(), "DELETE_COMMENT_DIALOG");
            }
        });
    }

    private void loadEventAndWaitingStatus() {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(this::onEventLoaded)
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load event", e);
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

        boolean currentlyViewingAsEntrant =
                getIntent().getBooleanExtra(EXTRA_VIEW_AS_ENTRANT, true);
        organizermode = !currentlyViewingAsEntrant;
        loadComments();
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
        titleView.setText(event.getTitle() != null
                ? event.getTitle() : getString(R.string.event_details));
        organizerView.setText(event.getOrganizerName() != null
                && !event.getOrganizerName().isEmpty()
                ? "By " + event.getOrganizerName() : "By Organizer");
        if (event.getEventDateMillis() > 0) {
            dateView.setText(formatDate(event.getEventDateMillis()));
            dateView.setVisibility(View.VISIBLE);
        } else {
            dateView.setVisibility(View.GONE);
        }
        descriptionView.setText(event.getDescription() != null
                && !event.getDescription().isEmpty()
                ? event.getDescription()
                : "Description description description......");

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
        return new SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
                .format(new Date(millis));
    }

    private void updateStatusAndButton() {
        if (!onWaitingList) {
            statusView.setText(R.string.status_not_joined);
            joinLeaveButton.setVisibility(View.VISIBLE);
            if (invitationButtonsContainer != null)
                invitationButtonsContainer.setVisibility(View.GONE);
            joinLeaveButton.setText(R.string.request_to_join);
            return;
        }

        if (waitingListStatus == null || waitingListStatus.isEmpty()) {
            statusView.setText(R.string.status_pending);
            joinLeaveButton.setVisibility(View.VISIBLE);
            if (invitationButtonsContainer != null)
                invitationButtonsContainer.setVisibility(View.GONE);
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
                acceptInvitationButton.setText("Accept");
                declineInvitationButton.setText("Decline");
                break;

            case "INVITED":
                // US 01.05.06 / 01.05.07 — invited to join private event waiting list
                statusView.setText("Invited to Private Event 🔒");
                joinLeaveButton.setVisibility(View.GONE);
                invitationButtonsContainer.setVisibility(View.VISIBLE);
                acceptInvitationButton.setText("Accept Invitation");
                declineInvitationButton.setText("Decline Invitation");
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

    /**
     * Updates invitation status to ACCEPTED or DECLINED.
     * Handles both lottery selected (SELECTED -> ACCEPTED/DECLINED)
     * and private event invitations (INVITED -> PENDING on accept, DECLINED on decline).
     * US 01.05.02, 01.05.03, 01.05.07.
     */
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
                        Toast.makeText(this, "Invitation record not found",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    WaitingListEntry entry =
                            documentSnapshot.toObject(WaitingListEntry.class);
                    if (entry == null) {
                        Toast.makeText(this, "Could not read invitation",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String currentStatus = entry.getStatus();

                    // Valid statuses that can be responded to
                    boolean isSelected = WaitingListEntry.Status.SELECTED.name()
                            .equals(currentStatus);
                    boolean isInvited  = WaitingListEntry.Status.INVITED.name()
                            .equals(currentStatus);

                    if (!isSelected && !isInvited) {
                        Toast.makeText(this, "This invitation is no longer active",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // US 01.05.07 — accepting a private invite adds them to
                    // the waiting list as PENDING
                    String statusToSave;
                    if (isInvited && newStatus == WaitingListEntry.Status.ACCEPTED) {
                        statusToSave = WaitingListEntry.Status.PENDING.name();
                    } else {
                        statusToSave = newStatus.name();
                    }

                    db.collection("events")
                            .document(eventId)
                            .collection("waitingList")
                            .document(deviceId)
                            .update("status", statusToSave)
                            .addOnSuccessListener(unused -> {
                                String message;
                                if (newStatus == WaitingListEntry.Status.ACCEPTED) {
                                    message = isInvited
                                            ? "You've joined the waiting list!"
                                            : "Registration confirmed!";
                                } else {
                                    message = "Invitation declined.";
                                }
                                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
                                waitingListStatus = statusToSave;
                                updateStatusAndButton();
                                refreshWaitingListCount();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(this, "Failed to update invitation",
                                            Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load invitation",
                                Toast.LENGTH_SHORT).show());
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
                        Toast.makeText(this, "Registration is closed for this event",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (event.isGeolocationRequired()) {
                        captureLocationOnceThenAddToWaitingList(event);
                    } else {
                        checkLimitThenAddToWaitingList(event);
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load event for join", e);
                    Toast.makeText(this, "Failed to load event", Toast.LENGTH_SHORT).show();
                });
    }

    private void captureLocationOnceThenAddToWaitingList(Event event) {
        pendingJoinEvent = event;

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    REQUEST_LOCATION_FOR_JOIN
            );
            return;
        }

        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        updateUserProfileWithLocationOnce(
                                location.getLatitude(),
                                location.getLongitude(),
                                () -> checkLimitThenAddToWaitingList(event)
                        );
                    } else {
                        Toast.makeText(this,
                                getString(R.string.location_unavailable_joining_without),
                                Toast.LENGTH_SHORT).show();
                        checkLimitThenAddToWaitingList(event);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            getString(R.string.location_unavailable_joining_without),
                            Toast.LENGTH_SHORT).show();
                    checkLimitThenAddToWaitingList(event);
                });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_FOR_JOIN && pendingJoinEvent != null) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureLocationOnceThenAddToWaitingList(pendingJoinEvent);
            } else {
                Toast.makeText(this,
                        getString(R.string.location_unavailable_joining_without),
                        Toast.LENGTH_SHORT).show();
                checkLimitThenAddToWaitingList(pendingJoinEvent);
            }
            pendingJoinEvent = null;
        }
    }

    private void checkLimitThenAddToWaitingList(Event event) {
        int limit = event.getWaitingListLimit();
        if (limit > 0) {
            db.collection("events").document(eventId)
                    .collection("waitingList").get()
                    .addOnSuccessListener(waitingSnapshot -> {
                        int currentCount = waitingSnapshot.size();
                        if (currentCount >= limit) {
                            Toast.makeText(this,
                                    getString(R.string.waiting_list_full),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        addToWaitingList();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to check waiting list",
                                    Toast.LENGTH_SHORT).show());
        } else {
            addToWaitingList();
        }
    }

    private void updateUserProfileWithLocationOnce(double latitude, double longitude,
                                                   Runnable onDone) {
        db.collection("users").document(deviceId).get()
                .addOnSuccessListener(doc -> {
                    Entrant entrant = doc.exists() && doc.toObject(Entrant.class) != null
                            ? doc.toObject(Entrant.class)
                            : new Entrant(deviceId, "", "", "", "entrant");

                    if (entrant == null) {
                        entrant = new Entrant(deviceId, "", "", "", "entrant");
                    }

                    entrant.setLatitude(latitude);
                    entrant.setLongitude(longitude);

                    Entrant finalEntrant = entrant;
                    db.collection("users").document(deviceId).set(finalEntrant)
                            .addOnSuccessListener(unused -> {
                                onDone.run();
                                new Thread(() -> {
                                    try {
                                        Geocoder geocoder =
                                                new Geocoder(this, Locale.getDefault());
                                        List<Address> list =
                                                geocoder.getFromLocation(latitude, longitude, 1);
                                        if (list != null && !list.isEmpty()
                                                && list.get(0).getAddressLine(0) != null) {
                                            String addr = list.get(0).getAddressLine(0);
                                            db.collection("users").document(deviceId)
                                                    .update("locationAddress", addr);
                                        }
                                    } catch (Exception e) {
                                        Log.e(TAG, "Geocode for join", e);
                                    }
                                }).start();
                            })
                            .addOnFailureListener(e -> onDone.run());
                })
                .addOnFailureListener(e -> onDone.run());
    }

    /** Adds the current user to the event waiting list as PENDING. */
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
                    Toast.makeText(this, "You have joined the waiting list",
                            Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(this, "You have left the waiting list",
                            Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to leave", Toast.LENGTH_SHORT).show());
    }

    private void loadComments() {
        if (eventId == null || eventId.isEmpty()) return;

        db.collection("events")
                .document(eventId)
                .collection("comments")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    comments.clear();
                    commentIds.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        String text = doc.getString("text");
                        if (text != null) {
                            comments.add(text);
                            commentIds.add(doc.getId());
                        }
                    }
                    commentsAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load comments",
                                Toast.LENGTH_SHORT).show());
    }

    private void postComment() {
        String text = commentInput.getText().toString().trim();
        if (text.isEmpty()) {
            Toast.makeText(this, "Enter a comment", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> comment = new HashMap<>();
        comment.put("text", text);
        comment.put("deviceId", deviceId);
        comment.put("timestamp", System.currentTimeMillis());

        db.collection("events")
                .document(eventId)
                .collection("comments")
                .add(comment)
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Comment posted", Toast.LENGTH_SHORT).show();
                    commentInput.setText("");
                    loadComments();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to post comment",
                                Toast.LENGTH_SHORT).show());
    }

    private void organizerdeleteComment(String commentIdToDelete) {
        db.collection("events")
                .document(eventId)
                .collection("comments")
                .document(commentIdToDelete)
                .delete()
                .addOnSuccessListener(docRef -> {
                    Toast.makeText(this, "Comment deleted", Toast.LENGTH_SHORT).show();
                    loadComments();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to delete comment",
                                Toast.LENGTH_SHORT).show());
    }
}