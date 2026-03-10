package com.example.eventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Organizer landing screen: list of events created by the current organizer,
 * with Create a New Event and Profile in the bottom bar.
 */
public class OrganizerDashboardFragment extends Fragment {

    private FirebaseFirestore db;
    private String deviceId;
    private OrganizerEventAdapter adapter;
    private RecyclerView recyclerView;
    private View emptyView;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_organizer_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        deviceId = DeviceIdManager.getDeviceId(requireContext());

        recyclerView = view.findViewById(R.id.recycler_organizer_events);
        emptyView = view.findViewById(R.id.empty_events_message);
        adapter = new OrganizerEventAdapter();
        recyclerView.setAdapter(adapter);

        adapter.setOnEventCardClickListener(this::onEditViewClick);

        view.findViewById(R.id.btn_create_event).setOnClickListener(v -> onCreateEventClick());
        view.findViewById(R.id.btn_profile).setOnClickListener(v -> onProfileClick());

        loadMyEvents();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMyEvents();
    }

    private void loadMyEvents() {
        db.collection("events")
                .whereEqualTo("organizerId", deviceId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<Event> events = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            event.setEventId(doc.getId());
                            events.add(event);
                        }
                    }
                    Collections.sort(events, (a, b) -> Long.compare(
                            b.getEventDateMillis() != 0 ? b.getEventDateMillis() : b.getRegistrationEndMillis(),
                            a.getEventDateMillis() != 0 ? a.getEventDateMillis() : a.getRegistrationEndMillis()));
                    adapter.setEvents(events);
                    showEmptyState(events.isEmpty());
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(requireContext(), "Could not load events", Toast.LENGTH_SHORT).show();
                    showEmptyState(true);
                });
    }

    private void showEmptyState(boolean empty) {
        if (recyclerView == null || emptyView == null) return;
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void onEditViewClick(Event event) {
        if (event.getEventId() == null) return;
        EventEditActivity.setCurrentEventId(requireContext(), event.getEventId());
        startActivity(EventEditActivity.newIntent(requireContext(), event.getEventId()));
    }

    private void onCreateEventClick() {
        startActivity(EventEditActivity.newIntent(requireContext(), null));
    }

    private void onProfileClick() {
        startActivity(new Intent(requireContext(), OrganizerProfileActivity.class));
    }
}
