package com.example.eventlottery;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.maps.android.clustering.ClusterManager;

/**
 * Shows events with coordinates using the same {@link EventFilterCriteria} as the home list
 * (registration-open filter when enabled; distance needs location — toast if unavailable).
 * Markers cluster when zoomed out.
 */
public class EntrantMapActivity extends AppCompatActivity implements OnMapReadyCallback {

    public static final String EXTRA_FILTER = "entrant_map_filter";

    private static final int REQ_LOC = 7101;

    private GoogleMap map;
    private ClusterManager<EventMapClusterItem> clusterManager;
    private FusedLocationProviderClient fusedClient;
    private EventFilterCriteria filter;
    private Double userLat;
    private Double userLng;
    private boolean userLocationKnown;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_map);
        filter = readFilterFromIntent();
        fusedClient = LocationServices.getFusedLocationProviderClient(this);

        Toolbar toolbar = findViewById(R.id.toolbar_map);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private EventFilterCriteria readFilterFromIntent() {
        Intent i = getIntent();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            EventFilterCriteria c = i.getSerializableExtra(EXTRA_FILTER, EventFilterCriteria.class);
            return c != null ? c : EventFilterCriteria.empty();
        }
        Object o = i.getSerializableExtra(EXTRA_FILTER);
        return o instanceof EventFilterCriteria ? (EventFilterCriteria) o : EventFilterCriteria.empty();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        clusterManager = new ClusterManager<>(this, googleMap);
        googleMap.setOnCameraIdleListener(clusterManager);
        googleMap.setOnMarkerClickListener(clusterManager);
        clusterManager.setOnClusterItemClickListener(item -> {
            startActivity(new Intent(this, EventDetailsActivity.class)
                    .putExtra(EventDetailsActivity.EXTRA_EVENT_ID, item.getEventId()));
            return true;
        });
        clusterManager.setOnClusterClickListener(cluster -> {
            float z = googleMap.getCameraPosition().zoom + 1f;
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(cluster.getPosition(), z));
            return true;
        });

        tryFetchLocationThenLoadMarkers();
    }

    private void tryFetchLocationThenLoadMarkers() {
        boolean fine = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        boolean coarse = ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
        if (!fine && !coarse) {
            userLocationKnown = false;
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, REQ_LOC);
            maybeToastMissingLocationForDistance();
            loadMarkersFromFirestore();
            return;
        }
        try {
            map.setMyLocationEnabled(true);
        } catch (SecurityException ignored) { }

        fusedClient.getLastLocation().addOnCompleteListener(task -> {
            Location loc = task.isSuccessful() ? task.getResult() : null;
            if (loc != null) {
                userLat = loc.getLatitude();
                userLng = loc.getLongitude();
                userLocationKnown = true;
                loadMarkersFromFirestore();
                return;
            }
            fusedClient.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null)
                    .addOnCompleteListener(t2 -> {
                        Location cur = t2.isSuccessful() ? t2.getResult() : null;
                        if (cur != null) {
                            userLat = cur.getLatitude();
                            userLng = cur.getLongitude();
                            userLocationKnown = true;
                        } else {
                            userLat = null;
                            userLng = null;
                            userLocationKnown = false;
                            maybeToastMissingLocationForDistance();
                        }
                        loadMarkersFromFirestore();
                    });
        });
    }

    private void maybeToastMissingLocationForDistance() {
        if (filter.getMaxDistanceKm() != null && filter.getMaxDistanceKm() > 0
                && !userLocationKnown) {
            Toast.makeText(this, R.string.distance_filter_needs_location, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQ_LOC || map == null || clusterManager == null) return;
        boolean anyLoc = false;
        for (int r : grantResults) {
            if (r == PackageManager.PERMISSION_GRANTED) {
                anyLoc = true;
                break;
            }
        }
        if (anyLoc) {
            tryFetchLocationThenLoadMarkers();
        }
    }

    private void loadMarkersFromFirestore() {
        if (map == null || clusterManager == null) return;

        boolean hasFix = userLocationKnown && userLat != null && userLng != null;
        FirebaseFirestore.getInstance().collection("events")
                .get()
                .addOnSuccessListener(snap -> {
                    clusterManager.clearItems();
                    for (QueryDocumentSnapshot doc : snap) {
                        Event e = EventFirestoreParser.fromSnapshot(doc);
                        // Private events are not publicly discoverable (US 02.01.02)
                        if (e.isPrivate()) continue;
                        if (!EventFilterUtils.matchesForMap(e, filter, userLat, userLng, hasFix)) {
                            continue;
                        }
                        String sub = e.getEventType();
                        if (sub == null || sub.isEmpty()) {
                            sub = e.getLocation();
                        }
                        clusterManager.addItem(new EventMapClusterItem(
                                e.getLatitude(), e.getLongitude(),
                                e.getTitle(), sub, e.getEventId()));
                    }
                    clusterManager.cluster();
                    if (hasFix) {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(userLat, userLng), 11f));
                    } else if (!snap.isEmpty()) {
                        map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                new LatLng(53.5, -113.5), 5f));
                    }
                });
    }
}
