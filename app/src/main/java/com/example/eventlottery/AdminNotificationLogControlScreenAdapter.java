package com.example.eventlottery;


import static androidx.fragment.app.FragmentManager.TAG;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
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
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
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
        TextView timeinput = view.findViewById(R.id.time);
        TextView notificationmessageinput = view.findViewById(R.id.notificationmessage);

        ImageView deletebutton = view.findViewById(R.id.admin_event_control_delete_button);


        //get required values using getter
        String notificationtitletobedisplayed = currentnotification.getTitle();
        String eventnametobedisplayed = currentnotification.getEventname();
        String notificationmessage = currentnotification.getMessage();
        String eventiddisplayed = currentnotification.getEventid();
        String sendernamettobedisplayed = currentnotification.getSendername();

        long timetobedisplayed = currentnotification.getTime();

        //displaying the value
        if(!(notificationtitletobedisplayed == null)&&!(notificationtitletobedisplayed.equals(""))){
            notificationtitleinput.setText(notificationtitletobedisplayed);
        }
        else{
            notificationtitleinput.setText("UNKNOWN NOTIFCATION TITLE");
        }

        String eventname = "";
        if(!(eventnametobedisplayed == null)&&!(eventnametobedisplayed.equals(""))){
            eventname = eventnametobedisplayed;
        }
        else{
            eventname ="UNKNOWN EVENT NAME";
        }

        String eventid = "";
        if(!(eventiddisplayed == null)&&!(eventiddisplayed.equals(""))){
            eventid = eventiddisplayed;
        }
        else{
            eventid ="UNKNOWN EVENT ID";
        }


        String eventnameandid = eventname + ": " + eventid;


        relatedeventnameandeventidinput.setText(eventnameandid);

        if(!(sendernamettobedisplayed == null)&&!(sendernamettobedisplayed.equals(""))){
            organizeroradmininput.setText(sendernamettobedisplayed);
        }
        else{
            organizeroradmininput.setText("UNKNOWN ORGANIZER");
        }

        if(!(notificationmessage == null)&&!(notificationmessage.equals(""))) {
            notificationmessageinput.setText(notificationmessage);
        }
        else{
            notificationmessageinput.setText("NO MESSAGE GIVEN");
        }

        //reference entrant main screen activity
        SimpleDateFormat format1 = new SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.CANADA);
        String date = format1.format(timetobedisplayed);
        timeinput.setText(date);

        //delete the notification from firestore when admin clicks delete icon
        deletebutton.setOnClickListener(v -> {
            //reference: https://stackoverflow.com/questions/2115758/how-do-i-display-an-alert-dialog-on-android
            new AlertDialog.Builder(context)
                    .setTitle("Delete notification confirmation")
                    .setMessage("Are you sure you want to delete this notification?(No reverse)")


                    .setPositiveButton("Confirm Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {


                            deletefunction(currentnotification);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override

                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    })
                    .show();
        });








        return view;
    }



    private void deletefunction(AdminNotificationLogItemTemporary currentnotification){
        String currentnotificationgroupid = currentnotification.getNotificationGroupId();

        db.collection("notificationStorageAdmin")
                .whereEqualTo("notificationgroupid", currentnotificationgroupid)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    /**
                     * @param value
                     */
                    @Override
                    public void onSuccess(QuerySnapshot value) {

                        for (QueryDocumentSnapshot eachvalue : value) {
                            eachvalue.getReference().delete();
                        }

                        deleterelatedusernotificationvaluefromdatabase(currentnotificationgroupid, currentnotification);
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
                        Log.w("AdminNotificationLogControlScreenAdapter", "Error deleting", e);
                    }
                });
    }

    //reference Get all documents in a collection: https://firebase.google.com/docs/firestore/query-data/get-data#java_4
    /**
     * this function deletes the intended notification from user database so user will no longer see that notification
     * @param currentnotificationgroupidtobedeleted the notification id we can used to search in user
     * @param currentnotificationclasstoberemoved the notification class to be removed
     */
    private void deleterelatedusernotificationvaluefromdatabase(String currentnotificationgroupidtobedeleted, AdminNotificationLogItemTemporary currentnotificationclasstoberemoved){

        db.collectionGroup("notifications")
                .whereEqualTo("notificationgroupid", currentnotificationgroupidtobedeleted)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    /**
                     * this deletes notification from all the user datebase
                     * @param value1
                     */
                    @Override
                    public void onSuccess(QuerySnapshot value1) {

                        for (QueryDocumentSnapshot eachvalue : value1) {
                            eachvalue.getReference().delete();
                        }


                        refreshscreen( currentnotificationclasstoberemoved);
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
                        Log.w("AdminNotificationLogControlScreenAdapter", "Error deleting", e);
                    }
                });
    }
    /**
     * this function deletes the intended notification from admin list
     * @param currentnotificationclasstoberemoved the notification to be removed from admin list
     */
    private void refreshscreen(AdminNotificationLogItemTemporary currentnotificationclasstoberemoved){
        adminnotificationlist.remove(currentnotificationclasstoberemoved);
        notifyDataSetChanged();
    }
}



