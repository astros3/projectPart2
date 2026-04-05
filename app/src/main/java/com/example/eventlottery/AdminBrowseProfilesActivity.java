package com.example.eventlottery;

import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.List;

/**
 * Activity for administrators to browse and delete all profiles (Entrants and Organizers).
 * Implements US 03.05.01 (Browse)
 */
public class AdminBrowseProfilesActivity extends BaseActivity {

    /** No-arg constructor required by the Android Activity lifecycle. */
    public AdminBrowseProfilesActivity() {}

    private ArrayList<Entrant> displayList;
    private ArrayList<Entrant> backupList;
    private AdminBrowseProfilesAdapter adapter;
    private EditText searchInput;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = FirebaseFirestore.getInstance();
        String deviceId = DeviceIdManager.getDeviceId(this);

        // Security check matching AdminEventControlScreenActivity
        db.collection("admins").document(deviceId).get().addOnSuccessListener(doc -> {
            if (!doc.exists()) {
                Toast.makeText(this, "Access Denied", Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            setupUI();
        });
    }

    private void setupUI() {
        setContentView(R.layout.admin_browse_profiles_screen);

        searchInput = findViewById(R.id.search_input_bar);
        ImageView searchBtn = findViewById(R.id.search_icon);
        ImageView backBtn = findViewById(R.id.back_button);
        ListView listView = findViewById(R.id.profile_list_view);

        displayList = new ArrayList<>();
        backupList = new ArrayList<>();
        adapter = new AdminBrowseProfilesAdapter(this, displayList);
        listView.setAdapter(adapter);

        fetchData();

        searchBtn.setOnClickListener(v -> filterResults());
        backBtn.setOnClickListener(v -> finish());
    }

    private void fetchData() {
        // Query both collections simultaneously
        Task<QuerySnapshot> usersTask = db.collection("users").get();
        Task<QuerySnapshot> orgsTask = db.collection("organizers").get();

        Tasks.whenAllComplete(usersTask, orgsTask).addOnCompleteListener(t -> {
            displayList.clear();
            backupList.clear();

            // Process Entrants from "users" collection
            if (usersTask.isSuccessful()) {
                for (QueryDocumentSnapshot doc : usersTask.getResult()) {
                    Entrant e = doc.toObject(Entrant.class);
                    if (e.getDeviceID() == null) e.setDeviceID(doc.getId());
                    displayList.add(e);
                    backupList.add(e);
                }
            }

            // Process Organizers from "organizers" collection
            if (orgsTask.isSuccessful()) {
                for (QueryDocumentSnapshot doc : orgsTask.getResult()) {
                    Organizer o = doc.toObject(Organizer.class);
                    // Standardize Organizer into Entrant format for the combined list
                    Entrant mapped = new Entrant(o.getOrganizerId(), o.getFullName(),
                            o.getEmail(), o.getPhoneNumber(), "Organizer");
                    displayList.add(mapped);
                    backupList.add(mapped);
                }
            }
            adapter.notifyDataSetChanged();
        });
    }

    private void filterResults() {
        String query = searchInput.getText().toString().toLowerCase().trim();
        displayList.clear();
        for (Entrant item : backupList) {
            if (item.getFullName().toLowerCase().contains(query)) {
                displayList.add(item);
            }
        }
        adapter.notifyDataSetChanged();
    }
}