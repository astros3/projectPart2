package com.example.eventlottery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * RecyclerView adapter for organizer dashboard event cards. View/Edit open event nav or EventEditActivity.
 */
public class OrganizerEventAdapter extends RecyclerView.Adapter<OrganizerEventAdapter.EventCardViewHolder> {

    private final List<Event> events = new ArrayList<>();
    private OnEventActionListener listener;

    interface OnEventActionListener {
        void onViewClick(Event event);
        void onEditClick(Event event);
    }

    void setOnEventActionListener(OnEventActionListener listener) {
        this.listener = listener;
    }

    void setEvents(List<Event> newEvents) {
        events.clear();
        if (newEvents != null) {
            events.addAll(newEvents);
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public EventCardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_organizer_event_card, parent, false);
        return new EventCardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull EventCardViewHolder holder, int position) {
        Event event = events.get(position);
        holder.bind(event, listener);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventCardViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleView;
        private final TextView dateView;
        private final View viewButton;
        private final View editButton;

        EventCardViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.event_card_title);
            dateView = itemView.findViewById(R.id.event_card_date);
            viewButton = itemView.findViewById(R.id.event_card_view);
            editButton = itemView.findViewById(R.id.event_card_edit);
        }

        void bind(Event event, OnEventActionListener listener) {
            titleView.setText(event.getTitle() != null && !event.getTitle().isEmpty()
                    ? event.getTitle() : "Untitled Event");

            long millis = event.getEventDateMillis() > 0
                    ? event.getEventDateMillis()
                    : event.getRegistrationEndMillis();

            if (millis > 0) {
                dateView.setText(formatDateTime(millis));
                dateView.setVisibility(View.VISIBLE);
            } else {
                dateView.setVisibility(View.GONE);
            }

            viewButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onViewClick(event);
                }
            });

            editButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onEditClick(event);
                }
            });
        }

        private static String formatDateTime(long millis) {
            return new SimpleDateFormat("MMM d, yyyy - h:mm a", Locale.getDefault())
                    .format(new Date(millis));
        }
    }
}