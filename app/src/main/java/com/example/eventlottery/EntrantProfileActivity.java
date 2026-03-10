package com.example.eventlottery;

import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class EntrantProfileActivity extends AppCompatActivity {

    private EditText nameInput, emailInput, phoneInput;
    private TextView displayName, displayEmail;
    private FirebaseFirestore db;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        db = FirebaseFirestore.getInstance();
        deviceId = DeviceIdManager.getDeviceId(this);

        nameInput = findViewById(R.id.edit_profile_name);
        emailInput = findViewById(R.id.edit_profile_email);
        phoneInput = findViewById(R.id.edit_profile_phone);

        displayName = findViewById(R.id.profile_display_name);
        displayEmail = findViewById(R.id.profile_display_email);

        loadProfile();

        findViewById(R.id.btn_save_changes).setOnClickListener(v -> updateProfile());
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
                            phoneInput.setText(existing.getPhoneNumber());

                            displayName.setText(fullName);
                            displayEmail.setText(existing.getEmail());
                        }
                    }
                });
    }

    private void updateProfile() {
        String fullName = nameInput.getText().toString().trim();
        String[] parts = fullName.split(" ", 2);

        String firstName = parts.length > 0 ? parts[0] : "";
        String lastName = parts.length > 1 ? parts[1] : "";

        Entrant entrant = new Entrant(
                firstName,
                lastName,
                emailInput.getText().toString().trim(),
                phoneInput.getText().toString().trim()
        );

        db.collection("users").document(deviceId)
                .set(entrant)
                .addOnSuccessListener(aVoid -> {
                    displayName.setText(entrant.getFullName());
                    displayEmail.setText(entrant.getEmail());
                    Toast.makeText(this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show());
    }
}