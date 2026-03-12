package com.example.eventlottery;

import static androidx.core.content.ContentProviderCompat.requireContext;

import static java.security.AccessController.getContext;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class InvitationInitialFragment extends Fragment {
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Identify user and manager
        String myId = DeviceIdManager.getDeviceId(requireContext());
        EntrantListManager manager = EntrantListManager.getInstance();

        Button btnAccept = view.findViewById(R.id.buttonAccept);
        Button btnDecline = view.findViewById(R.id.buttonDecline);

        // Accept Logic
        btnAccept.setOnClickListener(v -> {
            // We add this method to EntrantListManager to move user to an 'accepted' list
            manager.acceptInvitation(myId);
            Toast.makeText(getContext(), "Registration confirmed!", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack();
        });

        // Decline Logic
        btnDecline.setOnClickListener(v -> {
            // We add this method to move user to a 'declined' list (vacating the spot)
            manager.declineInvitation(myId);
            Toast.makeText(getContext(), "Invitation declined.", Toast.LENGTH_SHORT).show();
            NavHostFragment.findNavController(this).popBackStack();
        });
    }
}
