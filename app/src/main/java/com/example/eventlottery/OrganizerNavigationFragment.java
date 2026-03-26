package com.example.eventlottery;

/**
 * Organizer event menu: Update Event, QR Code, View Geolocation, Lottery Draw, waiting/selected/final lists.
 * Uses EventEditActivity.getCurrentEventId() for the selected event.
 */
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class OrganizerNavigationFragment extends Fragment {

    public OrganizerNavigationFragment() {
        super(R.layout.organizernavigationfragment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.btn_back).setOnClickListener(v -> {
            androidx.navigation.NavController nav = NavHostFragment.findNavController(OrganizerNavigationFragment.this);
            nav.popBackStack(R.id.OrganizerDashboardFragment, false);
        });

        // Update Event Information (US 02.01.01, 02.01.04)
        view.findViewById(R.id.buttonUpdate).setOnClickListener(v -> {
            String eventId = EventEditActivity.getCurrentEventId(requireContext());
            startActivity(EventEditActivity.newIntent(requireContext(), eventId));
        });

        // View QR Code (US 02.01.01)
        view.findViewById(R.id.buttonQR).setOnClickListener(v -> {
            String eventId = EventEditActivity.getCurrentEventId(requireContext());
            if (eventId == null || eventId.isEmpty()) {
                Toast.makeText(requireContext(), "Create an event first", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(QRCodeActivity.newIntent(requireContext(), eventId));
        });

        // View Geolocation — event venue from Update Event Information (Firestore)
        view.findViewById(R.id.buttonGeo).setOnClickListener(v -> {
            String eventId = EventEditActivity.getCurrentEventId(requireContext());
            if (eventId == null || eventId.isEmpty()) {
                Toast.makeText(requireContext(), "Create an event first", Toast.LENGTH_SHORT).show();
                return;
            }
            Bundle args = new Bundle();
            args.putString("eventId", eventId);
            NavHostFragment.findNavController(OrganizerNavigationFragment.this)
                    .navigate(R.id.OrganizerNavigationFragment_to_EventLocationFragment, args);
        });

        view.findViewById(R.id.buttonLottery).setOnClickListener(v ->
                NavHostFragment.findNavController(OrganizerNavigationFragment.this)
                        .navigate(R.id.OrganizerNavigationFragment_to_LotteryDraw)
        );
        view.findViewById(R.id.buttonWaiting).setOnClickListener(v ->
                NavHostFragment.findNavController(OrganizerNavigationFragment.this)
                        .navigate(R.id.OrganizerNavigationFragment_to_Waiting_list)
        );
        view.findViewById(R.id.buttonSelected).setOnClickListener(v ->
                NavHostFragment.findNavController(OrganizerNavigationFragment.this)
                        .navigate(R.id.OrganizerNavigationFragment_to_Selected_list)
        );
        view.findViewById(R.id.buttonFinal).setOnClickListener(v ->
                NavHostFragment.findNavController(OrganizerNavigationFragment.this)
                        .navigate(R.id.OrganizerNavigationFragment_to_Final_list)
        );

        view.findViewById(R.id.buttonComments).setOnClickListener(v -> {
            String eventId = EventEditActivity.getCurrentEventId(requireContext());
            if (eventId == null || eventId.isEmpty()) {
                Toast.makeText(requireContext(), "Create an event first", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(requireContext(), EventDetailsActivity.class);
            intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, eventId);
            intent.putExtra(EventDetailsActivity.EXTRA_VIEW_AS_ENTRANT, false);
            startActivity(intent);
        });
    }
}