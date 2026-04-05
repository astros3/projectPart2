package com.example.eventlottery;

/**
 * Shows PENDING entries in events/{eventId}/waitingList. Event ID from EventEditActivity.getCurrentEventId().
 */
import android.os.Bundle;
import android.util.Log;
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

public class WaitingListFragment extends Fragment {

    private static final String TAG = "ViewGeolocation";

    private final ArrayList<WaitingListEntry> waitingEntries = new ArrayList<>();
    private WaitingEntryAdapter adapter;
    private FirebaseFirestore db;
    private String eventId;

    public WaitingListFragment() {
        super(R.layout.waiting_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        eventId = EventEditActivity.getCurrentEventId(requireContext());

        ListView listView = view.findViewById(R.id.listWaitingEntrants);
        adapter = new WaitingEntryAdapter(requireActivity(), waitingEntries);
        listView.setAdapter(adapter);

        view.findViewById(R.id.buttonBack).setOnClickListener(v ->
                NavHostFragment.findNavController(WaitingListFragment.this)
                        .navigate(R.id.Waiting_list_to_OrganizerNavigationFragment));

        view.findViewById(R.id.buttonNotifyWaiting).setOnClickListener(v -> notifyWaitingEntrants());

        loadWaitingEntries();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadWaitingEntries();
    }

    private void loadWaitingEntries() {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(getContext(), "No current event selected", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    waitingEntries.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        WaitingListEntry entry = doc.toObject(WaitingListEntry.class);
                        if (entry != null &&
                                WaitingListEntry.Status.PENDING.name().equals(entry.getStatus())) {
                            if (entry.getDeviceId() == null || entry.getDeviceId().isEmpty()) {
                                entry.setDeviceId(doc.getId());
                                Log.d(TAG, "WaitingList: set deviceId from doc id for entry");
                            }
                            waitingEntries.add(entry);
                        }
                    }
                    Log.d(TAG, "WaitingList: loaded " + waitingEntries.size() + " PENDING entries");
                    resolveEntrantNamesAndNotify();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "WaitingList: load failed", e);
                    Toast.makeText(getContext(), "Failed to load waiting list", Toast.LENGTH_SHORT).show();
                });
    }

    /** Fetches entrant display names from users/{deviceId} and updates the adapter. */
    private void resolveEntrantNamesAndNotify() {
        Map<String, String> deviceIdToName = new HashMap<>();
        if (waitingEntries.isEmpty()) {
            adapter.setDeviceIdToName(deviceIdToName);
            adapter.notifyDataSetChanged();
            return;
        }
        AtomicInteger pending = new AtomicInteger(waitingEntries.size());
        for (WaitingListEntry entry : waitingEntries) {
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

    /** Notifies each PENDING waiting-list entrant with NotificationHelper.sendWaitingListUpdateNotification. */
    private void notifyWaitingEntrants() {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(getContext(), "No current event selected", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int sent = 0;
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        WaitingListEntry entry = doc.toObject(WaitingListEntry.class);

                        // ONLY waiting list entrants
                        if (entry == null ||
                                !WaitingListEntry.Status.PENDING.name().equals(entry.getStatus())) {
                            continue;
                        }

                        String deviceId = entry.getDeviceId();
                        if (deviceId == null || deviceId.isEmpty()) {
                            deviceId = doc.getId();
                        }
                        if (deviceId == null || deviceId.isEmpty()) {
                            continue;
                        }

                        NotificationHelper.sendWaitingListUpdateNotification(
                                db, requireContext(), deviceId, eventId);
                        sent++;
                    }

                    if (sent == 0) {
                        Toast.makeText(getContext(), "No entrants in waiting list to notify", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Notified " + sent + " waiting entrants", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load waiting entrants", Toast.LENGTH_SHORT).show());
    }
}