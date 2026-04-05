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

    /**
     * Creates a new map cluster item for the given event coordinates.
     *
     * @param lat     latitude of the event location in decimal degrees
     * @param lng     longitude of the event location in decimal degrees
     * @param title   marker title (event name); null becomes empty string
     * @param snippet marker snippet text; null becomes empty string
     * @param eventId non-null Firestore ID of the event this item represents
     */
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

    /**
     * Returns the Firestore document ID of the event this cluster item represents.
     *
     * @return non-null event ID string
     */
    @NonNull
    public String getEventId() {
        return eventId;
    }
}
