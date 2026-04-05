package com.example.eventlottery;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Invitation flow entry point: loads the event and waiting-list status for the current device.
 * Lets the entrant accept or decline a SELECTED invitation.
 */
public class InvitationInitialFragment extends Fragment {

    private FirebaseFirestore db;
    private String eventId;
    private String deviceId;

    /**
     * Creates a new InvitationInitialFragment and binds it to the invitation initial layout.
     */
    public InvitationInitialFragment() {
        super(R.layout.fragment_invitation_initial);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        deviceId = DeviceIdManager.getDeviceId(requireContext());
        eventId = resolveEventId();

        Button btnAccept = view.findViewById(R.id.buttonAccept);
        Button btnDecline = view.findViewById(R.id.buttonDecline);

        btnAccept.setOnClickListener(v -> updateInvitationStatus(WaitingListEntry.Status.ACCEPTED));
        btnDecline.setOnClickListener(v -> updateInvitationStatus(WaitingListEntry.Status.DECLINED));
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

    private void updateInvitationStatus(WaitingListEntry.Status newStatus) {
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
                        Toast.makeText(requireContext(), "Invitation record not found", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    WaitingListEntry entry = documentSnapshot.toObject(WaitingListEntry.class);
                    if (entry == null) {
                        Toast.makeText(requireContext(), "Could not read invitation", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String currentStatus = entry.getStatus();

                    if (!WaitingListEntry.Status.SELECTED.name().equals(currentStatus)) {
                        Toast.makeText(
                                requireContext(),
                                "This invitation is no longer active",
                                Toast.LENGTH_SHORT
                        ).show();
                        return;
                    }

                    db.collection("events")
                            .document(eventId)
                            .collection("waitingList")
                            .document(deviceId)
                            .update("status", newStatus.name())
                            .addOnSuccessListener(unused -> {
                                String message = (newStatus == WaitingListEntry.Status.ACCEPTED)
                                        ? "Registration confirmed!"
                                        : "Invitation declined.";
                                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
                                NavHostFragment.findNavController(InvitationInitialFragment.this).popBackStack();
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(requireContext(),
                                            "Failed to update invitation",
                                            Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e ->
                        Toast.makeText(requireContext(),
                                "Failed to load invitation",
                                Toast.LENGTH_SHORT).show());
    }
}