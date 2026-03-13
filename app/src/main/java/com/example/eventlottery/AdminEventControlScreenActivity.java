package com.example.eventlottery;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.eventlottery.Organizer;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;

/**
 * Admin view: list all events from Firestore, search by title (in-memory).
 * Access restricted to devices that have an entry in Firestore "admins" collection (document ID = deviceId).
 */
public class AdminEventControlScreenActivity extends AppCompatActivity {
    private ArrayList<Event> admineventlist;
    private ArrayList<Event> alladmineventlistbackup;
    private AdminEventControlScreenAdapter admineventadapter;
    private ImageView backbutton;
    private ImageView admineventfilterbutton;
    private EditText admineventsearchinputbar;
    private ImageView searchbutton;
    private ListView eventshistory;
    private FirebaseFirestore db;
    /**
     * the following will run when this activity/screen is opened
     * @param savedInstanceState *
     * it sets up the views adapter and gets required event data from firestore
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Restrict access: only devices with an entry in Firestore "admins" collection may open this screen
        String deviceId = DeviceIdManager.getDeviceId(this);
        FirebaseFirestore dbCheck = FirebaseFirestore.getInstance();
        dbCheck.collection("admins").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "Access denied. You must be an admin to access this.", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                    initAdminUi();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to verify admin: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void initAdminUi() {
        setContentView(R.layout.admin_event_control_screen);
        backbutton = findViewById(R.id.back_button);

        admineventfilterbutton = findViewById(R.id.admin_event_filter_button);
        admineventsearchinputbar = findViewById(R.id.admin_event_search_inputbar);
        searchbutton = findViewById(R.id.search_button);
        eventshistory = findViewById(R.id.Events_history);

        //two arraylists are used, one is used to store the displaying value, one is used to store the backup value.
        admineventlist = new ArrayList<>();

        alladmineventlistbackup = new ArrayList<>();

        //setting up the adapter
        admineventadapter = new AdminEventControlScreenAdapter(this,admineventlist);
        eventshistory.setAdapter(admineventadapter);
        //get all the events data from database
        db = FirebaseFirestore.getInstance();

        admineventlist.clear();
        alladmineventlistbackup.clear();

        //reference Get all documents in a collection: https://firebase.google.com/docs/firestore/query-data/get-data#java_4
        //get all events from firestore
        db.collection("events")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            QuerySnapshot alleventsdata = task.getResult();
                            int totaleventcount = alleventsdata.size();

                            if (totaleventcount == 0) {
                                admineventadapter.notifyDataSetChanged();
                                return;
                            }
                            //go through every event in firestore
                            for (QueryDocumentSnapshot eachevent : alleventsdata) {
                                //extracting all
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
                                admineventlist.add(thecurrentevent);
                                alladmineventlistbackup.add(thecurrentevent);
                            }

                            admineventadapter.notifyDataSetChanged();
                        } else {
                            Log.d("AdminEventControl", "Error getting documents: ", task.getException());
                        }
                    }
                });
        backbutton.setOnClickListener(v -> {
            finish();
        });

        //when filtering button is clicked it will navigate to filtering screen
        admineventfilterbutton.setOnClickListener(v -> {
            //Intent intent = new Intent(EntrantMainScreenActivity.this, //destination.class);
            //startActivity(intent);
        });

        //when search button is clicked search event by title
        searchbutton.setOnClickListener(v -> {
            String userinput = admineventsearchinputbar.getText().toString();
            admineventlist.clear();
            String userinputlowercase = userinput.toLowerCase().trim();
            //when the user didn't input anything, its intending to view all the events
            if (userinputlowercase.equals("")) {
                for(Event currentevent:alladmineventlistbackup) {
                    admineventlist.add(currentevent);
                }
                admineventadapter.notifyDataSetChanged();
                return;
            }
            //collects all the events that matches the title input
            for(Event currentevent:alladmineventlistbackup) {
                String currenteventtitle = currentevent.getTitle();
                String newcurrenteventtitle = currenteventtitle.toLowerCase();
                if(newcurrenteventtitle.contains(userinputlowercase)) {
                    admineventlist.add(currentevent);
                }
            }
            admineventadapter.notifyDataSetChanged();
        });

    }
}
