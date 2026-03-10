package com.example.projectpart_3;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;

public class Selected_list extends Fragment {
    public Selected_list(){
        super(R.layout.selected_list);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView textSelected = view.findViewById(R.id.textSelectedList);

        ArrayList<String> selectedList =
                EntrantListManager.getInstance().getSelectedList();

        StringBuilder sb = new StringBuilder("Selected Entrants:\n\n");

        for (String entrant : selectedList) {
            sb.append(entrant).append("\n");
        }

        textSelected.setText(sb.toString());

        view.findViewById(R.id.buttonBack).setOnClickListener(v ->
                NavHostFragment.findNavController(Selected_list.this)
                        .navigate(R.id.Selected_list_to_OrganizerNavigationFragment));
    }}