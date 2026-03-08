package com.example.eventlottery;

import android.os.Bundle;
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
    private Button enterButton;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_profile);

        // Initialize Firestore
        db = FirebaseFirestore.getInstance();

        // View Binding
        firstNameInput = findViewById(R.id.edit_first_name);
        lastNameInput = findViewById(R.id.edit_last_name);
        emailInput = findViewById(R.id.edit_email);
        phoneInput = findViewById(R.id.edit_phone);
        enterButton = findViewById(R.id.btn_enter);

        enterButton.setOnClickListener(v -> saveToFirebase());
    }

    /**
     * Captures UI input and pushes a new Entrant document to the "users" collection.
     * Maps to requirement: "Distinguish between entrants, organizers, and admin".
     */
    private void saveToFirebase() {
        String fName = firstNameInput.getText().toString().trim();
        String lName = lastNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String phone = phoneInput.getText().toString().trim();

        if (fName.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Name and Email are required!", Toast.LENGTH_SHORT).show();
            return;
        }

        // Create the model
        Entrant entrant = new Entrant(fName, lName, email, phone);

        // Save to Firestore under a "users" collection
        // In a real app, use the Firebase Auth UID instead of a random ID
        db.collection("users")
                .add(entrant)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(EntrantProfileActivity.this, "Profile Saved!", Toast.LENGTH_SHORT).show();
                    // Navigate to next screen (e.g., QR Scanner)
                })
                .addOnFailureListener(e -> {
                    Log.e("FirebaseError", "Error adding document", e);
                    Toast.makeText(EntrantProfileActivity.this, "Save Failed", Toast.LENGTH_SHORT).show();
                });
    }
}