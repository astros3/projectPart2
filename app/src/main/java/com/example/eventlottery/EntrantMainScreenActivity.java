package com.example.eventlottery;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.firebase.firestore.DocumentChange;

import androidx.annotation.NonNull;

import androidx.activity.result.ActivityResultLauncher;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Entrant home: lists events from Firestore with optional filters (keyword, type, distance,
 * registration open). Status counts use the full event set. Map tab opens {@link EntrantMapActivity}
 * with the same filter criteria.
 */
public class EntrantMainScreenActivity extends BaseActivity {

    private final ActivityResultLauncher<ScanOptions> qrScanner =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    String scannedValue = result.getContents().trim();
                    if (scannedValue.contains("/")) {
                        scannedValue = scannedValue.substring(scannedValue.lastIndexOf("/") + 1);
                    }
                    Intent intent = new Intent(EntrantMainScreenActivity.this, EventDetailsActivity.class);
                    intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, scannedValue);
                    startActivity(intent);
                }
            });

    private final ArrayList<Event> allEvents = new ArrayList<>();
    private final ArrayList<Event> eventlist = new ArrayList<>();
    private ArrayList<String[]> userstatuseventlist;
    private EntrantMainScreenAdapter eventadapter;

    private ListView events;
    private ImageView notificationbellbutton;
    private TextView filtertext;
    private ImageView filterimage;

    private TextView totalnumber;
    private TextView winnumber;
    private TextView pendingnumber;
    private TextView invitationnumber;
    private LinearLayout navigationscanbutton;
    private LinearLayout navigationmapbutton;
    private LinearLayout navigationhistorybutton;
    private LinearLayout navigationprofilebutton;

    private FirebaseFirestore db;
    private String currentdeviceid;
    private EventFilterCriteria currentFilter = EventFilterCriteria.empty();
    private ListenerRegistration notificationListener;
    private long listenerAttachTime;
    private FusedLocationProviderClient fusedLocationClient;
    private Double lastUserLat;
    private Double lastUserLng;
    /** Event IDs where the current user has any waiting list entry (used to reveal private events). */
    private final Set<String> userWaitingListEventIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_main_screen);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        findViewById(R.id.btn_back).setOnClickListener(v -> {
            startActivity(new Intent(this, WelcomePageActivity.class));
            finish();
        });

        events = findViewById(R.id.Events);
        notificationbellbutton = findViewById(R.id.notification_Bell_Button);
        filtertext = findViewById(R.id.filter_text);
        filterimage = findViewById(R.id.filterimage);

        totalnumber = findViewById(R.id.total_number);
        winnumber = findViewById(R.id.win_number);
        pendingnumber = findViewById(R.id.pending_number);
        invitationnumber = findViewById(R.id.invitation_number);

        navigationscanbutton = findViewById(R.id.navigation_scan_button);
        navigationmapbutton = findViewById(R.id.navigation_map_button);
        navigationhistorybutton = findViewById(R.id.navigation_history_button);
        navigationprofilebutton = findViewById(R.id.navigation_profile_button);

        eventadapter = new EntrantMainScreenAdapter(this, eventlist);
        events.setAdapter(eventadapter);

        db = FirebaseFirestore.getInstance();
        currentdeviceid = DeviceIdManager.getDeviceId(this);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        userstatuseventlist = new ArrayList<>();

        getSupportFragmentManager().setFragmentResultListener(
                EventFilterDialogFragment.REQUEST_KEY,
                this,
                (requestKey, result) -> {
                    EventFilterCriteria c;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        c = result.getSerializable(EventFilterDialogFragment.BUNDLE_CRITERIA,
                                EventFilterCriteria.class);
                    } else {
                        c = (EventFilterCriteria) result.getSerializable(
                                EventFilterDialogFragment.BUNDLE_CRITERIA);
                    }
                    if (c != null) {
                        currentFilter = c;
                        runAfterFilterChanged(false);
                    }
                });

        loadAllEventsFromFirestore();
        startNotificationListener();

        notificationbellbutton.setOnClickListener(v ->
                startActivity(new Intent(EntrantMainScreenActivity.this, EntrantNotificationsActivity.class)));

        View.OnClickListener openFilter = v -> EventFilterDialogFragment
                .newInstance(currentFilter)
                .show(getSupportFragmentManager(), "event_filter");
        filtertext.setOnClickListener(openFilter);
        filterimage.setOnClickListener(openFilter);

        navigationscanbutton.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
            options.setPrompt("Scan event QR code");
            qrScanner.launch(options);
        });

        navigationmapbutton.setOnClickListener(v ->
                startActivity(new Intent(this, EntrantMapActivity.class)
                        .putExtra(EntrantMapActivity.EXTRA_FILTER, currentFilter)));

        navigationhistorybutton.setOnClickListener(v ->
                startActivity(new Intent(EntrantMainScreenActivity.this, EntrantHistoryScreenActivity.class)));

        navigationprofilebutton.setOnClickListener(v ->
                startActivity(new Intent(EntrantMainScreenActivity.this, EntrantProfileActivity.class)));
    }

    private void loadAllEventsFromFirestore() {
        db.collection("events")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (!task.isSuccessful()) {
                            Log.d("EntrantMainScreen", "Error getting documents: ", task.getException());
                            return;
                        }
                        allEvents.clear();
                        QuerySnapshot alleventsdata = task.getResult();
                        if (alleventsdata == null || alleventsdata.isEmpty()) {
                            listisempty();
                            userstatuseventlist.clear();
                            CountAndCountHowManyPendingAndWin();
                            return;
                        }
                        for (QueryDocumentSnapshot eachevent : alleventsdata) {
                            allEvents.add(EventFirestoreParser.fromSnapshot(eachevent));
                        }
                        runAfterFilterChanged(true);
                    }
                });
    }

    private boolean needsDistanceFilter() {
        return currentFilter.getMaxDistanceKm() != null && currentFilter.getMaxDistanceKm() > 0;
    }

    /**
     * @param refetchStatuses true after loading events from Firestore; false when only filter
     *                        inputs changed (statuses already known).
     *
     * When refetchStatuses=true we must fetch statuses FIRST because private events are only
     * visible to this user when they have a waiting list entry.  The filter is then applied
     * inside {@link #fetchUserStatusesForAllEvents} once the set is ready.
     * When refetchStatuses=false the set is already populated; apply the filter directly.
     */
    private void runAfterFilterChanged(boolean refetchStatuses) {
        Runnable doWork = () -> {
            if (refetchStatuses) {
                fetchUserStatusesForAllEvents();
            } else {
                applyFilterToEventList();
            }
        };
        if (needsDistanceFilter()) {
            fetchUserLocation(doWork);
        } else {
            doWork.run();
        }
    }

    private void fetchUserLocation(@NonNull Runnable onDone) {
        boolean fine = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        if (!fine && !coarse) {
            lastUserLat = null;
            lastUserLng = null;
            if (needsDistanceFilter()) {
                Toast.makeText(this, R.string.distance_filter_needs_location, Toast.LENGTH_LONG).show();
            }
            onDone.run();
            return;
        }
        // getLastLocation() is often null on cold start; getCurrentLocation() helps distance filtering work.
        fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
            Location loc = task.isSuccessful() ? task.getResult() : null;
            if (loc != null) {
                lastUserLat = loc.getLatitude();
                lastUserLng = loc.getLongitude();
                onDone.run();
                return;
            }
            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnCompleteListener(t2 -> {
                        Location cur = t2.isSuccessful() ? t2.getResult() : null;
                        if (cur != null) {
                            lastUserLat = cur.getLatitude();
                            lastUserLng = cur.getLongitude();
                        } else {
                            lastUserLat = null;
                            lastUserLng = null;
                            if (needsDistanceFilter()) {
                                Toast.makeText(this, R.string.distance_filter_needs_location,
                                        Toast.LENGTH_LONG).show();
                            }
                        }
                        onDone.run();
                    });
        });
    }

    private void applyFilterToEventList() {
        boolean hasUserFix = lastUserLat != null && lastUserLng != null;
        eventlist.clear();
        for (Event e : allEvents) {
            // Private events are only visible to entrants who have a waiting list entry (US 01.05.06)
            if (e.isPrivate() && !userWaitingListEventIds.contains(e.getEventId())) continue;
            if (EventFilterUtils.matchesForList(e, currentFilter, lastUserLat, lastUserLng, hasUserFix)) {
                eventlist.add(e);
            }
        }
        eventadapter.notifyDataSetChanged();
        totalnumber.setText(String.valueOf(eventlist.size()));
    }

    public void listisempty() {
        eventlist.clear();
        allEvents.clear();
        eventadapter.notifyDataSetChanged();
        totalnumber.setText("0");
    }

    public void CountAndCountHowManyPendingAndWin() {
        int howmanypendingforthisuser = 0;
        int howmanywinforthisuser = 0;
        int howmanyinvitationsforthisuser = 0;
        Map<String, String> eventIdToStatus = new HashMap<>();

        for (String[] eventuserrelated : userstatuseventlist) {
            String eventId = eventuserrelated[0];
            String currenteventstatusforuser = eventuserrelated[1];
            eventIdToStatus.put(eventId, currenteventstatusforuser);

            if (currenteventstatusforuser != null) {
                if (currenteventstatusforuser.equalsIgnoreCase("accepted")) {
                    howmanywinforthisuser++;
                } else if (currenteventstatusforuser.equalsIgnoreCase("pending")) {
                    howmanypendingforthisuser++;
                } else if (currenteventstatusforuser.equalsIgnoreCase("selected")
                        || currenteventstatusforuser.equalsIgnoreCase("invited")) {
                    // SELECTED = lottery winner awaiting response
                    // INVITED  = private event invite awaiting response (US 01.05.06)
                    howmanyinvitationsforthisuser++;
                }
            }
        }

        winnumber.setText(String.valueOf(howmanywinforthisuser));
        pendingnumber.setText(String.valueOf(howmanypendingforthisuser));
        invitationnumber.setText(String.valueOf(howmanyinvitationsforthisuser));
        eventadapter.setEventIdToStatus(eventIdToStatus);
        eventadapter.notifyDataSetChanged();
    }

    private void fetchUserStatusesForAllEvents() {
        userstatuseventlist.clear();
        userWaitingListEventIds.clear();
        if (allEvents.isEmpty()) {
            applyFilterToEventList();
            CountAndCountHowManyPendingAndWin();
            return;
        }
        int totaleventcount = allEvents.size();
        final int[] done = {0};
        for (Event event : allEvents) {
            String eventid = event.getEventId();
            db.collection("events").document(eventid)
                    .collection("waitingList").document(currentdeviceid)
                    .get()
                    .addOnSuccessListener(waitinglistdocument -> {
                        if (waitinglistdocument.exists()) {
                            String status = waitinglistdocument.getString("status");
                            userstatuseventlist.add(new String[]{eventid, status});
                            // Track which events the user has any entry on so private events
                            // that they were invited to remain visible (US 01.05.06)
                            userWaitingListEventIds.add(eventid);
                        }
                        done[0]++;
                        if (done[0] == totaleventcount) {
                            applyFilterToEventList();
                            CountAndCountHowManyPendingAndWin();
                        }
                    })
                    .addOnFailureListener(e -> {
                        done[0]++;
                        if (done[0] == totaleventcount) {
                            applyFilterToEventList();
                            CountAndCountHowManyPendingAndWin();
                        }
                    });
        }
    }

    private void startInvitationListener() {
        String myId = DeviceIdManager.getDeviceId(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Listen only for unread invitations for THIS user
        db.collection("users").document(myId).collection("notifications")
                .whereEqualTo("type", "INVITATION")
                .whereEqualTo("read", false)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) return;

                    if (snapshots != null && !snapshots.isEmpty()) {
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == DocumentChange.Type.ADDED) {
                                // Trigger the actual phone alert
                                String title = dc.getDocument().getString("title");
                                String msg = dc.getDocument().getString("message");

                                triggerSystemAlert(title, msg);
                            }
                        }
                    }
                });

    }

    private void triggerSystemAlert(String title, String message) {
        String channelId = "invite_channel";
        NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // Create the Notification Channel (Required for Android 8.0+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Invitations", NotificationManager.IMPORTANCE_HIGH);
            nm.createNotificationChannel(channel);
        }

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.ic_invite) // The XML icon we created earlier
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Show it
        nm.notify((int) System.currentTimeMillis(), builder.build());
    }


    @Override
    protected void onStart() {
        super.onStart();
        startNotificationListener();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (notificationListener != null) {
            notificationListener.remove();
            notificationListener = null;
        }
    }

    /**
     * Listens for new unread notification documents on this device's Firestore subcollection.
     * When a new document arrives after we attached the listener, fires an OS notification banner.
     */
    private void startNotificationListener() {
        if (db == null || currentdeviceid == null) return;
        listenerAttachTime = System.currentTimeMillis();

        notificationListener = db.collection("users")
                .document(currentdeviceid)
                .collection("notifications")
                .whereEqualTo("read", false)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;
                    for (DocumentChange change : snapshots.getDocumentChanges()) {
                        if (change.getType() != DocumentChange.Type.ADDED) continue;
                        Long ts = change.getDocument().getLong("timestampMillis");
                        if (ts == null || ts < listenerAttachTime) continue;

                        String title = change.getDocument().getString("title");
                        String message = change.getDocument().getString("message");
                        if (title == null) title = getString(R.string.app_name);
                        if (message == null) message = "";

                        MyFirebaseMessagingService.postNotification(
                                EntrantMainScreenActivity.this,
                                title, message,
                                change.getDocument().getId().hashCode()
                        );
                    }
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUserStatuses();
    }

    private void refreshUserStatuses() {
        if (db == null || currentdeviceid == null || allEvents.isEmpty()) return;
        fetchUserStatusesForAllEvents();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
