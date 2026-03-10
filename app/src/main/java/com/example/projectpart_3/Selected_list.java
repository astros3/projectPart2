package com.example.projectpart_3;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;

public class Selected_list extends Fragment {

    private ArrayList<WaitingListEntry> selectedEntries;
    private SelectedEntryAdapter adapter;
    private Event event;

    public Selected_list() {
        super(R.layout.selected_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        event = EventRepository.getInstance().getCurrentEvent();

        ListView listSelectedEntrants = view.findViewById(R.id.listSelectedEntrants);

        selectedEntries = new ArrayList<>(event.getSelectedEntrants());

        adapter = new SelectedEntryAdapter(requireActivity(), selectedEntries, entry -> {
            event.cancelEntrant(entry.getEntrant());

            selectedEntries.clear();
            selectedEntries.addAll(event.getSelectedEntrants());
            adapter.notifyDataSetChanged();
        });

        listSelectedEntrants.setAdapter(adapter);

        view.findViewById(R.id.buttonBack).setOnClickListener(v ->
                NavHostFragment.findNavController(Selected_list.this)
                        .navigate(R.id.Selected_list_to_OrganizerNavigationFragment));
    }

    @Override
    public void onResume() {
        super.onResume();

        if (event != null && selectedEntries != null && adapter != null) {
            selectedEntries.clear();
            selectedEntries.addAll(event.getSelectedEntrants());
            adapter.notifyDataSetChanged();
        }
    }
}