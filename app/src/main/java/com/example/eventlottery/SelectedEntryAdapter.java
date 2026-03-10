package com.example.eventlottery;

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
                                ArrayList<Waiting_list> entries,
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

        Waiting_list entry = entries.get(position);
        Entrant entrant = entry.getEntrant();

        TextView textEntrantName = view.findViewById(R.id.textEntrantName);
        ImageView buttonDelete = view.findViewById(R.id.buttonDelete);

        textEntrantName.setText(entrant.getFullName());

        buttonDelete.setOnClickListener(v -> listener.onDeleteClick(entry));

        return view;
    }
}