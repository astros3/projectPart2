package com.example.eventlottery;

/**
 * List adapter for selected (SELECTED/ACCEPTED) WaitingListEntry in SelectedList fragment.
 * Shows entrant display name (from users collection); never exposes device ID. Supports delete callback per entry.
 */
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class SelectedEntryAdapter extends ArrayAdapter<WaitingListEntry> {

    public interface OnDeleteClickListener {
        void onDeleteClick(WaitingListEntry entry);
    }

    private final FragmentActivity activity;
    private final ArrayList<WaitingListEntry> entries;
    private final OnDeleteClickListener listener;
    private Map<String, String> deviceIdToName = new HashMap<>();

    public SelectedEntryAdapter(FragmentActivity activity,
                                ArrayList<WaitingListEntry> entries,
                                OnDeleteClickListener listener) {
        super(activity, 0, entries);
        this.activity = activity;
        this.entries = entries;
        this.listener = listener;
    }

    /** Sets display names (deviceId -> name). Call after loading from users collection. */
    public void setDeviceIdToName(@NonNull Map<String, String> deviceIdToName) {
        this.deviceIdToName = deviceIdToName != null ? deviceIdToName : new HashMap<>();
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View view = convertView;

        if (view == null) {
            view = LayoutInflater.from(activity).inflate(R.layout.item_selected_entry, parent, false);
        }

        WaitingListEntry entry = entries.get(position);

        TextView textEntrantName = view.findViewById(R.id.textEntrantName);
        ImageView buttonDelete = view.findViewById(R.id.buttonDelete);

        String deviceId = entry.getDeviceId();
        String displayName = deviceIdToName != null && deviceIdToName.containsKey(deviceId)
                ? deviceIdToName.get(deviceId)
                : null;
        textEntrantName.setText(displayName != null && !displayName.isEmpty() ? displayName : "Unknown Entrant");

        buttonDelete.setOnClickListener(v -> listener.onDeleteClick(entry));

        return view;
    }
}
