package com.example.eventlottery;

import android.content.Intent;
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

//US 03.08.01
public class AdminNotificationLogControlScreenActivity extends AppCompatActivity {
    //declearation
    private ImageView confirmbackButton1;
    private ListView nottificationlogListView;
    private ArrayList<AdminNotificationLogItemTemporary> notificationloglist;
    private ArrayList<AdminNotificationLogItemTemporary> notificationloglistbackup;
    private AdminNotificationLogControlScreenAdapter adminnotificationlogcontrolscreenadapter;
    private FirebaseFirestore db;



    /**
     * the following will run when this activity/screen is opened
     * @param savedInstanceState *
     * this function is used for verifying the admin status, and once it is passed admin status verification, it will passed to main alogorithm, and from there it will redener the screen and etc.
     */
    //reference AdminEventControlScreenActivity
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

                    MainScreenAlgorithmn();

                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to verify admin: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
    }


    private void MainScreenAlgorithmn() {
        //link the xml
        setContentView(R.layout.admin_notification_log_control_screen);
        confirmbackButton1 = findViewById(R.id.back_button_notification_log);
        nottificationlogListView = findViewById(R.id.notification_log_list);

        //two arraylists are used, one is used to store the displaying value, one is used to store the backup value.
        notificationloglist  = new ArrayList<>();
        notificationloglistbackup  = new ArrayList<>();
        //setting up the adapter

        adminnotificationlogcontrolscreenadapter = new AdminNotificationLogControlScreenAdapter(this, notificationloglist);
        nottificationlogListView.setAdapter(adminnotificationlogcontrolscreenadapter);
        //get all the profile data from database
        db = FirebaseFirestore.getInstance();

        notificationloglist.clear();



        //reference Get all documents in a collection: https://firebase.google.com/docs/firestore/query-data/get-data#java_4
        //get all notification information from firestore
        db.collection("notificationStorageAdmin")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            QuerySnapshot allnotificationinformation = task.getResult();

                            //go through every notification in firestore
                            for (QueryDocumentSnapshot eachnotification : allnotificationinformation) {
                                //extracting all information
                                String currentnotificationid = eachnotification.getId();

                                String currentnotificationgroupid = eachnotification.getString("notificationgroupid");
                                if ((currentnotificationgroupid == null)|| (currentnotificationgroupid.trim().equals(""))) {
                                    currentnotificationgroupid = "";
                                }


                                String currentnotificationtitlevalue = eachnotification.getString("title");
                                if((currentnotificationtitlevalue != null) && (!currentnotificationtitlevalue.trim().equals(""))) {
                                    currentnotificationtitlevalue = currentnotificationtitlevalue.trim();
                                }
                                else{
                                    currentnotificationtitlevalue = "NO TITLE";
                                }

                                String currentnotificationmessagevalue = eachnotification.getString("message");
                                if ((currentnotificationmessagevalue != null) && (!currentnotificationmessagevalue.trim().equals(""))) {
                                    currentnotificationmessagevalue = currentnotificationmessagevalue.trim();
                                }
                                else{
                                    currentnotificationmessagevalue = "NO MESSAGE";
                                }

                                String currentnotificationrelatedeventidvalue = eachnotification.getString("eventId");
                                if ((currentnotificationrelatedeventidvalue != null) && (!currentnotificationrelatedeventidvalue.trim().equals(""))) {
                                    currentnotificationrelatedeventidvalue = currentnotificationrelatedeventidvalue.trim();
                                }
                                else{
                                    currentnotificationrelatedeventidvalue = "IMPORTANT:NO ID FOUND";
                                }

                                Long currentnotificationtimevalue = eachnotification.getLong("timestampMillis");
                                long currentnotificationsenttime = 0;
                                if (currentnotificationtimevalue != null) {
                                    currentnotificationsenttime = currentnotificationtimevalue;
                                }

                                if(currentnotificationrelatedeventidvalue.equals("IMPORTANT:NO ID FOUND")) {
                                    AdminNotificationLogItemTemporary newitemtoadd = new AdminNotificationLogItemTemporary(currentnotificationsenttime, currentnotificationtitlevalue, currentnotificationmessagevalue, "", "UNKNOWN ORGANZIER", "UNKNOWN EVENT",currentnotificationid,currentnotificationgroupid);

                                    notificationloglist.add(newitemtoadd);
                                    adminnotificationlogcontrolscreenadapter.notifyDataSetChanged();
                                }
                                else {
                                    final String currentnotificationtitlevalue1 = currentnotificationtitlevalue;
                                    final String currentnotificationmessagevalue1 = currentnotificationmessagevalue;
                                    final String currentnotificationrelatedeventidvalue1 = currentnotificationrelatedeventidvalue;
                                    final long currentnotificationsenttime1 = currentnotificationsenttime;
                                    final String currentnotificationid1 = currentnotificationid;
                                    final String currentnotificationgroupid1 = currentnotificationgroupid;

                                    db.collection("events")
                                            .get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        QuerySnapshot alleventinformation = task.getResult();
                                                        String currenteventorganizername1 = "UNKNOWN ORGANIZER";
                                                        String currentnotificationeventname1 = "UNKNOWN EVENT";
                                                        //go through every event in firestore
                                                        for (QueryDocumentSnapshot eachevent : alleventinformation) {
                                                            //extracting all information
                                                            String currenteventid = eachevent.getId();
                                                            if (currentnotificationrelatedeventidvalue1.equals(currenteventid)) {
                                                                String currenteventorganizername = eachevent.getString("organizerName");

                                                                if ((currenteventorganizername != null) && (!currenteventorganizername.trim().equals(""))) {
                                                                    currenteventorganizername = currenteventorganizername.trim();
                                                                    currenteventorganizername1 = currenteventorganizername;
                                                                }

                                                                else{
                                                                    currenteventorganizername1 = "UNKNOWN ORGANIZER";
                                                                }
                                                                String currentnotificationeventname = eachevent.getString("title");
                                                                if((currentnotificationeventname != null) && (!currentnotificationeventname.trim().equals(""))) {
                                                                    currentnotificationeventname1 = currentnotificationeventname.trim();
                                                                }
                                                                else{
                                                                    currentnotificationeventname1 = "UNKNOWN EVENT";
                                                                }
                                                                break;

                                                            }
                                                        }
                                                        AdminNotificationLogItemTemporary currentnotifciationitem = new AdminNotificationLogItemTemporary(currentnotificationsenttime1, currentnotificationtitlevalue1, currentnotificationmessagevalue1, currentnotificationrelatedeventidvalue1, currenteventorganizername1, currentnotificationeventname1, currentnotificationid1,currentnotificationgroupid1);
                                                        notificationloglist.add(currentnotifciationitem);
                                                        adminnotificationlogcontrolscreenadapter.notifyDataSetChanged();
                                                    } else {
                                                        Log.d("AdminNotificationControl", "Error getting documents: ", task.getException());
                                                    }
                                                }
                                            });
                                }
                            }
                        }
                        else {
                            Log.d("AdminNotificationControl", "Error getting documents: ", task.getException());
                        }
                    }
                });








        //back button navigates back to the previous page
        confirmbackButton1.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminMainScreenActivity.class));
            finish();
        });




    }
}