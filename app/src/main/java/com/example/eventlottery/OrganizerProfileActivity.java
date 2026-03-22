package com.example.eventlottery;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Edit organizer profile (firstName, lastName, email, phone) in Firestore organizers/{deviceId}.
 */
public class OrganizerProfileActivity extends AppCompatActivity {

    private static final String COLLECTION_ORGANIZERS = "organizers";

    private FirebaseFirestore db;
    private String deviceId;

    private TextInputEditText editFirstName, editLastName, editEmail, editPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_profile);

        db = FirebaseFirestore.getInstance();
        deviceId = DeviceIdManager.getDeviceId(this);

        editFirstName = findViewById(R.id.edit_organizer_first_name);
        editLastName = findViewById(R.id.edit_organizer_last_name);
        editEmail = findViewById(R.id.edit_organizer_email);
        editPhone = findViewById(R.id.edit_organizer_phone);

        Toolbar toolbar = findViewById(R.id.toolbar_organizer_profile);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        findViewById(R.id.back_button).setOnClickListener(v -> finish());

        findViewById(R.id.btn_save_organizer).setOnClickListener(v -> saveProfile());

        loadProfile();
    }

    private void loadProfile() {
        db.collection(COLLECTION_ORGANIZERS).document(deviceId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Organizer organizer = doc.toObject(Organizer.class);
                        if (organizer != null) {
                            editFirstName.setText(organizer.getFirstName());
                            editLastName.setText(organizer.getLastName());
                            editEmail.setText(organizer.getEmail());
                            editPhone.setText(organizer.getPhoneNumber());
                        }
                    }
                });
    }

    private void saveProfile() {
        Organizer organizer = new Organizer();
        organizer.setOrganizerId(deviceId);
        organizer.setFirstName(editFirstName.getText() != null ? editFirstName.getText().toString().trim() : "");
        organizer.setLastName(editLastName.getText() != null ? editLastName.getText().toString().trim() : "");
        organizer.setEmail(editEmail.getText() != null ? editEmail.getText().toString().trim() : "");
        organizer.setPhoneNumber(editPhone.getText() != null ? editPhone.getText().toString().trim() : "");

        db.collection(COLLECTION_ORGANIZERS).document(deviceId).set(organizer)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, R.string.organizer_profile_saved, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to save profile", Toast.LENGTH_SHORT).show());
    }
}
