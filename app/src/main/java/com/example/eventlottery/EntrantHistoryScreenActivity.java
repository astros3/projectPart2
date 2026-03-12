package com.example.eventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import com.example.eventlottery.DeviceIdManager;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.Date;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

/**
 * Entrant history: events this user has joined (has waitingList doc). Shows per-event status.
 * Scan nav opens QRCodeActivity (organizer screen); consider opening scanner → EventDetails instead.
 */
public class EntrantHistoryScreenActivity extends AppCompatActivity {
    private ListView eventshistory;

    private ImageView notificationbellbutton;
    private LinearLayout navigationhomebutton;
    private LinearLayout navigationscanbutton;
    private LinearLayout navigationhistorybutton;
    private LinearLayout navigationprofilebutton;
    private ArrayList<Event> entranthistoryeventlist;
    private EntrantHistoryScreenAdapter entranthistoryadapter;
    private FirebaseFirestore db;
    private String currentdeviceid;
    /**
     *this runs when the history screen is opened
     *@param savedInstanceState *
     *it sets up the views, adapter, firestore, and buttons
     */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.entrant_history_screen);

        //connect the xml
        eventshistory = findViewById(R.id.Events_history);
        notificationbellbutton = findViewById(R.id.notification_Bell_Button);
        navigationhomebutton = findViewById(R.id.navigation_home_button);
        navigationscanbutton = findViewById(R.id.navigation_scan_button);
        navigationhistorybutton = findViewById(R.id.navigation_history_button);
        navigationprofilebutton = findViewById(R.id.navigation_profile_button);
        entranthistoryeventlist = new ArrayList<>();

        //set up firestore and current device id
        entranthistoryadapter = new EntrantHistoryScreenAdapter(this,entranthistoryeventlist);
        eventshistory.setAdapter(entranthistoryadapter);

        //set up the list and adapter
        db = FirebaseFirestore.getInstance();
        currentdeviceid = DeviceIdManager.getDeviceId(this);

        entranthistoryeventlist.clear();

        //reference Get all documents in a collection: https://firebase.google.com/docs/firestore/query-data/get-data#java_4
        //get all events first
        db.collection("events")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    /**
                     * this runs after firestore gets all events
                     * @param task *
                     * then it checks if the current user is in each event waiting list，if so put it in the list and its application status
                     */
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot alleventsdata = task.getResult();
                            final int[] currenteventindex = {0};
                            int totaleventcount = alleventsdata.size();
                            //if no events exist just show empty list
                            if (totaleventcount == 0) {
                                listisempty();
                                return;
                            }
                            //go through every event
                            for (QueryDocumentSnapshot eachevent : alleventsdata) {
                                //for every event we do:
                                String eventid = eachevent.getId();
                                //filter out the events current user has signed up/interacted before
                                db.collection("events")
                                        .document(eventid)
                                        .collection("waitingList")
                                        .document(currentdeviceid)
                                        .get()
                                        .addOnSuccessListener(waitinglistdocument->{
                                            //only add this event if this user actually signed up
                                            if (waitinglistdocument.exists()){
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

                                                //get this user's application status for this event
                                                String thecurrentuserapplicationstatus = waitinglistdocument.getString("status");

                                                Date eventdate = new Date(thecurrenteventdateandtime);
                                                Organizer organizer = new Organizer(thecurrenteventorganizerid, thecurrenteventorganizername);
                                                Event event = new Event(thecurrenteventid, thecurrenteventtitle, thecurrenteventdescription, thecurrenteventlocation, thecurrenteventorganizerid, thecurrenteventorganizername, thecurrenteventcapacity, thecurrentwaitinglistlimit, theregistrationstart, theregistrationend, thecurrenteventdateandtime, thecurrentgeolocationrequired, thecurrentprice);
                                                event.setPosterUri(thecurrenteventposteruri);
                                                event.setUserApplicationStatus(thecurrentuserapplicationstatus);
                                                entranthistoryeventlist.add(event);
                                            }
                                            currenteventindex[0]++;
                                            if (currenteventindex[0] == totaleventcount) {
                                                entranthistoryadapter.notifyDataSetChanged();
                                            }
                                        });
                            }
                        }
                        else{
                            Log.d("EntrantHistoryScreen", "Error getting documents: ", task.getException());
                        }
                    }
                });

        //navigates to notification activity
        //notification activity not yet implemented
        notificationbellbutton.setOnClickListener(v -> {
            //Intent intent = new Intent(EntrantMainScreenActivity.this, //destination.class);
            //startActivity(intent);
        });


        //navigates to home activity
        navigationhomebutton.setOnClickListener(v -> {
            Intent intent = new Intent(EntrantHistoryScreenActivity.this, EntrantMainScreenActivity.class);
            startActivity(intent);
        });


        //navigates to scan activity
        navigationscanbutton.setOnClickListener(v -> {
            Intent intent = new Intent(EntrantHistoryScreenActivity.this, QRCodeActivity.class);
            startActivity(intent);
        });


        //navigates to profile activity
        navigationprofilebutton.setOnClickListener(v -> {
            Intent intent = new Intent(EntrantHistoryScreenActivity.this, EntrantProfileActivity.class);
            startActivity(intent);
        });
    }

    public void listisempty(){
        entranthistoryadapter.notifyDataSetChanged();

    }
}
