package com.example.eventlottery;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.appcompat.app.AlertDialog;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

/**
 * Adapter to display mixed profiles and handle profile removal.
 */
public class AdminBrowseProfilesAdapter extends ArrayAdapter<Entrant> {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public AdminBrowseProfilesAdapter(Context context, ArrayList<Entrant> profiles) {
        super(context, 0, profiles);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.admin_profile_item, parent, false);
        }

        Entrant user = getItem(position);
        TextView nameView = convertView.findViewById(R.id.profile_name);
        TextView roleView = convertView.findViewById(R.id.profile_role);
        ImageView deleteBtn = convertView.findViewById(R.id.delete_profile_button);

        // UI population using safe getters
        nameView.setText(user.getFullName());
        roleView.setText(user.getRole());

        deleteBtn.setOnClickListener(v -> {
            String role = user.getRole() != null ? user.getRole() : "";
            String path = role.equalsIgnoreCase("Organizer") ? "organizers" : "users";
            String nameForMsg = user.getFullName();
            new AlertDialog.Builder(getContext())
                    .setTitle(R.string.admin_delete_profile_title)
                    .setMessage(getContext().getString(R.string.admin_delete_profile_message, role, nameForMsg))
                    .setPositiveButton(R.string.admin_delete_action, (dialog, which) ->
                            db.collection(path).document(user.getDeviceID()).delete()
                                    .addOnSuccessListener(aVoid -> {
                                        remove(user);
                                        notifyDataSetChanged();
                                    }))
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        });

        return convertView;
    }
}