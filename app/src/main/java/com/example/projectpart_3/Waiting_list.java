package com.example.projectpart_3;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;

public class Waiting_list extends Fragment {
    public Waiting_list(){
        super(R.layout.waiting_list);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textWaiting = view.findViewById(R.id.textWaitingList);

        ArrayList<String> waitingList = EntrantListManager.getInstance().getWaitingList();

        StringBuilder sb = new StringBuilder("Waiting List:\n\n");

        for (String entrant : waitingList){
            sb.append(entrant).append("\n");
        }

        textWaiting.setText(sb.toString());

        view.findViewById(R.id.buttonBack).setOnClickListener(v ->
                NavHostFragment.findNavController(Waiting_list.this)
                        .navigate(R.id.Waiting_list_to_OrganizerNavigationFragment));
}}
