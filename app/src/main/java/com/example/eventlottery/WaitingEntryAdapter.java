package com.example.eventlottery;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.Navigation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * List adapter for WAITING/PENDING WaitingListEntry items in WaitingListFragment.
 * Shows entrant display names resolved from the users collection; never exposes device IDs.
 * Tapping an entry can navigate to GeolocationFragment to view the entrant's location.
 */
public class WaitingEntryAdapter extends ArrayAdapter<WaitingListEntry> {

    private static final String TAG = "ViewGeolocation";

    private final FragmentActivity activity;
    private final ArrayList<WaitingListEntry> entries;
    private Map<String, String> deviceIdToName = new HashMap<>();

    /**
     * Creates a new WaitingEntryAdapter.
     *
     * @param activity the hosting FragmentActivity
     * @param entries  list of waiting/pending waiting-list entries to display
     */
    public WaitingEntryAdapter(@NonNull FragmentActivity activity,
                               @NonNull ArrayList<WaitingListEntry> entries) {
        super(activity, 0, entries);
        this.activity = activity;
        this.entries = entries;
    }

    /**
     * Sets display names resolved from the users collection.
     *
     * @param deviceIdToName map of device ID to entrant display name
     */
    public void setDeviceIdToName(@NonNull Map<String, String> deviceIdToName) {
        this.deviceIdToName = deviceIdToName;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(activity).inflate(R.layout.item_waiting_entry, parent, false);
        }

        WaitingListEntry entry = entries.get(position);

        TextView textEntrantName = view.findViewById(R.id.textEntrantName);
        ImageView buttonLocation = view.findViewById(R.id.buttonLocation);

        String deviceId = entry.getDeviceId();
        String displayName = deviceIdToName != null && deviceIdToName.containsKey(deviceId)
                ? deviceIdToName.get(deviceId)
                : null;
        textEntrantName.setText(displayName != null && !displayName.isEmpty() ? displayName : "Unknown Entrant");

        buttonLocation.setOnClickListener(v -> {
            Log.d(TAG, "location click: deviceId=" + (deviceId != null ? "present(len=" + deviceId.length() + ")" : "null"));
            Bundle bundle = new Bundle();
            bundle.putString("deviceId", deviceId);
            try {
                Navigation.findNavController(activity, R.id.nav_host_fragment)
                        .navigate(R.id.Waiting_list_to_GeolocationFragment, bundle);
                Log.d(TAG, "location click: navigate() called");
            } catch (Exception e) {
                Log.e(TAG, "location click: navigate failed", e);
            }
        });

        return view;
    }
}
