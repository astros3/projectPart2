package com.example.eventlottery;

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

/**
 * Shows the event's location on a map for the organizer.
 * Loads the event from Firestore and geocodes the address to display a marker on the map.
 */
public class EventLocationFragment extends Fragment implements OnMapReadyCallback {

    /**
     * Creates a new EventLocationFragment and binds it to the geolocation layout.
     */
    public EventLocationFragment() {
        super(R.layout.geolocation);
    }

    private static final String TAG = "EventLocation";

    private double latitude = 0.0;
    private double longitude = 0.0;
    private String eventTitle = "Event";
    private String locationAddress = "";
    private String eventId;

    private TextView textLocationOf;
    private TextView textLocationAddress;
    private GoogleMap mMap;
    private FirebaseFirestore db;

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();

        textLocationOf = view.findViewById(R.id.textLocationOf);
        textLocationAddress = view.findViewById(R.id.textLocationAddress);
        TextView toolbarTitle = view.findViewById(R.id.toolbarTitleGeo);
        if (toolbarTitle != null) {
            toolbarTitle.setText(R.string.event_location);
        }

        Bundle args = getArguments();
        if (args != null) {
            eventId = args.getString("eventId");
        }

        textLocationOf.setText(getString(R.string.event_location_label) + " Loading...");
        textLocationAddress.setText("");

        view.findViewById(R.id.buttonBackGeo).setOnClickListener(v ->
                NavHostFragment.findNavController(EventLocationFragment.this).navigateUp());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        loadEventLocation();
    }

    private void loadEventLocation() {
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(getContext(), "Event not specified", Toast.LENGTH_SHORT).show();
            textLocationOf.setText(getString(R.string.event_location_label) + " —");
            textLocationAddress.setText(getString(R.string.event_location_not_set));
            return;
        }

        db.collection("events").document(eventId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(getContext(), "Event not found", Toast.LENGTH_SHORT).show();
                        textLocationOf.setText(getString(R.string.event_location_label) + " —");
                        textLocationAddress.setText(getString(R.string.event_location_not_set));
                        return;
                    }

                    Event event = documentSnapshot.toObject(Event.class);
                    if (event != null) {
                        eventTitle = event.getTitle() != null && !event.getTitle().trim().isEmpty()
                                ? event.getTitle() : "Event";
                        locationAddress = event.getLocation() != null ? event.getLocation().trim() : "";
                    }

                    textLocationOf.setText(getString(R.string.event_location_label) + " " + eventTitle);
                    if (locationAddress.isEmpty()) {
                        textLocationAddress.setText(getString(R.string.event_location_not_set));
                        updateMap();
                        return;
                    }
                    textLocationAddress.setText(locationAddress);
                    geocodeAddressThenUpdateMap(locationAddress);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "loadEventLocation failed", e);
                    Toast.makeText(getContext(), "Failed to load event location", Toast.LENGTH_SHORT).show();
                    textLocationOf.setText(getString(R.string.event_location_label) + " —");
                    textLocationAddress.setText(getString(R.string.event_location_not_set));
                });
    }

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
                Log.e(TAG, "geocodeAddressThenUpdateMap", e);
            }
            if (getActivity() != null) {
                getActivity().runOnUiThread(this::updateMap);
            }
        });
    }

    private void updateMap() {
        if (mMap == null) return;
        if (latitude == 0.0 && longitude == 0.0) return;
        LatLng location = new LatLng(latitude, longitude);
        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(location).title(eventTitle));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, 15f));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        updateMap();
    }
}
