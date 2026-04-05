package com.example.eventlottery;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;



/**
 * Adapter for AdminEventControlScreenActivity event list. Row click opens event details; delete not fully wired.
 */
public class AdminEventControlScreenAdapter extends ArrayAdapter<Event> {

    private ArrayList<Event> events;
    private Context context;
    private FirebaseFirestore db;
    private static final String TAG = "AdminEventControl";

    /**
     *this is the constructor for the admin event control screen adapter
     * @param context this is the current screen context
     * @param events this is the event list that will be shown on the screen
     */
    public AdminEventControlScreenAdapter(Context context, ArrayList<Event> events){
        super(context, 0, events);
        this.events = events;
        this.context = context;
        this.db = FirebaseFirestore.getInstance();
    }

    /**
     *this makes each list item
     *@param position this is the current position in the list
     *param convertView this is the old view
     *@param parent this is the parent view
     *@return view this returns the formatted view
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.admin_event_control_detail_screen, parent, false);
        }
        //get the current event
        Event event = events.get(position);

        //connect xml
        ImageView eventphoto = view.findViewById(R.id.event_photo);
        TextView eventnameinput = view.findViewById(R.id.event_name_input);
        TextView eventdateinput = view.findViewById(R.id.event_date_input);

        TextView eventorganizerownernameinput = view.findViewById(R.id.event_organizer_owner_name_input);

        Button adminviewdetailbutton = view.findViewById(R.id.admin_event_control_view_detail_button);
        ImageView admindeletebutton = view.findViewById(R.id.admin_event_control_delete_button);

        //get the required event values from getters
        String eventnameinput_fromgetter = event.getTitle();
        long eventdateinput_fromgetter = event.getEventDateMillis();
        String eventposteruri_fromgetter = event.getPosterUri();

        //turn the date time value into a readable format
        SimpleDateFormat format1 = new SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.CANADA);
        String date = format1.format(eventdateinput_fromgetter);
        eventdateinput.setText(date);

        //show organizer name event image event title
        String eventownername_fromgetter = event.getOrganizerName();
        eventorganizerownernameinput.setText(eventownername_fromgetter);
        if (eventposteruri_fromgetter != null && !eventposteruri_fromgetter.isEmpty()) {
            Glide.with(context).load(eventposteruri_fromgetter)
                    .placeholder(R.drawable.ic_launcher_background)
                    .error(R.drawable.ic_launcher_background)
                    .centerCrop()
                    .into(eventphoto);
        } else {
            eventphoto.setImageResource(R.drawable.ic_launcher_background);
        }
        eventnameinput.setText(eventnameinput_fromgetter);

        //when admin clicks on a specific history item it will navigates to admin event details panel
        adminviewdetailbutton.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailsActivity.class);
            intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, event.getEventId());
            intent.putExtra(EventDetailsActivity.EXTRA_VIEW_AS_ENTRANT, false);
            context.startActivity(intent);
        });

        admindeletebutton.setOnClickListener(v -> {
            String title = eventnameinput_fromgetter != null ? eventnameinput_fromgetter.trim() : "";
            String msg = title.isEmpty()
                    ? context.getString(R.string.admin_delete_event_message_generic)
                    : context.getString(R.string.admin_delete_event_message, title);
            new AlertDialog.Builder(context)
                    .setTitle(R.string.admin_delete_event_title)
                    .setMessage(msg)
                    .setPositiveButton(R.string.admin_delete_action, (dialog, which) -> {
                        String eventidtobedeleted = event.getEventId();
                        db.collection("events").document(eventidtobedeleted)
                                .delete()
                                .addOnSuccessListener(aVoid -> {
                                    events.remove(event);
                                    notifyDataSetChanged();
                                })
                                .addOnFailureListener(e -> Log.w(TAG, "Error deleting document", e));
                    })
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        });

        return view;
    }
}

