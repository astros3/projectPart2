package com.example.eventlottery;

/**
 * List adapter for WaitingListEntry in WaitingListFragment. Shows entrant display name (from users collection);
 * never exposes device ID. Can navigate to GeolocationFragment for an entrant.
 */
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WaitingEntryAdapter extends ArrayAdapter<WaitingListEntry> {

    private final FragmentActivity activity;
    private final ArrayList<WaitingListEntry> entries;
    private final NavController navController;
    /** Map from entrant deviceId to display name (from users collection). */
    private Map<String, String> deviceIdToName = new HashMap<>();

    public WaitingEntryAdapter(@NonNull FragmentActivity activity,
                               @NonNull ArrayList<WaitingListEntry> entries,
                               @NonNull NavController navController) {
        super(activity, 0, entries);
        this.activity = activity;
        this.entries = entries;
        this.navController = navController;
    }

    /** Sets the display names for entrants (deviceId -> name). Call after loading from users collection. */
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
            if (deviceId == null || deviceId.isEmpty()) return;
            Bundle bundle = new Bundle();
            bundle.putString("deviceId", deviceId);
            navController.navigate(R.id.Waiting_list_to_GeolocationFragment, bundle);
        });

        return view;
    }
}
