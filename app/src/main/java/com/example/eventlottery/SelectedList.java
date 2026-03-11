package com.example.eventlottery;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

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
                            Toast.makeText(getContext(), "Failed to remove selected entrant", Toast.LENGTH_SHORT).show());
        });

        listSelectedEntrants.setAdapter(adapter);

        view.findViewById(R.id.buttonBack).setOnClickListener(v ->
                NavHostFragment.findNavController(SelectedList.this)
                        .navigate(R.id.Selected_list_to_OrganizerNavigationFragment));

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

                    for (var doc : querySnapshot.getDocuments()) {
                        WaitingListEntry entry = doc.toObject(WaitingListEntry.class);
                        if (entry != null &&
                                WaitingListEntry.Status.SELECTED.name().equals(entry.getStatus())) {
                            selectedEntries.add(entry);
                        }
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to load selected entrants", Toast.LENGTH_SHORT).show());
    }
}