package com.example.eventlottery;

import android.app.Dialog;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;

/**
 * Dialog to edit {@link EventFilterCriteria}; posts result on Apply/Clear.
 */
public class EventFilterDialogFragment extends DialogFragment {

    public static final String REQUEST_KEY = "event_filter_request";
    public static final String BUNDLE_CRITERIA = "criteria";
    private static final String ARG_CRITERIA = "arg_criteria";

    public static EventFilterDialogFragment newInstance(EventFilterCriteria current) {
        EventFilterDialogFragment f = new EventFilterDialogFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRITERIA, current != null ? current : EventFilterCriteria.empty());
        f.setArguments(args);
        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        View root = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_event_filter, null);
        TextInputEditText inputKeyword = root.findViewById(R.id.input_filter_keyword);
        TextInputEditText inputDistance = root.findViewById(R.id.input_filter_distance_km);
        Spinner spinnerType = root.findViewById(R.id.spinner_filter_event_type);
        MaterialSwitch switchReg = root.findViewById(R.id.switch_registration_open);

        List<String> typeChoices = new ArrayList<>();
        typeChoices.add(getString(R.string.filter_any_type));
        for (CharSequence s : getResources().getTextArray(R.array.event_types)) {
            typeChoices.add(s.toString());
        }
        spinnerType.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, typeChoices));
        ((ArrayAdapter<?>) spinnerType.getAdapter())
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        EventFilterCriteria initial = readArgCriteria();
        if (initial != null) {
            inputKeyword.setText(initial.getKeyword());
            if (initial.getMaxDistanceKm() != null && initial.getMaxDistanceKm() > 0) {
                inputDistance.setText(String.valueOf(initial.getMaxDistanceKm()));
            }
            switchReg.setChecked(initial.isRegistrationOpenOnly());
            String et = initial.getEventType();
            if (!et.isEmpty()) {
                for (int i = 0; i < typeChoices.size(); i++) {
                    if (et.equalsIgnoreCase(typeChoices.get(i))) {
                        spinnerType.setSelection(i);
                        break;
                    }
                }
            }
        }

        root.findViewById(R.id.btn_filter_clear).setOnClickListener(v -> {
            Bundle b = new Bundle();
            EventFilterCriteria cleared = EventFilterCriteria.empty();
            putCriteria(b, cleared);
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, b);
            dismiss();
        });

        root.findViewById(R.id.btn_filter_apply).setOnClickListener(v -> {
            EventFilterCriteria c = new EventFilterCriteria();
            String kw = inputKeyword.getText() != null ? inputKeyword.getText().toString().trim() : "";
            c.setKeyword(kw);
            c.setRegistrationOpenOnly(switchReg.isChecked());
            String distStr = inputDistance.getText() != null ? inputDistance.getText().toString().trim() : "";
            if (!distStr.isEmpty()) {
                try {
                    double d = Double.parseDouble(distStr);
                    if (d > 0) c.setMaxDistanceKm(d);
                } catch (NumberFormatException ignored) { }
            }
            int pos = spinnerType.getSelectedItemPosition();
            if (pos > 0) {
                c.setEventType(typeChoices.get(pos));
            } else {
                c.setEventType("");
            }
            Bundle b = new Bundle();
            putCriteria(b, c);
            getParentFragmentManager().setFragmentResult(REQUEST_KEY, b);
            dismiss();
        });

        return new AlertDialog.Builder(requireContext())
                .setTitle(R.string.filter_events_title)
                .setView(root)
                .create();
    }

    @Nullable
    private EventFilterCriteria readArgCriteria() {
        Bundle args = getArguments();
        if (args == null) return EventFilterCriteria.empty();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return args.getSerializable(ARG_CRITERIA, EventFilterCriteria.class);
        }
        Object o = args.getSerializable(ARG_CRITERIA);
        return o instanceof EventFilterCriteria ? (EventFilterCriteria) o : EventFilterCriteria.empty();
    }

    private static void putCriteria(Bundle b, EventFilterCriteria c) {
        b.putSerializable(BUNDLE_CRITERIA, c);
    }
}
