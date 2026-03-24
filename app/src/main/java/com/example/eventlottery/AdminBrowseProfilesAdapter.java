package com.example.eventlottery;

import android.content.Context;
import android.view.LayoutInflater;
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
            // Determine collection based on role to fulfill removal requirement
            String path = user.getRole().equalsIgnoreCase("Organizer") ? "organizers" : "users";

            db.collection(path).document(user.getDeviceID()).delete()
                    .addOnSuccessListener(aVoid -> {
                        remove(user);
                        notifyDataSetChanged();
                    });
        });

        return convertView;
    }
}