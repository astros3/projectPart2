package com.example.eventlottery;


import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
import androidx.core.app.NotificationCompat;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.firestore.DocumentChange;

import androidx.annotation.NonNull;

import androidx.activity.result.ActivityResultLauncher;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


/**
 * Entrant home: lists all events from Firestore, shows pending/accepted counts for current
 * user. QR scan opens EventDetailsActivity. Bell opens EntrantNotificationsActivity.
 */
public class EntrantMainScreenActivity extends AppCompatActivity {

    //scanner code
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

    private ArrayList<Event> eventlist;
    private ArrayList<String[]> userstatuseventlist;
    private EntrantMainScreenAdapter eventadapter;
    //xml variables
    private ListView events;
    private ImageView notificationbellbutton;
    private TextView filtertext;

    private TextView totalnumber;
    private TextView winnumber;
    private TextView pendingnumber;
    private TextView invitationnumber;
    private LinearLayout navigationscanbutton;
    private LinearLayout navigationhistorybutton;
    private LinearLayout navigationprofilebutton;

    private FirebaseFirestore db;
    private String currentdeviceid;


    /**
     *this runs when the screen opens
     * @param savedInstanceState *
     *it sets up the views, adapter, firestore, and click listeners
     */
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

        totalnumber = findViewById(R.id.total_number);
        winnumber = findViewById(R.id.win_number);
        pendingnumber = findViewById(R.id.pending_number);
        invitationnumber = findViewById(R.id.invitation_number);

        navigationscanbutton = findViewById(R.id.navigation_scan_button);
        navigationhistorybutton = findViewById(R.id.navigation_history_button);
        navigationprofilebutton = findViewById(R.id.navigation_profile_button);

        //setting up the adapter
        eventlist = new ArrayList<>();
        eventadapter = new EntrantMainScreenAdapter(this, eventlist);
        events.setAdapter(eventadapter);


        //set up firestore and current device id
        db = FirebaseFirestore.getInstance();
        currentdeviceid = DeviceIdManager.getDeviceId(this);
        eventlist.clear();

