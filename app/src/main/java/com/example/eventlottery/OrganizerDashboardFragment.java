package com.example.eventlottery;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Organizer dashboard: lists events where organizerId == deviceId. Create event, open event nav, or profile.
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
        adapter.setCurrentDeviceId(deviceId);
        recyclerView.setAdapter(adapter);

        adapter.setOnEventActionListener(new OrganizerEventAdapter.OnEventActionListener() {
            @Override
            public void onViewClick(Event event) {
                openEventNavigation(event);
            }

            @Override
            public void onEditClick(Event event) {
                openEditEvent(event);
            }

            @Override
            public void onDeleteClick(Event event) {
                deleteEvent(event);

            }
        });

        view.findViewById(R.id.btn_create_event).setOnClickListener(v -> onCreateEventClick());
        view.findViewById(R.id.btn_profile).setOnClickListener(v -> onProfileClick());

        view.findViewById(R.id.back_button).setOnClickListener(v -> {
            startActivity(new Intent(requireContext(), WelcomePageActivity.class));
            requireActivity().finish();
        });

        loadMyEvents();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadMyEvents();
    }

    private void loadMyEvents() {
        final List<Event> mergedEvents = new ArrayList<>();
        final boolean[] queriesDone = {false, false};

        // Query 1: events owned by this device
        db.collection("events")
                .whereEqualTo("organizerId", deviceId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            event.setEventId(doc.getId());
                            mergedEvents.add(event);
                        }
                    }
                    queriesDone[0] = true;
                    if (queriesDone[1]) onBothQueriesDone(mergedEvents);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Failed to load owned events", e);
                    queriesDone[0] = true;
                    if (queriesDone[1]) onBothQueriesDone(mergedEvents);
                });

        // Query 2: events where this device is a co-organizer
        db.collection("events")
                .whereArrayContains("coOrganizerIds", deviceId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    for (QueryDocumentSnapshot doc : snapshot) {
                        Event event = doc.toObject(Event.class);
                        if (event != null) {
                            event.setEventId(doc.getId());
                            // Deduplicate — shouldn't occur but defensive
                            boolean alreadyAdded = false;
                            for (Event e : mergedEvents) {
                                if (doc.getId().equals(e.getEventId())) {
                                    alreadyAdded = true;
                                    break;
                                }
                            }
                            if (!alreadyAdded) mergedEvents.add(event);
                        }
                    }
                    queriesDone[1] = true;
                    if (queriesDone[0]) onBothQueriesDone(mergedEvents);
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Failed to load co-organized events", e);
                    queriesDone[1] = true;
                    if (queriesDone[0]) onBothQueriesDone(mergedEvents);
                });
    }

    private void onBothQueriesDone(List<Event> events) {
        if (!isAdded()) return;
        Collections.sort(events, (a, b) -> Long.compare(
                b.getEventDateMillis() != 0 ? b.getEventDateMillis() : b.getRegistrationEndMillis(),
                a.getEventDateMillis() != 0 ? a.getEventDateMillis() : a.getRegistrationEndMillis()));
        adapter.setEvents(events);
        showEmptyState(events.isEmpty());
    }

    private void showEmptyState(boolean empty) {
        if (recyclerView == null || emptyView == null) return;
        recyclerView.setVisibility(empty ? View.GONE : View.VISIBLE);
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
    }

    private void openViewEvent(Event event) {
        if (event == null || event.getEventId() == null) return;

        Bundle bundle = new Bundle();
        bundle.putString("eventId", event.getEventId());

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_dashboard_to_navigation, bundle);
    }

    private void openEventNavigation(Event event) {
        if (event == null || event.getEventId() == null) return;

        EventEditActivity.setCurrentEventId(requireContext(), event.getEventId());

        Bundle bundle = new Bundle();
        bundle.putString("eventId", event.getEventId());
        bundle.putBoolean(EventDetailsActivity.EXTRA_VIEW_AS_ENTRANT, false);

        NavHostFragment.findNavController(this)
                .navigate(R.id.action_dashboard_to_navigation, bundle);
    }

    private void openEditEvent(Event event) {
        if (event == null || event.getEventId() == null) return;

        EventEditActivity.setCurrentEventId(requireContext(), event.getEventId());
        startActivity(EventEditActivity.newIntent(requireContext(), event.getEventId()));
    }

    private void onCreateEventClick() {
        startActivity(EventEditActivity.newIntent(requireContext(), null));
    }

    private void onProfileClick() {
        startActivity(new Intent(requireContext(), OrganizerProfileActivity.class));
    }

    private void deleteEvent(Event event) {
        if (event == null || event.getEventId() == null) {
            Toast.makeText(requireContext(), "Event ID is missing", Toast.LENGTH_SHORT).show();
            return;
        }
        // Only the primary organizer can delete
        if (!deviceId.equals(event.getOrganizerId())) {
            Toast.makeText(requireContext(), "Only the event organizer can delete this event",
                    Toast.LENGTH_SHORT).show();
            return;
        }

        Log.d("DeleteEvent", "Trying to delete eventId = " + event.getEventId());

        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Event")
                .setMessage("Are you sure you want to delete this event?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    db.collection("events")
                            .document(event.getEventId())
                            .delete()
                            .addOnSuccessListener(unused -> {
                                adapter.removeEventById(event.getEventId());
                                showEmptyState(adapter.getItemCount() == 0);
                                Toast.makeText(requireContext(), "Event deleted", Toast.LENGTH_SHORT).show();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("DeleteEvent", "Delete failed", e);
                                Toast.makeText(requireContext(),
                                        "Failed to delete event: " + e.getMessage(),
                                        Toast.LENGTH_LONG).show();
                            });
                })   // ← CLOSE setPositiveButton HERE
                .setNegativeButton("Cancel", null)
                .show();}}