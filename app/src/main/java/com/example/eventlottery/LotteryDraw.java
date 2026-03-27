package com.example.eventlottery;

/**
 * Organizer lottery: enter count N, randomly select N PENDING waiting-list entrants and set
 * their status to SELECTED. Notifies winners (US 01.04.01) and losers (US 01.04.02) via
 * NotificationHelper after the draw. Uses EventEditActivity.getCurrentEventId().
 */
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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

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
                    final List<WaitingListEntry> pendingEntries = new ArrayList<>();

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

                    final List<WaitingListEntry> selectedEntries =
                            new ArrayList<>(pendingEntries.subList(0, count));

                    WriteBatch batch = db.batch();

                    long invitedAt = System.currentTimeMillis();
                    for (WaitingListEntry entry : selectedEntries) {
                        DocumentReference ref = db.collection("events")
                                .document(eventId)
                                .collection("waitingList")
                                .document(entry.getDeviceId());

                        Map<String, Object> updates = new HashMap<>();
                        updates.put("status", WaitingListEntry.Status.SELECTED.name());
                        updates.put("invitationSentMillis", invitedAt);
                        batch.update(ref, updates);
                    }

                    batch.commit()
                            .addOnSuccessListener(unused -> {

                                // Build set of selected device IDs for fast lookup
                                Set<String> selectedIds = new HashSet<>();
                                for (WaitingListEntry e : selectedEntries) {
                                    selectedIds.add(e.getDeviceId());
                                }

                                // US 01.04.01 — notify winners
                                for (WaitingListEntry entry : selectedEntries) {
                                    NotificationHelper.sendLotteryWinNotification(
                                            db, entry.getDeviceId(), eventId);
                                }

                                // US 01.04.02 — notify losers (pending but not selected)
                                for (WaitingListEntry entry : pendingEntries) {
                                    if (!selectedIds.contains(entry.getDeviceId())) {
                                        NotificationHelper.sendNotification(
                                                db,
                                                entry.getDeviceId(),
                                                NotificationHelper.TYPE_LOTTERY_LOST,
                                                "Lottery result",
                                                "Unfortunately, you were not selected in this draw. You may still get a chance if someone declines their invitation.",
                                                eventId
                                        );
                                    }
                                }

                                showSelectedEntrants(selectedEntries, textViewResult);
                            })
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
        textViewResult.setText("Selected Entrants:\n\nLoading...");
        if (selectedEntries.isEmpty()) {
            textViewResult.setText("Selected Entrants:\n\nNone");
            return;
        }
        final String[] names = new String[selectedEntries.size()];
        AtomicInteger pending = new AtomicInteger(selectedEntries.size());
        for (int i = 0; i < selectedEntries.size(); i++) {
            final int index = i;
            String deviceId = selectedEntries.get(i).getDeviceId();
            if (deviceId == null || deviceId.isEmpty()) {
                names[index] = "Unknown Entrant";
                if (pending.decrementAndGet() == 0) {
                    buildResultAndSet(names, textViewResult);
                }
                continue;
            }
            db.collection("users").document(deviceId).get()
                    .addOnSuccessListener(doc -> {
                        if (doc != null && doc.exists()) {
                            Entrant entrant = doc.toObject(Entrant.class);
                            names[index] = entrant != null ? entrant.getFullName() : "Unknown Entrant";
                        } else {
                            names[index] = "Unknown Entrant";
                        }
                        if (pending.decrementAndGet() == 0) {
                            buildResultAndSet(names, textViewResult);
                        }
                    })
                    .addOnFailureListener(e -> {
                        names[index] = "Unknown Entrant";
                        if (pending.decrementAndGet() == 0) {
                            buildResultAndSet(names, textViewResult);
                        }
                    });
        }
    }

    private void buildResultAndSet(String[] names, TextView textViewResult) {
        StringBuilder result = new StringBuilder("Selected Entrants:\n\n");
        for (String name : names) {
            result.append(name != null ? name : "Unknown Entrant").append("\n");
        }
        textViewResult.setText(result.toString());
    }
}