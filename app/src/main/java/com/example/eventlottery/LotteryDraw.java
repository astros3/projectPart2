package com.example.eventlottery;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LotteryDraw extends Fragment {

    private FirebaseFirestore db;

    public LotteryDraw() {
        super(R.layout.lotterydraw);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        view.findViewById(R.id.buttonBack).setOnClickListener(v ->
                NavHostFragment.findNavController(LotteryDraw.this)
                        .navigate(R.id.LotteryDraw_to_OrganizerNavigationFragment)
        );

        EditText editTextNumber = view.findViewById(R.id.editTextNumber);
        View buttonDraw = view.findViewById(R.id.buttonDraw);
        TextView textViewResult = view.findViewById(R.id.textViewResult);

        buttonDraw.setOnClickListener(v -> {
            String input = editTextNumber.getText().toString().trim();

            if (TextUtils.isEmpty(input)) {
                Toast.makeText(getContext(), "Enter number of entrants", Toast.LENGTH_SHORT).show();
                return;
            }

            int count;
            try {
                count = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                Toast.makeText(getContext(), "Enter a valid number", Toast.LENGTH_SHORT).show();
                return;
            }

            if (count <= 0) {
                Toast.makeText(getContext(), "Number must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }

            String eventId = EventEditActivity.getCurrentEventId(requireContext());
            if (eventId == null || eventId.isEmpty()) {
                Toast.makeText(getContext(), "No current event selected", Toast.LENGTH_SHORT).show();
                return;
            }

            runLotteryDraw(eventId, count, textViewResult);
        });
    }

    private void runLotteryDraw(String eventId, int count, TextView textViewResult) {
        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<WaitingListEntry> pendingEntries = new ArrayList<>();

                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        WaitingListEntry entry = doc.toObject(WaitingListEntry.class);
                        if (entry != null && WaitingListEntry.Status.PENDING.name().equals(entry.getStatus())) {
                            pendingEntries.add(entry);
                        }
                    }

                    if (pendingEntries.isEmpty()) {
                        Toast.makeText(getContext(), "No entrants in waiting list", Toast.LENGTH_SHORT).show();
                        textViewResult.setText("Selected Entrants:\n\nNone");
                        return;
                    }

                    if (count > pendingEntries.size()) {
                        Toast.makeText(getContext(),
                                "Not enough entrants in waiting list",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Collections.shuffle(pendingEntries);

                    List<WaitingListEntry> selectedEntries =
                            new ArrayList<>(pendingEntries.subList(0, count));

                    WriteBatch batch = db.batch();

                    for (WaitingListEntry entry : selectedEntries) {
                        DocumentReference ref = db.collection("events")
                                .document(eventId)
                                .collection("waitingList")
                                .document(entry.getDeviceId());

                        batch.update(ref, "status", WaitingListEntry.Status.SELECTED.name());
                    }

                    batch.commit()
                            .addOnSuccessListener(unused -> showSelectedEntrants(selectedEntries, textViewResult))
                            .addOnFailureListener(e ->
                                    Toast.makeText(getContext(),
                                            "Failed to complete lottery draw",
                                            Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Failed to load waiting list",
                                Toast.LENGTH_SHORT).show());
    }

    private void showSelectedEntrants(List<WaitingListEntry> selectedEntries, TextView textViewResult) {
        StringBuilder result = new StringBuilder("Selected Entrants:\n\n");

        for (WaitingListEntry entry : selectedEntries) {
            result.append(entry.getDeviceId()).append("\n");
        }

        textViewResult.setText(result.toString());
    }
}