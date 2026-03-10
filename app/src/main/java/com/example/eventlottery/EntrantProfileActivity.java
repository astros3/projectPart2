package com.example.eventlottery;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Purpose: Controller that persists Entrant data to Firebase Firestore.
 * Pattern: Controller layer utilizing the Data Access Object (DAO) pattern.
 * Outstanding Issues: Requires a connected google-services.json file in the app folder.
 */
public class EntrantProfileActivity extends AppCompatActivity {

    private EditText firstNameInput, lastNameInput, emailInput, phoneInput;
    private FirebaseFirestore db;
    private String deviceId; // Unique key for this user

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_profile);

        db = FirebaseFirestore.getInstance();
        // Get a unique ID for this hardware
        deviceId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);

        // UI Initialization...
        firstNameInput = findViewById(R.id.edit_first_name);
        lastNameInput = findViewById(R.id.edit_last_name);
        emailInput = findViewById(R.id.edit_email);
        phoneInput = findViewById(R.id.edit_phone);

        // 1. Load existing data
        loadProfile();

        findViewById(R.id.btn_enter).setOnClickListener(v -> updateProfile());
    }

    /**
     * Fetches current profile data from Firestore and populates the UI.
     */
    private void loadProfile() {
        db.collection("users").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        Entrant existing = documentSnapshot.toObject(Entrant.class);
                        if (existing != null) {
                            firstNameInput.setText(existing.getFirstName());
                            lastNameInput.setText(existing.getLastName());
                            emailInput.setText(existing.getEmail());
                            phoneInput.setText(existing.getPhoneNumber());
                        }
                    }
                });
    }

    /**
     * Saves or Updates the profile using the unique Device ID.
     */
    private void updateProfile() {
        Entrant entrant = new Entrant(
                firstNameInput.getText().toString(),
                lastNameInput.getText().toString(),
                emailInput.getText().toString(),
                phoneInput.getText().toString()
        );

        // .set() with the deviceId will overwrite the old data (Update)
        db.collection("users").document(deviceId)
                .set(entrant)
                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e -> Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show());
    }
}