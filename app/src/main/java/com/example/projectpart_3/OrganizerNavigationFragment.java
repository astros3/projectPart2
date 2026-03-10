package com.example.projectpart_3;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

public class OrganizerNavigationFragment extends Fragment {

    public OrganizerNavigationFragment() {
        super(R.layout.organizernavigationfragment);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.buttonLottery).setOnClickListener(v ->
                NavHostFragment.findNavController(OrganizerNavigationFragment.this)
                        .navigate(R.id.OrganizerNavigationFragment_to_LotteryDraw)
        );
        view.findViewById(R.id.buttonWaiting).setOnClickListener(v ->
                NavHostFragment.findNavController(OrganizerNavigationFragment.this)
                        .navigate(R.id.OrganizerNavigationFragment_to_Waiting_list)
        );
        view.findViewById(R.id.buttonSelected).setOnClickListener(v ->
                NavHostFragment.findNavController(OrganizerNavigationFragment.this)
                        .navigate(R.id.OrganizerNavigationFragment_to_Selected_list)
        );

    }
}