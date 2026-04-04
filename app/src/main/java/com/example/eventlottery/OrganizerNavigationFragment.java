package com.example.eventlottery;

/**
 * Organizer event menu: Update Event, QR Code, View Geolocation, Lottery Draw, waiting/selected/final lists.
 * Uses EventEditActivity.getCurrentEventId() for the selected event.
 * For private events: shows "Invite Entrants" instead of "View QR Code" (US 02.01.02, 02.01.03).
 */
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.firestore.FirebaseFirestore;

public class OrganizerNavigationFragment extends Fragment {

    public OrganizerNavigationFragment() {
        super(R.layout.organizernavigationfragment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String deviceId = DeviceIdManager.getDeviceId(requireContext());
        // App-wide prefs: use application context so FragmentScenario / non-standard hosts still
        // see the same organizer current-event id as EventEditActivity.
        final Context appCtx = requireContext().getApplicationContext();

        view.findViewById(R.id.btn_back).setOnClickListener(v -> {
            androidx.navigation.NavController nav = NavHostFragment.findNavController(OrganizerNavigationFragment.this);
            nav.popBackStack(R.id.OrganizerDashboardFragment, false);
        });

        // Update Event Information (US 02.01.01, 02.01.04)
        view.findViewById(R.id.buttonUpdate).setOnClickListener(v -> {
            String eventId = EventEditActivity.getCurrentEventId(appCtx);
            startActivity(EventEditActivity.newIntent(requireContext(), eventId));
        });

        // View event comments and post new ones as organizer
        view.findViewById(R.id.buttonComments).setOnClickListener(v -> {
            String eventId = EventEditActivity.getCurrentEventId(appCtx);
            if (eventId == null || eventId.isEmpty()) {
                Toast.makeText(requireContext(), "No event selected", Toast.LENGTH_SHORT).show();
                return;
            }
            Intent intent = new Intent(requireContext(), EventDetailsActivity.class);
            intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, eventId);
            intent.putExtra(EventDetailsActivity.EXTRA_VIEW_AS_ENTRANT, false);
            startActivity(intent);
        });

        Button buttonManageCoOrganizers = view.findViewById(R.id.buttonManageCoOrganizers);
        Button buttonQR = view.findViewById(R.id.buttonQR);
        Button buttonInviteEntrants = view.findViewById(R.id.buttonInviteEntrants);

        // Manage Co-Organizers — only shown to the primary organizer
        buttonManageCoOrganizers.setOnClickListener(v -> {
            String eventId = EventEditActivity.getCurrentEventId(appCtx);
            if (eventId == null || eventId.isEmpty()) {
                Toast.makeText(requireContext(), "No event selected", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(CoOrganizerManagementActivity.newIntent(requireContext(), eventId));
        });

        // View QR Code (US 02.01.01) — hidden for private events
        buttonQR.setOnClickListener(v -> {
            String eventId = EventEditActivity.getCurrentEventId(appCtx);
            if (eventId == null || eventId.isEmpty()) {
                Toast.makeText(requireContext(), "Create an event first", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(QRCodeActivity.newIntent(requireContext(), eventId));
        });

        // Invite Entrants (US 02.01.03) — private events only
        buttonInviteEntrants.setOnClickListener(v -> {
            String eventId = EventEditActivity.getCurrentEventId(appCtx);
            if (eventId == null || eventId.isEmpty()) {
                Toast.makeText(requireContext(), "No event selected", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(OrganizerInviteEntrantActivity.newIntent(requireContext(), eventId));
        });

        // Load event to show/hide QR vs Invite button, and show Manage Co-Organizers for primary organizer only
        String eventId = EventEditActivity.getCurrentEventId(appCtx);
        if (eventId != null && !eventId.isEmpty()) {
            FirebaseFirestore.getInstance().collection("events").document(eventId).get()
                    .addOnSuccessListener(doc -> {
                        if (doc == null || !doc.exists()) return;
                        Event event = doc.toObject(Event.class);
                        if (event == null) return;
                        event.setEventId(doc.getId());
                        // Align with EventFirestoreParser: some POJO mappings miss boolean isPrivate.
                        Boolean priv = doc.getBoolean("isPrivate");
                        if (priv == null) {
                            priv = doc.getBoolean("private");
                        }
                        if (priv != null) {
                            event.setPrivate(priv);
                        }
                        if (event.isPrivate()) {
                            buttonQR.setVisibility(View.GONE);
                            buttonInviteEntrants.setVisibility(View.VISIBLE);
                        } else {
                            buttonQR.setVisibility(View.VISIBLE);
                            buttonInviteEntrants.setVisibility(View.GONE);
                        }
                        // Show Manage Co-Organizers only to the primary organizer
                        if (deviceId.equals(event.getOrganizerId())) {
                            buttonManageCoOrganizers.setVisibility(View.VISIBLE);
                        } else {
                            buttonManageCoOrganizers.setVisibility(View.GONE);
                        }
                    });
        }

        // View Geolocation — event venue from Update Event Information (Firestore)
        view.findViewById(R.id.buttonGeo).setOnClickListener(v -> {
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

        view.findViewById(R.id.buttonNotification).setOnClickListener(v -> {
            String notifEventId = EventEditActivity.getCurrentEventId(appCtx);
            if (notifEventId == null || notifEventId.isEmpty()) {
                Toast.makeText(requireContext(), "No event selected", Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(OrganizerNotificationLogActivity.newIntent(requireContext(), notifEventId));
        });
        view.findViewById(R.id.buttonCancelled).setOnClickListener(v ->
                NavHostFragment.findNavController(OrganizerNavigationFragment.this)
                        .navigate(R.id.OrganizerNavigationFragment_to_Cancelled_list)
        );
    }
}