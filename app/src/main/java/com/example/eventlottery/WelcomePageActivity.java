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
        db.collection("admins").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    adminbutton.setVisibility(documentSnapshot.exists() ? View.VISIBLE : View.GONE);
                })
                .addOnFailureListener(e -> adminbutton.setVisibility(View.GONE));

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
                            startActivity(new Intent(WelcomePageActivity.this, AdminEventControlScreenActivity.class));
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
}