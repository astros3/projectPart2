package com.example.eventlottery;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;


//reference lab 05 adapter
/**
 *this adapter is for the entrant main screen
 */
public class EntrantMainScreenAdapter extends ArrayAdapter<Event> {

    private ArrayList<Event> events;
    private Context context;
    /**
     *this is constructor for the entrant main screen adapter
     *@param context this is the current screen context
     *@param events this is the event list that will be shown on the screen
     */
    public EntrantMainScreenAdapter(Context context, ArrayList<Event> events){
        super(context, 0, events);
        this.events = events;
        this.context = context;
    }

    /**
     *this makes each row in the event list
     *@param position this is the current position in the list
     *@param convertView this is the old view
     *@param parent this is the parent view group
     *@return view this returns the finished formatted view
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if(view == null){
            view = LayoutInflater.from(context).inflate(R.layout.event_list_detail_screen, parent, false);
        }
        //get the current event object
        Event event = events.get(position);
        //connect xml
        ImageView eventphoto = view.findViewById(R.id.event_photo);
        TextView eventnameinput = view.findViewById(R.id.event_name_input);
        TextView eventdateinput = view.findViewById(R.id.event_date_input);
        Button buttonviewdetail = view.findViewById(R.id.button_view_detail);
        //get the required event values from the getters
        String eventnameinput_fromgetter = event.getTitle();
        long eventdateinput_fromgetter = event.getEventDateMillis();
        String eventposteruri_fromgetter = event.getPosterUri();
        //show event title
        eventnameinput.setText(eventnameinput_fromgetter);
        //turn the date time into a readable format
        SimpleDateFormat format1 = new SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.CANADA);
        String date = format1.format(eventdateinput_fromgetter);
        eventdateinput.setText(date);

        //show event photo from uri
       // eventphoto.setImageURI(android.net.Uri.parse(eventposteruri_fromgetter));
        eventphoto.setImageURI(null);
        //when user click on a specific event's "view detail" it will navigates to its detail screen
        buttonviewdetail.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailsActivity.class);
            intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, event.getEventId());
            intent.putExtra(EventDetailsActivity.EXTRA_VIEW_AS_ENTRANT, true);
            context.startActivity(intent);
        });

        return view;
    }
}

