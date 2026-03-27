package com.example.eventlottery;

/**
 * Launcher: role selection (Entrant / Organizer / Admin). Routes to setup or main screen
 * based on Firestore profile (users vs organizers). Issue: Organizer doc has no "role" field,
 * so existing organizers are always sent to setup.
 */
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class WelcomePageActivity extends AppCompatActivity {

    LinearLayout userbutton;
    LinearLayout organizerbutton;
    LinearLayout adminbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_page);

        userbutton = findViewById(R.id.userbutton);
        organizerbutton = findViewById(R.id.organizerbutton);
        adminbutton = findViewById(R.id.adminbutton);
        adminbutton.setVisibility(View.GONE); // show only after we confirm user is in "admins" collection

        // Admin button visible only to users who have an entry in Firestore "admins" collection
        String deviceId = DeviceIdManager.getDeviceId(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Initial Admin Check & Auto-Enrollment (US 03.09.01)
        db.collection("admins").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        adminbutton.setVisibility(View.VISIBLE);

                        // Admin found! Mirror their ID to other collections so they gain full access
                        String adminName = documentSnapshot.getString("name");
                        if (adminName == null) adminName = "Admin User";
                        enrollAdminInAllRoles(db, deviceId, adminName);
                    } else {
                        adminbutton.setVisibility(View.GONE);
                    }
                });

        // Entrant/User
        userbutton.setOnClickListener(v -> {



            db.collection("users")
                    .document(deviceId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {

                        if (documentSnapshot.exists() &&
                                "entrant".equals(documentSnapshot.getString("role"))) {

                            // Existing entrant device
                            startActivity(new Intent(this, EntrantMainScreenActivity.class));

                        } else {

                            // New device → open setup screen
                            startActivity(new Intent(this, EntrantSetupActivity.class));
                        }

                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Failed to check profile: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show()
                    );
        });

        // Organizer
        organizerbutton.setOnClickListener(v -> {



            db.collection("organizers")
                    .document(deviceId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {

                        if (documentSnapshot.exists() &&
                                "organizer".equals(documentSnapshot.getString("role"))) {

                            // Existing organizer device
                            startActivity(new Intent(this, MainActivity.class));

                        } else {

                            // New organizer device → setup screen
                            startActivity(new Intent(this, OrganizerSetupActivity.class));
                        }

                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this,
                                    "Failed to check profile: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show()
                    );
        });

        // Admin: only allow if this device has an entry in Firestore "admins" collection
        adminbutton.setOnClickListener(v -> {
            String adminDeviceId = DeviceIdManager.getDeviceId(this);
            FirebaseFirestore adminDb = FirebaseFirestore.getInstance();
            adminDb.collection("admins").document(adminDeviceId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            startActivity(new Intent(WelcomePageActivity.this, AdminMainScreenActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Access denied. You must be an admin to access this.", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to verify admin: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });
    }

    /**
     * US 03.09.01: Ensures Admin ID is present in all role collections.
     * Uses merge() so we don't overwrite existing user/organizer data.
     */
    private void enrollAdminInAllRoles(FirebaseFirestore db, String deviceId, String name) {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);

        // Ensure "role" field matches what your buttons expect
        Map<String, Object> userData = new HashMap<>(data);
        userData.put("role", "entrant");

        Map<String, Object> orgData = new HashMap<>(data);
        orgData.put("role", "organizer");

        db.collection("users").document(deviceId).set(userData, SetOptions.merge());
        db.collection("organizers").document(deviceId).set(orgData, SetOptions.merge());
    }
}
