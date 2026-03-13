package com.example.eventlottery;

/**
 * Shows entrant location on a map (from waiting list or users). Used from Waiting List flow.
 * Reads entrant data from Firestore; displays name only, never device ID.
 * If only locationAddress is in DB, geocodes it to get latitude/longitude for the map.
 */
import android.location.Geocoder;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

import android.location.Address;

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
        if (deviceId == null) {
            deviceId = "";
        }

        textLocationOf.setText("LOCATION OF: Loading...");
        textLocationAddress.setText("Loading location...");

        view.findViewById(R.id.buttonBackGeo).setOnClickListener(v ->
                NavHostFragment.findNavController(GeolocationFragment.this).navigateUp());

        // Map fragment may not be attached yet; look it up after layout pass
        view.post(() -> {
            getChildFragmentManager().executePendingTransactions();
            SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
            if (mapFragment == null) {
                mapFragment = (SupportMapFragment) getParentFragmentManager().findFragmentById(R.id.map);
            }
            if (mapFragment == null) {
                View mapContainer = view.findViewById(R.id.map);
                if (mapContainer instanceof FragmentContainerView) {
                    Fragment child = ((FragmentContainerView) mapContainer).getFragment();
                    if (child instanceof SupportMapFragment) {
                        mapFragment = (SupportMapFragment) child;
                    }
                }
            }
            if (mapFragment != null) {
                mapFragment.getMapAsync(this);
            }
        });

        loadEntrantLocation();
    }

    private void loadEntrantLocation() {
        if (deviceId == null || deviceId.isEmpty()) {
            Toast.makeText(getContext(), "Entrant not specified", Toast.LENGTH_SHORT).show();
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
                        textLocationOf.setText("LOCATION OF: Unknown Entrant");
                        textLocationAddress.setText("No location available");
                        return;
                    }

                    Entrant entrant = documentSnapshot.toObject(Entrant.class);

                    if (entrant != null) {
                        if (entrant.getFullName() != null && !entrant.getFullName().trim().isEmpty()) {
                            entrantName = entrant.getFullName();
                        } else {
                            entrantName = "Unknown Entrant";
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
                        entrantName = "Unknown Entrant";
                    }

                    textLocationOf.setText("LOCATION OF: " + entrantName);
                    boolean hasCoordinates = (latitude != 0.0 || longitude != 0.0);
                    if (locationAddress == null || locationAddress.isEmpty()) {
                        locationAddress = "No location available";
                    }
                    textLocationAddress.setText(locationAddress);
                    if (hasCoordinates) {
                        updateMap();
                    } else if (!"No location available".equals(locationAddress)) {
                        // Address in DB but no lat/lng: geocode to get coordinates for the map
                        geocodeAddressThenUpdateMap(locationAddress);
                    } else {
                        updateMap();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Failed to load entrant location", Toast.LENGTH_SHORT).show();
                    textLocationOf.setText("LOCATION OF: Unknown Entrant");
                    textLocationAddress.setText("No location available");
                });
    }

    /**
     * Geocode the address string to get latitude/longitude, then update the map on the main thread.
     * Assumes address is in DB but lat/lng are missing.
     */
    private void geocodeAddressThenUpdateMap(String address) {
        if (getContext() == null || address == null || address.trim().isEmpty()) {
            updateMap();
            return;
        }
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                List<Address> results = geocoder.getFromLocationName(address.trim(), 1);
                if (results != null && !results.isEmpty()) {
                    Address first = results.get(0);
                    if (first.hasLatitude() && first.hasLongitude()) {
                        double lat = first.getLatitude();
                        double lng = first.getLongitude();
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                latitude = lat;
                                longitude = lng;
                                updateMap();
                            });
                            return;
                        }
                    }
                }
            } catch (Exception e) {
                // Geocoder failed (no network, not present, etc.)
            }
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::updateMap);
            }
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
