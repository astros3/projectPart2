package com.example.eventlottery;

/**
 * Shows PENDING entries in events/{eventId}/waitingList. Event ID from EventEditActivity.getCurrentEventId().
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

public class WaitingListFragment extends Fragment {

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
                            waitingEntries.add(entry);
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load waiting list", Toast.LENGTH_SHORT).show());
    }
}