package com.example.eventlottery;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * RecyclerView adapter for organizer dashboard event cards. View/Edit open event nav or EventEditActivity.
 * For co-organized events (where deviceId != organizerId), the delete button is hidden.
 */
public class OrganizerEventAdapter extends RecyclerView.Adapter<OrganizerEventAdapter.EventCardViewHolder> {

    /** No-arg constructor required by the RecyclerView.Adapter framework. */
    public OrganizerEventAdapter() {}

    private final List<Event> events = new ArrayList<>();
    private OnEventActionListener listener;
    private String currentDeviceId = "";

    interface OnEventActionListener {
        void onViewClick(Event event);
        void onEditClick(Event event);
        void onDeleteClick(Event event);
    }

    void setOnEventActionListener(OnEventActionListener listener) {
        this.listener = listener;
    }

    void setCurrentDeviceId(String deviceId) {
        this.currentDeviceId = deviceId != null ? deviceId : "";
    }

    void setEvents(List<Event> newEvents) {
        events.clear();
        if (newEvents != null) {
            events.addAll(newEvents);
        }
        notifyDataSetChanged();
    }
    void removeEventById(String eventId) {
        if (eventId == null) return;

        for (int i = 0; i < events.size(); i++) {
            Event event = events.get(i);
            if (event != null && eventId.equals(event.getEventId())) {
                events.remove(i);
                notifyItemRemoved(i);
                return;
            }
        }
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
        boolean isCoOrganized = !currentDeviceId.isEmpty()
                && !currentDeviceId.equals(event.getOrganizerId());
        holder.bind(event, listener, isCoOrganized);
    }

    @Override
    public int getItemCount() {
        return events.size();
    }

    static class EventCardViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleView;
        private final TextView dateView;
        private final TextView coOrgBadge;
        private final View viewButton;
        private final View editButton;
        private final View deleteButton;

        EventCardViewHolder(View itemView) {
            super(itemView);
            titleView = itemView.findViewById(R.id.event_card_title);
            dateView = itemView.findViewById(R.id.event_card_date);
            coOrgBadge = itemView.findViewById(R.id.event_card_co_org_badge);
            viewButton = itemView.findViewById(R.id.event_card_view);
            editButton = itemView.findViewById(R.id.event_card_edit);
            deleteButton = itemView.findViewById(R.id.event_card_delete);
        }

        void bind(Event event, OnEventActionListener listener, boolean isCoOrganized) {
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

            // Show co-organizer badge and hide delete for co-organized events
            if (isCoOrganized) {
                coOrgBadge.setVisibility(View.VISIBLE);
                deleteButton.setVisibility(View.GONE);
            } else {
                coOrgBadge.setVisibility(View.GONE);
                deleteButton.setVisibility(View.VISIBLE);
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

            deleteButton.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onDeleteClick(event);
                }
            });
        }


        private static String formatDateTime(long millis) {
            return new SimpleDateFormat("MMM d, yyyy - h:mm a", Locale.getDefault())
                    .format(new Date(millis));
        }
    }

    private void sendInvitationNotification(String entrantDeviceId, String eventId, String eventName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        final String title = "Co-Organizer Invitation";
        final String message = "You've been invited to help manage: " + eventName;

        Map<String, Object> notif = new HashMap<>();
        notif.put("type", "INVITATION");
        notif.put("title", title);
        notif.put("message", message);
        notif.put("eventId", eventId);
        notif.put("timestampMillis", System.currentTimeMillis());
        notif.put("read", false);

        db.collection("users").document(entrantDeviceId)
                .collection("notifications").add(notif)
                .addOnSuccessListener(docRef -> {
                    NotificationHelper.NotificationMAINstorageForAdmin(
                            db, title, message, eventId, entrantDeviceId);
                    Toast.makeText(docRef.getFirestore().getApp().getApplicationContext(),
                            "Invitation sent to Entrant!", Toast.LENGTH_SHORT).show();
                });
    }
}