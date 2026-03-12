package com.example.eventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.example.eventlottery.DeviceIdManager;
import com.google.firebase.firestore.DocumentSnapshot;

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


/**
 * US 01.01.03
 * this is the main screen for entrant
 * it shows all events and also shows some user stats
 * reference Lab7 format
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

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        events = findViewById(R.id.Events);
        notificationbellbutton = findViewById(R.id.notification_Bell_Button);
        filtertext = findViewById(R.id.filter_text);

        totalnumber = findViewById(R.id.total_number);
        winnumber = findViewById(R.id.win_number);
        pendingnumber = findViewById(R.id.pending_number);

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
                                        });
                            }
                        }
                        else{
                            Log.d("EntrantMainScreen", "Error getting documents: ", task.getException());
                        }
                    }
                });



        //navigates to notification activity
        //notification activity not yet implemented
        notificationbellbutton.setOnClickListener(v -> {
            //Intent intent = new Intent(EntrantMainScreenActivity.this, //destination.class);
            //startActivity(intent);
        });

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
     * this function counts how many events are pending and how many are accepted for the current user
     * once get the value, will set it on diplay
     */
    public void CountAndCountHowManyPendingAndWin(){
        int howmanypendingforthisuser =0;
        int howmanywinforthisuser = 0;
        for(String[] eventuserrelated : userstatuseventlist){
            String currenteventstatusforuser = eventuserrelated[1];

            if ((currenteventstatusforuser != null)&&(currenteventstatusforuser.equalsIgnoreCase("accepted"))){
                howmanywinforthisuser++;
            }

            if ((currenteventstatusforuser != null)&&(currenteventstatusforuser.equalsIgnoreCase("pending"))){
                howmanypendingforthisuser++;
            }

        }
        winnumber.setText(String.valueOf(howmanywinforthisuser));
        pendingnumber.setText(String.valueOf(howmanypendingforthisuser));
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}