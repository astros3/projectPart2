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

/**
 * Admin screen for browsing and managing entrant/organizer profiles (US 03.02.01, US 03.07.01).
 * Lists all profiles from Firestore with an in-memory search by name.
 * Access is restricted to devices registered in the Firestore "admins" collection.
 */
public class AdminProfileControlScreenActivity extends BaseActivity {

    /** No-arg constructor required by the Android Activity lifecycle. */
    public AdminProfileControlScreenActivity() {}

    //declearation
    private ImageView confirmbackButton;
    private ImageView confirmsearchButton;
    private EditText searchInput;
    private ListView userListView;
    private ArrayList<AdminProfileItemTemporary> profilelist;
    private ArrayList<AdminProfileItemTemporary> profilelistbackup;
    private AdminProfileControlScreenAdapter adminprofilecontrolscreenadapter;
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
    /**
     * the following will run when this activity/screen is opened
     *
     *it sets up the views adapter and gets required profile data from firestore
     */
    private void MainScreenAlgorithmn() {
        //link the xml
        setContentView(R.layout.admin_profile_control_screen);
        confirmbackButton = findViewById(R.id.confirm_back_button);
        confirmsearchButton = findViewById(R.id.confirmation_search_button);
        searchInput = findViewById(R.id.admin_user_search_inputbar);
        userListView = findViewById(R.id.user_list);

        //two arraylists are used, one is used to store the displaying value, one is used to store the backup value.
        profilelist  = new ArrayList<>();
        profilelistbackup  = new ArrayList<>();
        //setting up the adapter
        adminprofilecontrolscreenadapter = new AdminProfileControlScreenAdapter(this, profilelist);
        userListView.setAdapter(adminprofilecontrolscreenadapter);
        //get all the profile data from database
        db = FirebaseFirestore.getInstance();

        profilelist.clear();
        profilelistbackup.clear();

        //organizer

        //reference Get all documents in a collection: https://firebase.google.com/docs/firestore/query-data/get-data#java_4
        //get all organizer information from firestore
        db.collection("organizers")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            QuerySnapshot allOrganizerinformaiton = task.getResult();
                            //go through every organizer in firestore
                            for (QueryDocumentSnapshot eachorganizer : allOrganizerinformaiton) {
                                //extracting all
                                String currentorganizerid = eachorganizer.getId();
                                String currentorganizerfirstname = eachorganizer.getString("firstName");
                                if (currentorganizerfirstname!=null){
                                    currentorganizerfirstname = currentorganizerfirstname.trim();
                                }
                                else{
                                    currentorganizerfirstname = "";
                                }

                                String currentorganizerlastname = eachorganizer.getString("lastName");
                                if (currentorganizerlastname!=null){
                                    currentorganizerlastname = currentorganizerlastname.trim();
                                }
                                else{
                                    currentorganizerlastname = "";
                                }
                                String combinedFirstandLastname = currentorganizerfirstname + " " + currentorganizerlastname;
                                combinedFirstandLastname = combinedFirstandLastname.trim();

                                if (combinedFirstandLastname.equals("")){
                                    combinedFirstandLastname = "UNKNOWN NAME";
                                }

                                AdminProfileItemTemporary currentorganizerprofile = new AdminProfileItemTemporary(currentorganizerid, combinedFirstandLastname, "Organizer");
                                profilelist.add(currentorganizerprofile);
                                profilelistbackup.add(currentorganizerprofile);
                            }

                            adminprofilecontrolscreenadapter.notifyDataSetChanged();
                        } else {
                            Log.d("AdminEventControl", "Error getting documents: ", task.getException());
                        }
                    }
                });

        //organizer

        //reference Get all documents in a collection: https://firebase.google.com/docs/firestore/query-data/get-data#java_4
        //get all user information from firestore
        db.collection("users")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            QuerySnapshot allUserinformaiton = task.getResult();
                            //go through every organizer in firestore
                            for (QueryDocumentSnapshot eachUser : allUserinformaiton) {
                                //extracting all
                                Entrant eachUserinformation = eachUser.toObject(Entrant.class);


                                String currentuserid = eachUserinformation.getDeviceID();
                                if (currentuserid == null || currentuserid.trim().equals("")) {
                                    currentuserid = "Unknown ID";
                                }


                                String currentusername = eachUserinformation.getFullName();

                                if (currentusername != null && !currentusername.equals("")) {
                                    currentusername = currentusername.trim();
                                }
                                else if (currentusername == null || currentusername.equals("")) {
                                    currentusername = "Unknown Name";
                                }

                                AdminProfileItemTemporary currentuserprofile =new AdminProfileItemTemporary(currentuserid,currentusername,"Entrant");



                                profilelist.add(currentuserprofile);
                                profilelistbackup.add(currentuserprofile);
                            }

                            adminprofilecontrolscreenadapter.notifyDataSetChanged();
                        } else {
                            Log.d("AdminEventControl", "Error getting documents: ", task.getException());
                        }
                    }
                });






        //back button navigates back to the previous page
        confirmbackButton.setOnClickListener(v -> {
            startActivity(new Intent(this, AdminMainScreenActivity.class));
            finish();
        });


        //when search button is clicked search entrant/organizer by name
        if (confirmsearchButton != null) {
            confirmsearchButton.setOnClickListener(v -> {
                String userinput = searchInput.getText().toString();
                profilelist.clear();
                String userinputlowercase = userinput.toLowerCase().trim();
                //when the user didn't input anything, its intending to view all the profiles
                if (userinputlowercase.equals("")) {

                    for(AdminProfileItemTemporary currentprofileitem:profilelistbackup) {
                        profilelist.add(currentprofileitem);
                    }

                    adminprofilecontrolscreenadapter.notifyDataSetChanged();
                    return;
                }

                //collects all the profiles that matches the name input
                for(AdminProfileItemTemporary currentprofileitem:profilelistbackup) {

                    String currentprofilename= currentprofileitem.getProfileName();
                    String newcurrentprofilename = currentprofilename.toLowerCase();


                    if(newcurrentprofilename.contains(userinputlowercase)) {
                        profilelist.add(currentprofileitem);
                    }
                }
                adminprofilecontrolscreenadapter.notifyDataSetChanged();
            });
        }


    }
}