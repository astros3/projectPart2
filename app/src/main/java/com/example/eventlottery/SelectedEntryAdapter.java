package com.example.eventlottery;

/**
 * List adapter for selected (SELECTED/ACCEPTED) WaitingListEntry in SelectedList fragment.
 * Supports delete callback per entry.
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

public class SelectedEntryAdapter extends ArrayAdapter<WaitingListEntry> {

    public interface OnDeleteClickListener {
        void onDeleteClick(WaitingListEntry entry);
    }

    private final FragmentActivity activity;
    private final ArrayList<WaitingListEntry> entries;
    private final OnDeleteClickListener listener;

    public SelectedEntryAdapter(FragmentActivity activity,
                                ArrayList<WaitingListEntry> entries,
                                OnDeleteClickListener listener) {
        super(activity, 0, entries);
        this.activity = activity;
        this.entries = entries;
        this.listener = listener;
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

        textEntrantName.setText(entry.getDeviceId());

        buttonDelete.setOnClickListener(v -> listener.onDeleteClick(entry));

        return view;
    }
}
