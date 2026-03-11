package com.example.eventlottery;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

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

public class GeolocationFragment extends Fragment implements OnMapReadyCallback {

    private double latitude;
    private double longitude;
    private String entrantName;
    private String locationAddress;

    public GeolocationFragment() {
        super(R.layout.geolocation);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Bundle args = getArguments();
        if (args != null) {
            latitude = args.getDouble("latitude", 0.0);
            longitude = args.getDouble("longitude", 0.0);
            entrantName = args.getString("entrant_name", "Entrant");
            locationAddress = args.getString("location_address", "No location available");
        }

        TextView textLocationOf = view.findViewById(R.id.textLocationOf);
        TextView textLocationAddress = view.findViewById(R.id.textLocationAddress);

        textLocationOf.setText("LOCATION OF: " + entrantName);
        textLocationAddress.setText(locationAddress);

        view.findViewById(R.id.buttonBackGeo).setOnClickListener(v ->
                NavHostFragment.findNavController(GeolocationFragment.this).navigateUp());

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        LatLng entrantLocation = new LatLng(latitude, longitude);
        googleMap.addMarker(new MarkerOptions()
                .position(entrantLocation)
                .title(entrantName));
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(entrantLocation, 15f));
    }
}