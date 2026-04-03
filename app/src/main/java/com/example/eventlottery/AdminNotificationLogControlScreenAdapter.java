package com.example.eventlottery;


import static androidx.fragment.app.FragmentManager.TAG;

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
//US 03.08.01
public class AdminNotificationLogControlScreenAdapter extends ArrayAdapter<AdminNotificationLogItemTemporary> {

    private ArrayList<AdminNotificationLogItemTemporary> adminnotificationlist;
    private Context context;
    private FirebaseFirestore db;
    /**
     *this is the constructor for the admin profile control screen adapter
     *@param context this is the current screen context
     *@param adminnotificationlist this is the profile list that will be shown on the screen
     */
    public AdminNotificationLogControlScreenAdapter(Context context, ArrayList<AdminNotificationLogItemTemporary> adminnotificationlist){
        super(context, 0, adminnotificationlist);
        this.adminnotificationlist = adminnotificationlist;
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
            view = LayoutInflater.from(context).inflate(R.layout.admin_notification_log_control_detail_screen, parent, false);
        }

        //get the current profile
        AdminNotificationLogItemTemporary currentnotification = adminnotificationlist.get(position);
        //connect xml




        TextView notificationtitleinput = view.findViewById(R.id.notification_title_input);
        TextView relatedeventnameandeventidinput = view.findViewById(R.id.notification_related_event_name_and_event_id);
        TextView organizeroradmininput = view.findViewById(R.id.organizerOrAdmininput);
        TextView recipientinput = view.findViewById(R.id.notification_recipient_name);
        TextView timeinput = view.findViewById(R.id.time);
        TextView notificationmessageinput = view.findViewById(R.id.notificationmessage);

        ImageView deletebutton = view.findViewById(R.id.admin_event_control_delete_button);

        // Notification title
        String title = currentnotification.getTitle();
        notificationtitleinput.setText((title != null && !title.isEmpty()) ? title : "UNKNOWN TITLE");

        // Event name only (no ID)
        String eventname = currentnotification.getEventname();
        relatedeventnameandeventidinput.setText("Event: " + ((eventname != null && !eventname.isEmpty()) ? eventname : "UNKNOWN EVENT"));

        // Organizer (sender) name
        String sendername = currentnotification.getSendername();
        organizeroradmininput.setText("Organizer: " + ((sendername != null && !sendername.isEmpty()) ? sendername : "UNKNOWN ORGANIZER"));

        // Entrant (recipient) name
        String receivername = currentnotification.getReceiverName();
        recipientinput.setText("Sent to: " + ((receivername != null && !receivername.isEmpty()) ? receivername : "Loading..."));

        // Message
        String notificationmessage = currentnotification.getMessage();
        notificationmessageinput.setText((notificationmessage != null && !notificationmessage.isEmpty()) ? notificationmessage : "NO MESSAGE GIVEN");

        SimpleDateFormat format1 = new SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.CANADA);
        timeinput.setText(format1.format(new Date(currentnotification.getTime())));




        //delete the profile from firestore when admin clicks delete icon
        deletebutton.setOnClickListener(v -> {
            String notificationidneedstobedeleted = currentnotification.getNotificationID();
            db.collection("notificationStorageAdmin").document(notificationidneedstobedeleted)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        /**
                         *@param aVoid
                         *removes the notification from the list and refreshes the adapter
                         */
                        @Override
                        public void onSuccess(Void aVoid) {
                            adminnotificationlist.remove(currentnotification);
                            notifyDataSetChanged();
                        }
                    })
                    /**
                     * this runs if delete failed
                     * @param e prints the error in log
                     * reference from https://firebase.google.com/docs/firestore/manage-data/delete-data#java
                     */
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("AdminNotificationLogControlScreenAadpter", "Error deleting document", e);
                        }
                    });
        });


        ///MISSING, AWAITING FOR ORGANZIER NOTIFICATION TO BE COMPLETED
        ///MISSING: REMOVE ALL THE NOTIFICATIONS UNDER USER DATABASE

        ///MISSING: REMOVE THE NOTIFICATION UNDER ORGANIZER





        return view;
    }
}

