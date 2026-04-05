package com.example.eventlottery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import com.bumptech.glide.Glide;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;

/**
 * Adapter that populates a GridView with event poster images.
 * Each grid item shows the poster and a delete button that removes the image from Firestore.
 */
public class AdminBrowseImagesAdapter extends ArrayAdapter<Event> {
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    /**
     * Creates a new adapter for the given event list.
     *
     * @param context the hosting context
     * @param events  list of events whose poster URIs will be displayed
     */
    public AdminBrowseImagesAdapter(Context context, ArrayList<Event> events) {
        super(context, 0, events);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.admin_image_item, parent, false);
        }

        Event event = getItem(position);
        ImageView imageView = convertView.findViewById(R.id.admin_image_view);
        ImageView deleteBtn = convertView.findViewById(R.id.delete_image_button);

        // Uses Glide to load the posterUri from the Event class
        Glide.with(getContext())
                .load(event.getPosterUri())
                .centerCrop()
                .into(imageView);

        // US 03.03.01: Remove image by updating Firestore
        deleteBtn.setOnClickListener(v -> {
            db.collection("events").document(event.getEventId())
                    .update("posterUri", null)
                    .addOnSuccessListener(aVoid -> {
                        remove(event);
                        notifyDataSetChanged();
                    });
        });

        return convertView;
    }
}