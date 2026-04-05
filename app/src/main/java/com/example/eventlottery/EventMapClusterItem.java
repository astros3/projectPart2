package com.example.eventlottery;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * Map marker / cluster leaf for an Event with coordinates.
 */
public class EventMapClusterItem implements ClusterItem {
    private final LatLng position;
    private final String title;
    private final String snippet;
    private final String eventId;

    public EventMapClusterItem(double lat, double lng, @Nullable String title,
                               @Nullable String snippet, @NonNull String eventId) {
        this.position = new LatLng(lat, lng);
        this.title = title != null ? title : "";
        this.snippet = snippet != null ? snippet : "";
        this.eventId = eventId;
    }

    @NonNull
    @Override
    public LatLng getPosition() {
        return position;
    }

    @Nullable
    @Override
    public String getTitle() {
        return title;
    }

    @Nullable
    @Override
    public String getSnippet() {
        return snippet;
    }

    @Nullable
    @Override
    public Float getZIndex() {
        return 0f;
    }

    @NonNull
    public String getEventId() {
        return eventId;
    }
}
