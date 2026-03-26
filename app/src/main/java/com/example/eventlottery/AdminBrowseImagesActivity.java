package com.example.eventlottery;

import android.os.Bundle;
import android.util.Log;
import android.widget.GridView;
import android.widget.ImageView;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;

public class AdminBrowseImagesActivity extends AppCompatActivity {

    private GridView imagesGrid;
    private AdminBrowseImagesAdapter adapter;
    private ArrayList<Event> eventImagesList;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.admin_browse_images);

        // 1. Initialize Firestore and Data List
        db = FirebaseFirestore.getInstance();
        eventImagesList = new ArrayList<>();

        // 2. Initialize UI Components
        imagesGrid = findViewById(R.id.admin_images_grid);
        ImageView backButton = findViewById(R.id.back_button);

        // 3. Setup the Adapter
        // This connects your list to the GridView UI
        adapter = new AdminBrowseImagesAdapter(this, eventImagesList);
        imagesGrid.setAdapter(adapter);

        // 4. Back button navigation
        backButton.setOnClickListener(v -> finish());

        // 5. Load the images
        fetchImagesFromFirestore();
    }

    /**
     * Retrieves all events that have an uploaded poster.
     * Maps the Firestore data to the Event model.
     */
    private void fetchImagesFromFirestore() {
        db.collection("events")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        eventImagesList.clear(); // Prevent duplicates on refresh

                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String posterUri = document.getString("posterUri");

                            // Only add to the list if there is an actual image to display
                            if (posterUri != null && !posterUri.isEmpty()) {
                                Event event = document.toObject(Event.class);
                                event.setEventId(document.getId()); // Store ID for deletion logic
                                eventImagesList.add(event);
                            }
                        }

                        // Trigger the GridView to redraw with the new data
                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e("AdminBrowseImages", "Error getting documents: ", task.getException());
                    }
                });
    }
}