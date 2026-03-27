package com.example.eventlottery;

import android.content.Context;
import android.widget.Toast;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Randomly promotes one PENDING entrant to SELECTED (same updates as main lottery draw).
 * Notifies only the chosen entrant; does not send lottery-loss notifications to others.
 */
public final class LotteryRedrawHelper {

    private LotteryRedrawHelper() {}

    public static void redrawOneFromPending(Context context,
                                            FirebaseFirestore db,
                                            String eventId,
                                            Runnable onSuccess) {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(context, "No event selected", Toast.LENGTH_SHORT).show();
            return;
        }
        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<WaitingListEntry> pendingEntries = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        WaitingListEntry entry = doc.toObject(WaitingListEntry.class);
                        if (entry != null
                                && WaitingListEntry.Status.PENDING.name().equals(entry.getStatus())) {
                            if (entry.getDeviceId() == null || entry.getDeviceId().isEmpty()) {
                                entry.setDeviceId(doc.getId());
                            }
                            pendingEntries.add(entry);
                        }
                    }
                    if (pendingEntries.isEmpty()) {
                        Toast.makeText(context, R.string.redraw_replacement_no_pending, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    Collections.shuffle(pendingEntries);
                    WaitingListEntry picked = pendingEntries.get(0);
                    String deviceId = picked.getDeviceId();
                    if (deviceId == null || deviceId.isEmpty()) {
                        Toast.makeText(context, R.string.redraw_replacement_failed, Toast.LENGTH_SHORT).show();
                        return;
                    }
                    long invitedAt = System.currentTimeMillis();
                    DocumentReference ref = db.collection("events")
                            .document(eventId)
                            .collection("waitingList")
                            .document(deviceId);
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("status", WaitingListEntry.Status.SELECTED.name());
                    updates.put("invitationSentMillis", invitedAt);
                    ref.update(updates)
                            .addOnSuccessListener(unused -> {
                                NotificationHelper.sendLotteryWinNotification(db, deviceId, eventId);
                                Toast.makeText(context, R.string.redraw_replacement_success, Toast.LENGTH_SHORT).show();
                                if (onSuccess != null) {
                                    onSuccess.run();
                                }
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(context, R.string.redraw_replacement_failed, Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, R.string.redraw_replacement_failed, Toast.LENGTH_SHORT).show());
    }
}
