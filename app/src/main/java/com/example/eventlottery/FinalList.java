package com.example.eventlottery;

/**
 * Lists entrants with status ACCEPTED (those who accepted the invitation) for current event.
 * Same structure as SelectedList but filtered by ACCEPTED. Event ID from EventEditActivity.getCurrentEventId().
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

public class FinalList extends Fragment {

    private final ArrayList<WaitingListEntry> acceptedEntries = new ArrayList<>();
    private SelectedEntryAdapter adapter;
    private FirebaseFirestore db;
    private String eventId;

    public FinalList() {
        super(R.layout.final_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        eventId = EventEditActivity.getCurrentEventId(requireContext());

        ListView listFinalEntrants = view.findViewById(R.id.listFinalEntrants);

        adapter = new SelectedEntryAdapter(requireActivity(), acceptedEntries, entry -> {
            db.collection("events")
                    .document(eventId)
                    .collection("waitingList")
                    .document(entry.getDeviceId())
                    .update("status", WaitingListEntry.Status.CANCELLED.name())
                    .addOnSuccessListener(unused -> loadAcceptedEntries())
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed to remove from final list", Toast.LENGTH_SHORT).show());
        });

        listFinalEntrants.setAdapter(adapter);

        view.findViewById(R.id.buttonBack).setOnClickListener(v ->
                NavHostFragment.findNavController(FinalList.this)
                        .navigate(R.id.Final_list_to_OrganizerNavigationFragment));

        view.findViewById(R.id.buttonNotifyCancelled).setOnClickListener(v -> notifyCancelledEntrants());

        loadAcceptedEntries();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadAcceptedEntries();
    }

    private void loadAcceptedEntries() {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(getContext(), "No current event selected", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    acceptedEntries.clear();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        WaitingListEntry entry = doc.toObject(WaitingListEntry.class);
                        if (entry != null &&
                                WaitingListEntry.Status.ACCEPTED.name().equals(entry.getStatus())) {
                            if (entry.getDeviceId() == null || entry.getDeviceId().isEmpty()) {
                                entry.setDeviceId(doc.getId());
                            }
                            acceptedEntries.add(entry);
                        }
                    }

                    resolveEntrantNamesAndNotify();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load final list", Toast.LENGTH_SHORT).show());
    }

    //finds all the entrants, filters out the cancelled and sends a notification to each of them
    private void notifyCancelledEntrants() {
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

                        // ONLY cancelled entrants
                        if (entry == null ||
                                !WaitingListEntry.Status.CANCELLED.name().equals(entry.getStatus())) {
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
                        Toast.makeText(getContext(), "No cancelled entrants to notify", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Notified " + sent + " cancelled entrants", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load cancelled entrants", Toast.LENGTH_SHORT).show());
    }

    /** Fetches entrant display names from users/{deviceId} and updates the adapter. Never exposes device ID in UI. */
    private void resolveEntrantNamesAndNotify() {
        Map<String, String> deviceIdToName = new HashMap<>();
        if (acceptedEntries.isEmpty()) {
            adapter.setDeviceIdToName(deviceIdToName);
            adapter.notifyDataSetChanged();
            return;
        }
        AtomicInteger pending = new AtomicInteger(acceptedEntries.size());
        for (WaitingListEntry entry : acceptedEntries) {
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
}
