package com.example.projectpart_3;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import java.util.ArrayList;

public class LotteryDraw extends Fragment {

    public LotteryDraw() {
        super(R.layout.lotterydraw);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.buttonBack).setOnClickListener(v ->
                NavHostFragment.findNavController(LotteryDraw.this)
                        .navigate(R.id.LotteryDraw_to_OrganizerNavigationFragment)
        );

        EditText editTextNumber = view.findViewById(R.id.editTextNumber);
        View buttonDraw = view.findViewById(R.id.buttonDraw);
        TextView textViewResult = view.findViewById(R.id.textViewResult);

        buttonDraw.setOnClickListener(v -> {
            String input = editTextNumber.getText().toString().trim();

            if (TextUtils.isEmpty(input)) {
                Toast.makeText(getContext(), "Enter number of entrants", Toast.LENGTH_SHORT).show();
                return;
            }

            int count = Integer.parseInt(input);

            if (count <= 0) {
                Toast.makeText(getContext(), "Number must be greater than 0", Toast.LENGTH_SHORT).show();
                return;
            }

            if (count > EntrantListManager.getInstance().getWaitingList().size()) {
                Toast.makeText(getContext(),
                        "Not enough entrants in waiting list",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            ArrayList<String> selected =
                    EntrantListManager.getInstance().drawEntrants(count);

            StringBuilder result = new StringBuilder("Selected Entrants:\n\n");
            for (String entrant : selected) {
                result.append(entrant).append("\n");
            }

            textViewResult.setText(result.toString());
        });
    }
}