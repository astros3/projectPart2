package com.example.eventlottery;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import android.util.Log;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;


/**
 * Adapter for AdminProfileControlScreenActivity: Row click will directs to profile detail screen.
 */
//US 03.02.01
//US 03.07.01
public class AdminProfileControlScreenAdapter extends ArrayAdapter<AdminProfileItemTemporary> {

    private ArrayList<AdminProfileItemTemporary> adminprofileitemlist;
    private Context context;
    private FirebaseFirestore db;
    /**
     *this is the constructor for the admin profile control screen adapter
     *@param context this is the current screen context
     *@param adminprofileitemlist this is the profile list that will be shown on the screen
     */
    public AdminProfileControlScreenAdapter(Context context, ArrayList<AdminProfileItemTemporary> adminprofileitemlist){
        super(context, 0, adminprofileitemlist);
        this.adminprofileitemlist = adminprofileitemlist;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     *this makes each row for the list
     *@param position this is the current position in the list
     *@param convertView this is the old view
     *@param parent this is the parent view group
     *@return view this returns the finished row view
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.admin_profile_control_detail_screen, parent, false);
        }

        //get the current profile
        AdminProfileItemTemporary currentprofile = adminprofileitemlist.get(position);
        //connect xml


        TextView profilenameinput = view.findViewById(R.id.profile_name_input);
        TextView profiletypeinput = view.findViewById(R.id.event_organizer_owner_name_input);
        Button viewdetailbutton = view.findViewById(R.id.admin_event_control_view_detail_button);
        ImageView deletebutton = view.findViewById(R.id.admin_event_control_delete_button);


        //get required values using getter
        String profilenametobedisplayed = currentprofile.getProfileName();
        String profiletypetobedisplayed = currentprofile.getType();
        String profileidtobedisplayed = currentprofile.getId();



        if((profilenametobedisplayed == null)||(profilenametobedisplayed.equals(""))) {
            profilenameinput.setText("UNKNOWN NAME");
        }
        else{
            profilenameinput.setText(profilenametobedisplayed);
        }

        if((profiletypetobedisplayed == null)||(profiletypetobedisplayed.equals(""))) {
            profiletypeinput.setText("UNKNOWN TYPE");
        }
        else{
            profiletypeinput.setText(profiletypetobedisplayed);
        }










        //delete the profile from firestore when admin clicks delete icon
        deletebutton.setOnClickListener(v -> {
            //reference: https://firebase.google.com/docs/firestore/manage-data/delete-data#java
            String profileidneedstobedeleted = currentprofile.getId();
            String profiletypeneedstobedeleted = currentprofile.getType();

            if(profileidneedstobedeleted != null && !profileidneedstobedeleted.equals("")
                    && profiletypeneedstobedeleted != null && !profiletypeneedstobedeleted.equals("")) {

                if(profiletypeneedstobedeleted.equalsIgnoreCase("Organizer")) {
                    db.collection("organizers").document(profileidneedstobedeleted)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>(){
                                /**
                                 *@param aVoid
                                 *removes the profile from the list and refreshes the adapter
                                 */
                                @Override
                                public void onSuccess(Void aVoid) {
                                    adminprofileitemlist.remove(currentprofile);
                                    notifyDataSetChanged();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener(){
                                /**
                                 * this runs if delete failed
                                 * @param e prints the error in log
                                 * reference from https://firebase.google.com/docs/firestore/manage-data/delete-data#java
                                 */
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("AdminProfileControl", "Error deleting organizer document", e);
                                }
                            });
                }

                else if(profiletypeneedstobedeleted.equalsIgnoreCase("Entrant")) {
                    db.collection("users").document(profileidneedstobedeleted)
                            .delete()
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                /**
                                 *@param aVoid
                                 *removes the profile from the list and refreshes the adapter
                                 */
                                @Override
                                public void onSuccess(Void aVoid) {
                                    adminprofileitemlist.remove(currentprofile);
                                    notifyDataSetChanged();
                                }
                            })

                            .addOnFailureListener(new OnFailureListener() {
                                /**
                                 * this runs if delete failed
                                 * @param e prints the error in log
                                 * reference from https://firebase.google.com/docs/firestore/manage-data/delete-data#java
                                 */
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w("AdminProfileControl", "Error deleting user document", e);
                                }
                            });
                }
            }
        });

        //when user clicks on a specific profile item it will navigates to profile details screen
        viewdetailbutton.setOnClickListener(v -> {


        });





        return view;
    }
}

