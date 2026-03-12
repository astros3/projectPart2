package com.example.eventlottery;

/**
 * Shows entrant location on a map (from waiting list or users). Used from Waiting List flow.
 * Reads deviceId/entrant data from Firestore to display marker and address.
 */
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

public class GeolocationFragment extends Fragment implements OnMapReadyCallback {

    private double latitude = 0.0;
    private double longitude = 0.0;
    private String entrantName = "Entrant";
    private String locationAddress = "No location available";
    private String deviceId;

    private TextView textLocationOf;
    private TextView textLocationAddress;
    private GoogleMap mMap;
    private FirebaseFirestore db;

    public GeolocationFragment() {
        super(R.layout.geolocation);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        textLocationOf = view.findViewById(R.id.textLocationOf);
        textLocationAddress = view.findViewById(R.id.textLocationAddress);

        Bundle args = getArguments();
        if (args != null) {
            deviceId = args.getString("deviceId");
        }

        textLocationOf.setText("LOCATION OF: Loading...");
        textLocationAddress.setText("Loading location...");

        view.findViewById(R.id.buttonBackGeo).setOnClickListener(v ->
                NavHostFragment.findNavController(GeolocationFragment.this).navigateUp());

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        loadEntrantLocation();
    }

    private void loadEntrantLocation() {
        if (deviceId == null || deviceId.isEmpty()) {
            Toast.makeText(getContext(), "Missing entrant device ID", Toast.LENGTH_SHORT).show();
            textLocationOf.setText("LOCATION OF: Unknown");
            textLocationAddress.setText("No location available");
            return;
        }

        db.collection("users")
                .document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(getContext(), "Entrant profile not found", Toast.LENGTH_SHORT).show();
                        textLocationOf.setText("LOCATION OF: " + deviceId);
                        textLocationAddress.setText("No location available");
                        return;
                    }

                    Entrant entrant = documentSnapshot.toObject(Entrant.class);

                    if (entrant != null) {
                        if (entrant.getFullName() != null && !entrant.getFullName().trim().isEmpty()) {
                            entrantName = entrant.getFullName();
                        } else {
                            entrantName = deviceId;
                        }

                        if (entrant.getLocationAddress() != null && !entrant.getLocationAddress().trim().isEmpty()) {
                            locationAddress = entrant.getLocationAddress();
                        }

                        if (entrant.getLatitude() != null) {
                            latitude = entrant.getLatitude();
                        }

                        if (entrant.getLongitude() != null) {
                            longitude = entrant.getLongitude();
                        }
                    } else {
                        entrantName = deviceId;
                    }

                    textLocationOf.setText("LOCATION OF: " + entrantName);
                    textLocationAddress.setText(locationAddress);
                    updateMap();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load entrant location", Toast.LENGTH_SHORT).show();
                    textLocationOf.setText("LOCATION OF: " + deviceId);
                    textLocationAddress.setText("No location available");
                });
    }

    private void updateMap() {
        if (mMap == null) {
            return;
        }

        if (latitude == 0.0 && longitude == 0.0) {
            return;
        }

        LatLng entrantLocation = new LatLng(latitude, longitude);
        mMap.clear();
        mMap.addMarker(new MarkerOptions()
                .position(entrantLocation)
                .title(entrantName));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(entrantLocation, 15f));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        updateMap();
    }
}
