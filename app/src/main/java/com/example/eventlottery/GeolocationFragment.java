package com.example.eventlottery;

/**
 * Shows entrant location on a map. If only locationAddress is in DB, geocodes it for lat/lng.
 */
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
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

import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class GeolocationFragment extends Fragment implements OnMapReadyCallback {

    private static final String TAG = "ViewGeolocation";

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
        Log.d(TAG, "onViewCreated: args=" + (args != null) + ", deviceId=" + (deviceId != null ? "present(len=" + deviceId.length() + ")" : "null"));

        textLocationOf.setText("LOCATION OF: Loading...");
        textLocationAddress.setText("Loading location...");

        view.findViewById(R.id.buttonBackGeo).setOnClickListener(v ->
                NavHostFragment.findNavController(GeolocationFragment.this).navigateUp());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        Log.d(TAG, "mapFragment from getChildFragmentManager: " + (mapFragment != null));
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        loadEntrantLocation();
    }

    private void loadEntrantLocation() {
        if (deviceId == null || deviceId.isEmpty()) {
            Log.w(TAG, "loadEntrantLocation: deviceId null or empty, aborting");
            Toast.makeText(getContext(), "Entrant not specified", Toast.LENGTH_SHORT).show();
            textLocationOf.setText("LOCATION OF: Unknown");
            textLocationAddress.setText("No location available");
            return;
        }

        Log.d(TAG, "loadEntrantLocation: fetching users/" + deviceId);
        db.collection("users")
                .document(deviceId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Log.d(TAG, "loadEntrantLocation: Firestore success, exists=" + documentSnapshot.exists());
                    if (!documentSnapshot.exists()) {
                        Log.w(TAG, "loadEntrantLocation: document does not exist");
                        Toast.makeText(getContext(), "Entrant profile not found", Toast.LENGTH_SHORT).show();
                        textLocationOf.setText("LOCATION OF: Unknown Entrant");
                        textLocationAddress.setText("No location available");
                        return;
                    }

                    Entrant entrant = documentSnapshot.toObject(Entrant.class);
                    Log.d(TAG, "loadEntrantLocation: entrant=" + (entrant != null) + " name=" + (entrant != null ? entrant.getFullName() : "n/a") + " address=" + (entrant != null && entrant.getLocationAddress() != null ? "present" : "null") + " lat=" + (entrant != null && entrant.getLatitude() != null ? entrant.getLatitude() : "null") + " lng=" + (entrant != null && entrant.getLongitude() != null ? entrant.getLongitude() : "null"));

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
                        Log.d(TAG, "loadEntrantLocation: using DB coords, calling updateMap");
                        updateMap();
                    } else if (!"No location available".equals(locationAddress)) {
                        Log.d(TAG, "loadEntrantLocation: no coords, geocoding address: " + locationAddress);
                        geocodeAddressThenUpdateMap(locationAddress);
                    } else {
                        Log.d(TAG, "loadEntrantLocation: no coords and no address, calling updateMap (no-op)");
                        updateMap();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "loadEntrantLocation: Firestore failed", e);
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
            Log.d(TAG, "geocodeAddressThenUpdateMap: skip, context or address null/empty");
            updateMap();
            return;
        }
        Log.d(TAG, "geocodeAddressThenUpdateMap: starting for address=" + address);
        Executors.newSingleThreadExecutor().execute(() -> {
            try {
                Geocoder geocoder = new Geocoder(requireContext(), Locale.getDefault());
                List<Address> results = geocoder.getFromLocationName(address.trim(), 1);
                Log.d(TAG, "geocodeAddressThenUpdateMap: Geocoder returned " + (results != null ? results.size() : 0) + " result(s)");
                if (results != null && !results.isEmpty()) {
                    Address first = results.get(0);
                    if (first.hasLatitude() && first.hasLongitude()) {
                        double lat = first.getLatitude();
                        double lng = first.getLongitude();
                        Log.d(TAG, "geocodeAddressThenUpdateMap: got lat=" + lat + " lng=" + lng);
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                latitude = lat;
                                longitude = lng;
                                updateMap();
                            });
                            return;
                        }
                    } else {
                        Log.d(TAG, "geocodeAddressThenUpdateMap: first result has no lat/lng");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "geocodeAddressThenUpdateMap: exception", e);
            }
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::updateMap);
            }
        });
    }

    private void updateMap() {
        if (mMap == null) {
            Log.d(TAG, "updateMap: mMap is null, skipping");
            return;
        }
        if (latitude == 0.0 && longitude == 0.0) {
            Log.d(TAG, "updateMap: lat/lng both 0, skipping marker");
            return;
        }
        Log.d(TAG, "updateMap: setting marker at " + latitude + "," + longitude);
        LatLng entrantLocation = new LatLng(latitude, longitude);
        mMap.clear();
        mMap.addMarker(new MarkerOptions()
                .position(entrantLocation)
                .title(entrantName));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(entrantLocation, 15f));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        Log.d(TAG, "onMapReady: map ready");
        mMap = googleMap;
        updateMap();
    }
}