        //reference Get all documents in a collection: https://firebase.google.com/docs/firestore/query-data/get-data#java_4
        //get all event data from firestore
        db.collection("events")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    /**
                     *this runs after firestore finishes getting all events
                     * @param task *
                     *it puts all event info into the list on the main screen
                     */
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()) {
                            QuerySnapshot alleventsdata = task.getResult();
                            int totaleventcount = alleventsdata.size();
                            if(totaleventcount == 0) {
                                listisempty();
                                return;
                            }
                            for(QueryDocumentSnapshot eachevent : alleventsdata) {
                                String thecurrenteventid = eachevent.getId();
                                String thecurrenteventtitle = eachevent.getString("title");
                                String thecurrenteventlocation = eachevent.getString("location");
                                String thecurrenteventposteruri = eachevent.getString("posterUri");
                                String thecurrenteventorganizername = eachevent.getString("organizerName");
                                Long thecurrenteventdateandtime = eachevent.getLong("eventDateMillis");

                                String thecurrenteventdescription = eachevent.getString("description");
                                String thecurrenteventorganizerid = eachevent.getString("organizerId");
                                int thecurrenteventcapacity = eachevent.getLong("capacity").intValue();
                                int thecurrentwaitinglistlimit = eachevent.getLong("waitingListLimit").intValue();
                                Long theregistrationstart = eachevent.getLong("registrationStartMillis");
                                Long theregistrationend = eachevent.getLong("registrationEndMillis");
                                Boolean thecurrentgeolocationrequired = eachevent.getBoolean("geolocationRequired");
                                Double thecurrentprice = eachevent.getDouble("price");
                                Date currenteventdate = new Date(thecurrenteventdateandtime);
                                Organizer thecurrentorganizer = new Organizer(thecurrenteventorganizerid, thecurrenteventorganizername);
                                Event thecurrentevent = new Event(thecurrenteventid, thecurrenteventtitle, thecurrenteventdescription, thecurrenteventlocation, thecurrenteventorganizerid, thecurrenteventorganizername, thecurrenteventcapacity, thecurrentwaitinglistlimit, theregistrationstart, theregistrationend, thecurrenteventdateandtime, thecurrentgeolocationrequired, thecurrentprice);
                                thecurrentevent.setPosterUri(thecurrenteventposteruri);
                                eventlist.add(thecurrentevent);
                            }

                            eventadapter.notifyDataSetChanged();
                            totalnumber.setText(String.valueOf(eventlist.size()));
                        }
                        else{
                            Log.d("EntrantMainScreen", "Error getting documents: ", task.getException());
                        }
                    }
                });

        //this list is used to keep event id and current user's status for that event
        userstatuseventlist = new ArrayList<>();
        userstatuseventlist.clear();
        //reference Get all documents in a collection: https://firebase.google.com/docs/firestore/query-data/get-data#java_4
        //get all events from firestore
        db.collection("events")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    /**
                     * this runs after firestore gets all event docs
                     * @param task *
                     * then it checks the current user's application status in each waiting list
                     */
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            QuerySnapshot alleventsdata = task.getResult();
                            final int[] currenteventindex = {0};
                            int totaleventcount = alleventsdata.size();
                            //if there are no events, just count total number as 0
                            if(totaleventcount==0){
                                CountAndCountHowManyPendingAndWin();
                                return;
                            }
                            //go through every event in firestore
                            for(QueryDocumentSnapshot eachevent : alleventsdata) {
                                String eventid = eachevent.getId();
                                db.collection("events")
                                        .document(eventid)
                                        .collection("waitingList")
                                        .document(currentdeviceid)
                                        .get()
                                        .addOnSuccessListener(waitinglistdocument ->{
                                            if(waitinglistdocument.exists()){
                                                String thecurrenteventstatusforuser = waitinglistdocument.getString("status");
                                                String[] eventuserrelated = new String[2];
                                                eventuserrelated[0] = eventid;
                                                eventuserrelated[1] = thecurrenteventstatusforuser;
                                                userstatuseventlist.add(eventuserrelated);
                                            }
                                            currenteventindex[0]++;
                                            if (currenteventindex[0] == totaleventcount) {
                                                CountAndCountHowManyPendingAndWin();
                                            }
                                        })
                                        .addOnFailureListener(e -> {
                                            currenteventindex[0]++;
                                            if (currenteventindex[0] == totaleventcount) {
                                                CountAndCountHowManyPendingAndWin();
                                            }
                                        });
                            }
                        }
                        else{
                            Log.d("EntrantMainScreen", "Error getting documents: ", task.getException());
                        }
                    }
                });



        notificationbellbutton.setOnClickListener(v ->
                startActivity(new Intent(EntrantMainScreenActivity.this, EntrantNotificationsActivity.class)));

        //navigates to filtering activity
        //!!!!awaiting!!!!
        filtertext.setOnClickListener(v -> {
            //Intent intent = new Intent(EntrantMainScreenActivity.this, //destination.class);
            //startActivity(intent);
        });

        //launch QR scanner
        navigationscanbutton.setOnClickListener(v -> {
            ScanOptions options = new ScanOptions();
            options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
            options.setPrompt("Scan event QR code");
            qrScanner.launch(options);
        });

        //navigates to history activity
        navigationhistorybutton.setOnClickListener(v -> {
            Intent intent = new Intent(EntrantMainScreenActivity.this, EntrantHistoryScreenActivity.class);
            startActivity(intent);
        });

        //navigates to profile activity
        navigationprofilebutton.setOnClickListener(v -> {
            Intent intent = new Intent(EntrantMainScreenActivity.this, EntrantProfileActivity.class);
            startActivity(intent);
        });


    }
    /**
     * this is used when there are no events from database
     * it shows 0 for total events
     */
    public void listisempty(){
        eventadapter.notifyDataSetChanged();
        totalnumber.setText("0");
    }

    /**
     * Counts pending, accepted, and invitation (SELECTED) statuses for the current user,
     * builds eventId->status map for the adapter, and updates the summary and list.
     */
    public void CountAndCountHowManyPendingAndWin(){
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
                } else if (currenteventstatusforuser.equalsIgnoreCase("selected")) {
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

    @Override
    protected void onResume() {
        super.onResume();
        refreshUserStatuses();
    }

    /** Re-fetches current user's status per event so invitation count and list stay in sync (e.g. after accept/decline). */
    private void refreshUserStatuses() {
        if (db == null || currentdeviceid == null || eventlist == null || eventlist.isEmpty()) return;
        userstatuseventlist.clear();
        int totaleventcount = eventlist.size();
        final int[] done = {0};
        for (Event event : eventlist) {
            String eventid = event.getEventId();
            db.collection("events").document(eventid)
                    .collection("waitingList").document(currentdeviceid)
                    .get()
                    .addOnSuccessListener(waitinglistdocument -> {
                        if (waitinglistdocument.exists()) {
                            String status = waitinglistdocument.getString("status");
                            userstatuseventlist.add(new String[]{eventid, status});
                        }
                        done[0]++;
                        if (done[0] == totaleventcount) CountAndCountHowManyPendingAndWin();
                    })
                    .addOnFailureListener(e -> {
                        done[0]++;
                        if (done[0] == totaleventcount) CountAndCountHowManyPendingAndWin();
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
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}