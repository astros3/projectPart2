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

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.text.SimpleDateFormat;
import java.util.Locale;


/**
 * Adapter for event list on EntrantMainScreenActivity. Each row opens EventDetailsActivity.
 * When user's status for an event is SELECTED, shows invitation prompt and "RESPOND TO INVITATION" CTA.
 */
public class EntrantMainScreenAdapter extends ArrayAdapter<Event> {

    private ArrayList<Event> events;
    private Context context;
    private Map<String, String> eventIdToStatus = new HashMap<>();

    public EntrantMainScreenAdapter(Context context, ArrayList<Event> events){
        super(context, 0, events);
        this.events = events;
        this.context = context;
    }

    /** Sets per-event status (from waiting list) so we can show invitation CTA when status is SELECTED. */
    public void setEventIdToStatus(Map<String, String> eventIdToStatus) {
        this.eventIdToStatus = eventIdToStatus != null ? eventIdToStatus : new HashMap<>();
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
        Event event = events.get(position);
        ImageView eventphoto = view.findViewById(R.id.event_photo);
        TextView eventnameinput = view.findViewById(R.id.event_name_input);
        TextView eventdateinput = view.findViewById(R.id.event_date_input);
        TextView eventInvitationPrompt = view.findViewById(R.id.event_invitation_prompt);
        Button buttonviewdetail = view.findViewById(R.id.button_view_detail);

        String eventnameinput_fromgetter = event.getTitle();
        long eventdateinput_fromgetter = event.getEventDateMillis();
        eventnameinput.setText(eventnameinput_fromgetter);

        SimpleDateFormat format1 = new SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.CANADA);
        String date = format1.format(eventdateinput_fromgetter);
        eventdateinput.setText(date);

        String posterUri = event.getPosterUri();
        if (posterUri != null && !posterUri.isEmpty()) {
            Glide.with(context).load(posterUri).centerCrop().into(eventphoto);
        } else {
            eventphoto.setImageDrawable(null);
        }

        String status = eventIdToStatus.get(event.getEventId());
        // Show invitation CTA for lottery winners (SELECTED) and private event invites (INVITED)
        boolean needsResponse = "SELECTED".equalsIgnoreCase(status)
                || "INVITED".equalsIgnoreCase(status);
        if (needsResponse) {
            eventInvitationPrompt.setVisibility(View.VISIBLE);
            buttonviewdetail.setText("RESPOND TO INVITATION");
        } else {
            eventInvitationPrompt.setVisibility(View.GONE);
            buttonviewdetail.setText("VIEW DETAILS");
        }

        buttonviewdetail.setOnClickListener(v -> {
            Intent intent = new Intent(context, EventDetailsActivity.class);
            intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, event.getEventId());
            intent.putExtra(EventDetailsActivity.EXTRA_VIEW_AS_ENTRANT, true);
            context.startActivity(intent);
        });

        return view;
    }
}

