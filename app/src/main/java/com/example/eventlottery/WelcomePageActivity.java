package com.example.eventlottery;

/**
 * Launcher: role selection (Entrant / Organizer / Admin). Routes to setup or main screen
 * based on Firestore profile (users vs organizers). Issue: Organizer doc has no "role" field,
 * so existing organizers are always sent to setup.
 */
import android.content.Intent;
import android.os.Bundle;
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

        // Entrant/User
        userbutton.setOnClickListener(v -> {

            String deviceId = DeviceIdManager.getDeviceId(this);
            FirebaseFirestore db = FirebaseFirestore.getInstance();

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

            String deviceId = DeviceIdManager.getDeviceId(this);
            FirebaseFirestore db = FirebaseFirestore.getInstance();

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

        // Admin
        adminbutton.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomePageActivity.this, AdminEventControlScreenActivity.class);
            startActivity(intent);
        });
    }
}