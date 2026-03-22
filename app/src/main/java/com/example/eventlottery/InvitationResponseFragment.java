package com.example.eventlottery;

/**
 * Invitation response UI: accept/decline for SELECTED entrants. Updates waitingList doc status
 * to ACCEPTED or DECLINED. Requires eventId (e.g. from nav args).
 */
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.firestore.FirebaseFirestore;

public class InvitationResponseFragment extends Fragment {

    private FirebaseFirestore db;
    private String eventId;
    private String deviceId;

    public InvitationResponseFragment() {
        super(R.layout.invitation_response);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        deviceId = DeviceIdManager.getDeviceId(requireContext());
        eventId = resolveEventId();

        Button btnJoinAgain = view.findViewById(R.id.buttonRequestJoinAgain);
        TextView statusHeader = view.findViewById(R.id.textStatusHeader);
        TextView statusDetail = view.findViewById(R.id.textStatusDetail);

        btnJoinAgain.setVisibility(View.GONE);

        btnJoinAgain.setOnClickListener(v -> requestJoinAgain(btnJoinAgain, statusHeader, statusDetail));

        loadCurrentStatus(statusHeader, statusDetail, btnJoinAgain);
    }

    @Override
    public void onResume() {
        super.onResume();

        View view = getView();
        if (view == null) return;

        Button btnJoinAgain = view.findViewById(R.id.buttonRequestJoinAgain);
        TextView statusHeader = view.findViewById(R.id.textStatusHeader);
        TextView statusDetail = view.findViewById(R.id.textStatusDetail);

        loadCurrentStatus(statusHeader, statusDetail, btnJoinAgain);
    }

    private String resolveEventId() {
        Bundle args = getArguments();
        if (args != null) {
            String argEventId = args.getString("eventId");
            if (argEventId != null && !argEventId.trim().isEmpty()) {
                return argEventId;
            }
        }
        return EventEditActivity.getCurrentEventId(requireContext());
    }

    private void loadCurrentStatus(TextView statusHeader, TextView statusDetail, Button btnJoinAgain) {
        if (eventId == null || eventId.trim().isEmpty()) {
            statusHeader.setText("STATUS: UNKNOWN");
            statusDetail.setText("No event selected.");
            btnJoinAgain.setVisibility(View.GONE);
            return;
        }

        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        statusHeader.setText("STATUS: NOT REGISTERED");
                        statusDetail.setText("You are not currently registered for this event.");
                        btnJoinAgain.setVisibility(View.GONE);
                        return;
                    }

                    WaitingListEntry entry = documentSnapshot.toObject(WaitingListEntry.class);
                    if (entry == null || entry.getStatus() == null) {
                        statusHeader.setText("STATUS: UNKNOWN");
                        statusDetail.setText("Could not determine your application status.");
                        btnJoinAgain.setVisibility(View.GONE);
                        return;
                    }

                    String status = entry.getStatus();

                    switch (status) {
                        case "SELECTED":
                            statusHeader.setText("STATUS: SELECTED");
                            statusDetail.setText("Congratulations! You have been invited to this event.");
                            btnJoinAgain.setVisibility(View.GONE);
                            break;

                        case "ACCEPTED":
                            statusHeader.setText("STATUS: ACCEPTED");
                            statusDetail.setText("Your registration has been confirmed.");
                            btnJoinAgain.setVisibility(View.GONE);
                            break;

                        case "DECLINED":
                            statusHeader.setText("STATUS: DECLINED");
                            statusDetail.setText("You declined the invitation. You can join the waiting list again.");
                            btnJoinAgain.setVisibility(View.VISIBLE);
                            break;

                        case "CANCELLED":
                            statusHeader.setText("STATUS: CANCELLED");
                            statusDetail.setText("Your previous selection is no longer active. You can join the waiting list again.");
                            btnJoinAgain.setVisibility(View.VISIBLE);
                            break;

                        case "PENDING":
                        case "WAITING":
                            statusHeader.setText("STATUS: WAITING");
                            statusDetail.setText("You are currently on the waiting list.");
                            btnJoinAgain.setVisibility(View.GONE);
                            break;

                        default:
                            statusHeader.setText("STATUS: UNKNOWN");
                            statusDetail.setText("Current status: " + status);
                            btnJoinAgain.setVisibility(View.GONE);
                            break;
                    }
                })
                .addOnFailureListener(e -> {
                    statusHeader.setText("STATUS: ERROR");
                    statusDetail.setText("Failed to load your invitation status.");
                    btnJoinAgain.setVisibility(View.GONE);
                });
    }

    private void requestJoinAgain(Button btnJoinAgain, TextView statusHeader, TextView statusDetail) {
        if (eventId == null || eventId.trim().isEmpty()) {
            Toast.makeText(requireContext(), "No event selected", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events")
                .document(eventId)
                .collection("waitingList")
                .document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        WaitingListEntry newEntry =
                                new WaitingListEntry(deviceId, WaitingListEntry.Status.PENDING);

                        db.collection("events")
                                .document(eventId)
                                .collection("waitingList")
                                .document(deviceId)
                                .set(newEntry)
                                .addOnSuccessListener(unused -> {
                                    Toast.makeText(requireContext(),
                                            "You are back in the waiting list!",
                                            Toast.LENGTH_SHORT).show();
                                    btnJoinAgain.setVisibility(View.GONE);
                                    statusHeader.setText("STATUS: WAITING");
                                    statusDetail.setText("You are currently on the waiting list.");
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(requireContext(),
                                                "Failed to rejoin waiting list",
                                                Toast.LENGTH_SHORT).show());
                        return;
                    }

                    db.collection("events")
                            .document(eventId)
                            .collection("waitingList")
                            .document(deviceId)
                            .update("status", WaitingListEntry.Status.PENDING.name())
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(requireContext(),
                                        "You are back in the waiting list!",
                                        Toast.LENGTH_SHORT).show();
                                btnJoinAgain.setVisibility(View.GONE);
                                statusHeader.setText("STATUS: WAITING");
                                statusDetail.setText("You are currently on the waiting list.");
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(requireContext(),
                                            "Failed to rejoin waiting list",
                                            Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Failed to load your application",
                                Toast.LENGTH_SHORT).show());
    }
}