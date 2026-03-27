package com.example.eventlottery;

/**
 * Lists entrants with status DECLINED or CANCELLED (Option B) for the current event.
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

public class CancelledListFragment extends Fragment {

    private final ArrayList<WaitingListEntry> cancelledEntries = new ArrayList<>();
    private SelectedEntryAdapter adapter;
    private FirebaseFirestore db;
    private String eventId;

    public CancelledListFragment() {
        super(R.layout.cancelled_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        eventId = EventEditActivity.getCurrentEventId(requireContext());

        ListView listView = view.findViewById(R.id.listCancelledEntrants);
        adapter = new SelectedEntryAdapter(requireActivity(), cancelledEntries, null);
        listView.setAdapter(adapter);

        view.findViewById(R.id.buttonBack).setOnClickListener(v ->
                NavHostFragment.findNavController(CancelledListFragment.this)
                        .navigate(R.id.Cancelled_list_to_OrganizerNavigationFragment));

        view.findViewById(R.id.buttonRedrawReplacement).setOnClickListener(v -> {
            if (eventId == null || eventId.isEmpty()) {
                Toast.makeText(requireContext(), "No current event selected", Toast.LENGTH_SHORT).show();
                return;
            }
            if (cancelledEntries.isEmpty()) {
                Toast.makeText(requireContext(), R.string.cannot_draw_replacement_applicant,
                        Toast.LENGTH_SHORT).show();
                return;
            }
            LotteryRedrawHelper.redrawOneFromPending(requireContext(), db, eventId,
                    CancelledListFragment.this::loadCancelledEntries);
        });

        loadCancelledEntries();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCancelledEntries();
    }

    private void loadCancelledEntries() {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(getContext(), "No current event selected", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    cancelledEntries.clear();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        WaitingListEntry entry = doc.toObject(WaitingListEntry.class);
                        if (entry == null) continue;
                        String st = entry.getStatus();
                        if (WaitingListEntry.Status.DECLINED.name().equals(st)
                                || WaitingListEntry.Status.CANCELLED.name().equals(st)) {
                            if (entry.getDeviceId() == null || entry.getDeviceId().isEmpty()) {
                                entry.setDeviceId(doc.getId());
                            }
                            cancelledEntries.add(entry);
                        }
                    }
                    resolveEntrantNamesAndNotify();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load cancelled entrants", Toast.LENGTH_SHORT).show());
    }

    private void resolveEntrantNamesAndNotify() {
        Map<String, String> deviceIdToName = new HashMap<>();
        if (cancelledEntries.isEmpty()) {
            adapter.setDeviceIdToName(deviceIdToName);
            adapter.notifyDataSetChanged();
            return;
        }
        AtomicInteger pending = new AtomicInteger(cancelledEntries.size());
        for (WaitingListEntry entry : cancelledEntries) {
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
