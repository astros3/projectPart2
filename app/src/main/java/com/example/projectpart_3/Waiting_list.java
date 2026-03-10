package com.example.projectpart_3;

import android.os.Bundle;
import android.view.View;
import android.widget.ListView;

import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;



import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;
import java.util.List;

public class Waiting_list extends Fragment {

    public Waiting_list() {
        super(R.layout.waiting_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        Event event = EventRepository.getInstance().getCurrentEvent();

        ListView listView = view.findViewById(R.id.listWaitingEntrants);

        List<WaitingListEntry> entries = event.getWaitingList();
        ArrayList<WaitingListEntry> waitingEntries = new ArrayList<>(entries);

        WaitingEntryAdapter adapter = new WaitingEntryAdapter(requireActivity(), waitingEntries);
        listView.setAdapter(adapter);

        view.findViewById(R.id.buttonBack).setOnClickListener(v ->
                NavHostFragment.findNavController(Waiting_list.this)
                        .navigate(R.id.Waiting_list_to_OrganizerNavigationFragment));
    }
}