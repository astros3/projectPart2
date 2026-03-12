package com.example.eventlottery;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.eventlottery.DeviceIdManager;
import com.example.eventlottery.R;

public class InvitationResponseFragment extends Fragment {

    public InvitationResponseFragment() {
        super(R.layout.invitation_response);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // 1. Identify the entrant using your DeviceIdManager
        String myId = DeviceIdManager.getDeviceId(getContext());

        Button btnJoinAgain = view.findViewById(R.id.buttonRequestJoinAgain);
        TextView statusHeader = view.findViewById(R.id.textStatusHeader);
        TextView statusDetail = view.findViewById(R.id.textStatusDetail);

        // 2. Determine UI state based on EntrantListManager lists
        EntrantListManager manager = EntrantListManager.getInstance();

        if (manager.getRejectedList().contains(myId)) {
            // User was not picked (Rejected State)
            statusHeader.setText("STATUS: NOT SELECTED");
            statusDetail.setText("You were not chosen for this event. Would you like to try again?");
            btnJoinAgain.setVisibility(View.VISIBLE);
        } else if (manager.getSelectedList().contains(myId)) {
            // User was picked (Selected State)
            statusHeader.setText("STATUS: SELECTED!");
            statusDetail.setText("Congratulations! You have been invited.");
            btnJoinAgain.setVisibility(View.GONE);
            // You could add a 'Decline' button here later!
        } else {
            // User is either already in waiting or hasn't signed up
            statusHeader.setText("STATUS: WAITING");
            btnJoinAgain.setVisibility(View.GONE);
        }

        // 3. Handle the "Second Chance" button click
        btnJoinAgain.setOnClickListener(v -> {
            manager.requestToJoinAgain(myId);
            Toast.makeText(getContext(), "You are back in the lottery!", Toast.LENGTH_SHORT).show();

            // Refresh the UI to hide the button
            btnJoinAgain.setVisibility(View.GONE);
            statusHeader.setText("STATUS: WAITING");
        });
    }
}