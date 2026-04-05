package com.example.eventlottery;

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
import com.google.firebase.firestore.WriteBatch;

/**
 * Lets an entrant edit their profile (name, email, phone, notification preference) in Firestore.
 * Also provides an option to delete the profile (US 01.04.03).
 */
public class EntrantProfileActivity extends BaseActivity {

    /** No-arg constructor required by the Android Activity lifecycle. */
    public EntrantProfileActivity() {}

    /** Firestore collection for entrant profiles (used when deleting user document). */
    public static final String USERS_COLLECTION = "users";

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
        db.collection(USERS_COLLECTION).document(deviceId).get()
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

        db.collection(USERS_COLLECTION).document(deviceId)
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
                .setTitle(R.string.delete_profile_title)
                .setMessage(R.string.delete_profile_message)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> deleteProfile())
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    /**
     * Deletes the entrant profile from Firestore and removes this user from all event waiting
     * lists, then returns to the welcome screen.
     */
    private void deleteProfile() {
        if (deviceId == null || deviceId.isEmpty()) {
            Toast.makeText(this, "Cannot delete: device ID not available.", Toast.LENGTH_LONG).show();
            return;
        }
        // Remove this user from all event waiting lists, then delete profile
        db.collectionGroup("waitingList")
                .whereEqualTo("deviceId", deviceId)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    WriteBatch batch = db.batch();
                    querySnapshot.getDocuments().forEach(doc -> batch.delete(doc.getReference()));
                    if (querySnapshot.isEmpty()) {
                        deleteUserDocument();
                    } else {
                        batch.commit()
                                .addOnSuccessListener(aVoid -> deleteUserDocument())
                                .addOnFailureListener(e -> {
                                    Log.w("EntrantProfile", "Failed to remove from waiting lists", e);
                                    deleteUserDocument();
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    Log.w("EntrantProfile", "Could not query waiting lists (index may be required)", e);
                    // Still delete profile so the user can leave the app
                    deleteUserDocument();
                });
    }

    private void deleteUserDocument() {
        db.collection(USERS_COLLECTION).document(deviceId)
                .delete()
                .addOnSuccessListener(aVoid -> runOnUiThread(() -> {
                    Toast.makeText(this, R.string.profile_deleted, Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, WelcomePageActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();
                }))
                .addOnFailureListener(e -> runOnUiThread(() ->
                        Toast.makeText(this, R.string.delete_profile_failed, Toast.LENGTH_LONG).show()));
    }
}