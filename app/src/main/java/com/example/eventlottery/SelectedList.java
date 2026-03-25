package com.example.eventlottery;

/**
 * Lists entrants with status SELECTED (and optionally ACCEPTED) for current event.
 * Event ID from EventEditActivity.getCurrentEventId().
 */
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class SelectedList extends Fragment {

    private final ArrayList<WaitingListEntry> selectedEntries = new ArrayList<>();
    private SelectedEntryAdapter adapter;
    private FirebaseFirestore db;
    private String eventId;

    public SelectedList() {
        super(R.layout.selected_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        eventId = EventEditActivity.getCurrentEventId(requireContext());

        ListView listSelectedEntrants = view.findViewById(R.id.listSelectedEntrants);

        adapter = new SelectedEntryAdapter(requireActivity(), selectedEntries, entry -> {
            db.collection("events")
                    .document(eventId)
                    .collection("waitingList")
                    .document(entry.getDeviceId())
                    .update("status", WaitingListEntry.Status.CANCELLED.name())
                    .addOnSuccessListener(unused -> loadSelectedEntries())
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed to cancel entrant", Toast.LENGTH_SHORT).show());
        });

        listSelectedEntrants.setAdapter(adapter);

        view.findViewById(R.id.buttonBack).setOnClickListener(v ->
                NavHostFragment.findNavController(SelectedList.this)
                        .navigate(R.id.Selected_list_to_OrganizerNavigationFragment));

        view.findViewById(R.id.buttonNotifySelected).setOnClickListener(v -> notifyChosenEntrants());

        loadSelectedEntries();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadSelectedEntries();
    }

    private void loadSelectedEntries() {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(getContext(), "No current event selected", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    selectedEntries.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments())
                    {
                        WaitingListEntry entry = doc.toObject(WaitingListEntry.class);
                        if (entry != null &&
                                WaitingListEntry.Status.SELECTED.name().equals(entry.getStatus())) {
                            if (entry.getDeviceId() == null || entry.getDeviceId().isEmpty()) {
                                entry.setDeviceId(doc.getId());
                            }
                            selectedEntries.add(entry);
                        }
                    }

                    resolveEntrantNamesAndNotify();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load selected entrants", Toast.LENGTH_SHORT).show());
    }

    /** Fetches entrant display names from users/{deviceId} and updates the adapter. Never exposes device ID in UI. */
    private void resolveEntrantNamesAndNotify() {
        Map<String, String> deviceIdToName = new HashMap<>();
        if (selectedEntries.isEmpty()) {
            adapter.setDeviceIdToName(deviceIdToName);
            adapter.notifyDataSetChanged();
            return;
        }
        AtomicInteger pending = new AtomicInteger(selectedEntries.size());
        for (WaitingListEntry entry : selectedEntries) {
            String deviceId = entry.getDeviceId();
            if (deviceId == null || deviceId.isEmpty()) {
                if (pending.decrementAndGet() == 0) {
                    adapter.setDeviceIdToName(deviceIdToName);
                    adapter.notifyDataSetChanged();
                }
                continue;
            }
            db.collection("users").document(deviceId).get()
                    .addOnSuccessListener(doc -> {
                        if (doc != null && doc.exists()) {
                            Entrant entrant = doc.toObject(Entrant.class);
                            deviceIdToName.put(deviceId, entrant != null ? entrant.getFullName() : "Unknown Entrant");
                        } else {
                            deviceIdToName.put(deviceId, "Unknown Entrant");
                        }
                        if (pending.decrementAndGet() == 0) {
                            adapter.setDeviceIdToName(deviceIdToName);
                            adapter.notifyDataSetChanged();
                        }
                    })
                    .addOnFailureListener(e -> {
                        deviceIdToName.put(deviceId, "Unknown Entrant");
                        if (pending.decrementAndGet() == 0) {
                            adapter.setDeviceIdToName(deviceIdToName);
                            adapter.notifyDataSetChanged();
                        }
                    });
        }
    }

    /**
     * Organizer action: send the same lottery-win / sign-up notification as the draw,
     * to every entrant currently in SELECTED status for this event.
     */
    private void notifyChosenEntrants() {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(getContext(), "No current event selected", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    String notificationGroupId = db.collection("notificationStorageAdmin")
                            .document()
                            .getId();
                    int sent = 0;
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        WaitingListEntry entry = doc.toObject(WaitingListEntry.class);
                        if (entry == null
                                || !WaitingListEntry.Status.SELECTED.name().equals(entry.getStatus())) {
                            continue;
                        }
                        String deviceId = entry.getDeviceId();
                        if (deviceId == null || deviceId.isEmpty()) {
                            deviceId = doc.getId();
                        }
                        if (deviceId == null || deviceId.isEmpty()) {
                            continue;
                        }
                        NotificationHelper.sendLotteryWinNotification(db, deviceId, eventId,notificationGroupId);
                        sent++;
                    }
                    if (sent == 0) {
                        Toast.makeText(getContext(), R.string.notify_chosen_entrants_none, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(),
                                getString(R.string.notify_chosen_entrants_sent, sent),
                                Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load selected entrants", Toast.LENGTH_SHORT).show());
    }
}