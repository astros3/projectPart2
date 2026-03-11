package com.example.eventlottery;


import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Locale;


//reference lab 05 adapter
/**
 *this adapter is for the entrant history screen
 *it shows all the events the current user signed up for before
 *user can click on the event and it will navigates to the event detail activity
 */
public class EntrantHistoryScreenAdapter extends ArrayAdapter<Event> {

    private ArrayList<Event> events;
    private Context context;
    /**
     *this is the constructor for the entrant history adapter
     *@param context this is the current screen context
     *@param events this is the event list that will be shown on the screen
     */
    public EntrantHistoryScreenAdapter(Context context, ArrayList<Event> events){
        super(context, 0, events);
        this.events = events;
        this.context = context;
    }

    /**
     *this makes each row in the history list
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
            view = LayoutInflater.from(context).inflate(R.layout.entrant_event_history_detail_screen, parent, false);
        }
        //get the current event
        Event event = events.get(position);
        //connect xml
        TextView eventnameinput = view.findViewById(R.id.event_name_input);
        TextView eventdatelocationinput = view.findViewById(R.id.event_date_location_input);
        TextView eventcurrentstatus = view.findViewById(R.id.event_current_status);
        ImageView eventarrow = view.findViewById(R.id.event_arrow);
        LinearLayout entranteventhistorybar = view.findViewById(R.id.entrant_event_history_bar);

        //get required event values using getter
        String eventnameinput_fromgetter = event.getTitle();
        long eventdateinput_fromgetter = event.getEventDateMillis();
        String eventlocationinput_fromgetter = event.getLocation();
        String eventstatus_fromgetter = event.getUserApplicationStatus();

        eventnameinput.setText(eventnameinput_fromgetter);

        //turn the date time value into a readable format
        SimpleDateFormat format1 = new SimpleDateFormat("MMM dd, yyyy", Locale.CANADA);
        String date = format1.format(eventdateinput_fromgetter);
        String dateandlocation = date + " · " + eventlocationinput_fromgetter;
        eventdatelocationinput.setText(dateandlocation);

        eventstatus_fromgetter = eventstatus_fromgetter.toUpperCase();
        String backgroundcolortoset ="#FFFFFF";
        String textcolortoset ="#111111";

        //set different colors for different application status
        if (eventstatus_fromgetter.equals("ACCEPTED")) {
            backgroundcolortoset = "#BBF7D0";
            textcolortoset = "#047857";
        }
        if (eventstatus_fromgetter.equals("SELECTED")) {
            backgroundcolortoset = "#4FC3F7";
            textcolortoset = "#FFFFFF";
        }
        if (eventstatus_fromgetter.equals("NOT SELECTED")) {
            backgroundcolortoset = "#F4E04D";
            textcolortoset = "#7C6F00";
        }
        if (eventstatus_fromgetter.equals("PENDING")) {
            backgroundcolortoset = "#9E9E9E";
            textcolortoset = "#FFFFFF";
        }
        if (eventstatus_fromgetter.equals("DECLINED")) {
            backgroundcolortoset = "#FAD4D4";
            textcolortoset = "#C62828";
        }

        //show status text and color
        eventcurrentstatus.setText(eventstatus_fromgetter);


        eventcurrentstatus.setBackgroundColor(Color.parseColor(backgroundcolortoset));
        eventcurrentstatus.setTextColor(Color.parseColor(textcolortoset));



        //when user clicks on a specific history item it will navigates to event details screen
        entranteventhistorybar.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailsActivity.class);
            intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, event.getEventId());
            intent.putExtra(EventDetailsActivity.EXTRA_VIEW_AS_ENTRANT, true);
            context.startActivity(intent);
        });





        return view;
    }
}
