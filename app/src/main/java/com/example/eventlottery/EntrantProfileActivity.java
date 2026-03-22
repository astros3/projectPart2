package com.example.eventlottery;

/**
 * Edit entrant profile in Firestore users/{deviceId}. Load/save name, email, phone,
 * and notification preference (US 01.04.03). Optional delete profile.
 * Bottom nav: Home, Scan, History, Profile.
 */
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.firestore.FirebaseFirestore;

public class EntrantProfileActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, phoneInput;
    private TextView displayName, displayEmail;
    private SwitchMaterial notifSwitch;
    private FirebaseFirestore db;
    private String deviceId;

    private Double currentLatitude;
    private Double currentLongitude;
    private String currentLocationAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();
        deviceId = DeviceIdManager.getDeviceId(this);

        nameInput  = findViewById(R.id.edit_profile_name);
        emailInput = findViewById(R.id.edit_profile_email);
        phoneInput = findViewById(R.id.edit_profile_phone);

        displayName  = findViewById(R.id.profile_display_name);
        displayEmail = findViewById(R.id.profile_display_email);

        // US 01.04.03 — notification opt-out toggle
        notifSwitch = findViewById(R.id.switch_notifications);

        loadProfile();

        findViewById(R.id.btn_save_changes).setOnClickListener(v -> updateProfile());
        findViewById(R.id.btn_delete_profile).setOnClickListener(v -> confirmDeleteProfile());

        // Bottom navigation
        findViewById(R.id.navigation_home_button).setOnClickListener(v -> {
            startActivity(new Intent(this, EntrantMainScreenActivity.class));
            finish();
        });
        findViewById(R.id.navigation_scan_button).setOnClickListener(v -> {
            startActivity(new Intent(this, QRCodeActivity.class));
            finish();
        });
        findViewById(R.id.navigation_history_button).setOnClickListener(v -> {
            startActivity(new Intent(this, EntrantHistoryScreenActivity.class));
            finish();
        });
        findViewById(R.id.navigation_profile_button).setOnClickListener(v -> { /* already here */ });
    }

    private void loadProfile() {
        db.collection("users").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Entrant existing = documentSnapshot.toObject(Entrant.class);
                        if (existing != null) {
                            String fullName = existing.getFullName();
                            nameInput.setText(fullName);
                            emailInput.setText(existing.getEmail());
                            phoneInput.setText(existing.getPhone());

                            displayName.setText(fullName);
                            displayEmail.setText(existing.getEmail());

                            currentLatitude        = existing.getLatitude();
                            currentLongitude       = existing.getLongitude();
                            currentLocationAddress = existing.getLocationAddress();

                            // US 01.04.03 — load saved notification preference
                            notifSwitch.setChecked(existing.isNotificationsEnabled());
                        }
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e("FirestoreError", "Failed to load profile", e);
                    Toast.makeText(this,
                            "Failed to load profile: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
    }

    private void updateProfile() {
        if (deviceId == null || deviceId.isEmpty()) {
            Toast.makeText(this, "Cannot save: device ID not available.", Toast.LENGTH_LONG).show();
            return;
        }

        String fullName = nameInput.getText().toString().trim();

        Entrant entrant = new Entrant(
                deviceId,
                fullName,
                emailInput.getText().toString().trim(),
                phoneInput.getText().toString().trim(),
                "entrant"
        );
        entrant.setLatitude(currentLatitude);
        entrant.setLongitude(currentLongitude);
        entrant.setLocationAddress(currentLocationAddress);

        // US 01.04.03 — persist notification preference alongside profile
        entrant.setNotificationsEnabled(notifSwitch.isChecked());

        db.collection("users").document(deviceId)
                .set(entrant)
                .addOnSuccessListener(aVoid -> runOnUiThread(() -> {
                    displayName.setText(entrant.getFullName());
                    displayEmail.setText(entrant.getEmail());
                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                }))
                .addOnFailureListener(e -> runOnUiThread(() ->
                        Toast.makeText(this,
                                "Update Failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()));
    }

    private void confirmDeleteProfile() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Profile")
                .setMessage("Are you sure you want to delete your profile? This cannot be undone.")
                .setPositiveButton(android.R.string.yes, (dialog, which) -> deleteProfile())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void deleteProfile() {
        if (deviceId == null || deviceId.isEmpty()) {
            Toast.makeText(this, "Cannot delete: device ID not available.", Toast.LENGTH_LONG).show();
            return;
        }
        db.collection("users").document(deviceId)
                .delete()
                .addOnSuccessListener(aVoid -> runOnUiThread(() -> {
                    Toast.makeText(this, "Profile deleted.", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, WelcomePageActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }))
                .addOnFailureListener(e -> runOnUiThread(() ->
                        Toast.makeText(this,
                                "Delete failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()));
    }
}