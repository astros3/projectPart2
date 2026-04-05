package com.example.eventlottery;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

/**
 * Edit the signed-in admin's profile (firstName, lastName, email, optional phone)
 * in Firestore admins/{deviceId}. No notification toggle on this screen.
 */
public class AdminProfileActivity extends AppCompatActivity {

    private static final String COLLECTION_ADMINS = "admins";

    private FirebaseFirestore db;
    private String deviceId;

    private TextInputEditText editFirstName, editLastName, editEmail, editPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        deviceId = DeviceIdManager.getDeviceId(this);
        db = FirebaseFirestore.getInstance();

        db.collection(COLLECTION_ADMINS).document(deviceId).get()
                .addOnSuccessListener(doc -> {
                    if (doc == null || !doc.exists()) {
                        Toast.makeText(this,
                                "Access denied. You must be an admin to access this.",
                                Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                    setContentView(R.layout.activity_admin_profile);
                    bindUi();
                    loadProfile();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this,
                            "Failed to verify admin: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                    finish();
                });
    }

    private void bindUi() {
        editFirstName = findViewById(R.id.edit_admin_first_name);
        editLastName = findViewById(R.id.edit_admin_last_name);
        editEmail = findViewById(R.id.edit_admin_email);
        editPhone = findViewById(R.id.edit_admin_phone);

        Toolbar toolbar = findViewById(R.id.toolbar_admin_profile);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
        findViewById(R.id.btn_save_admin).setOnClickListener(v -> saveProfile());
    }

    private void loadProfile() {
        db.collection(COLLECTION_ADMINS).document(deviceId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        AdminProfile profile = doc.toObject(AdminProfile.class);
                        if (profile != null) {
                            if (profile.getFirstName() != null) {
                                editFirstName.setText(profile.getFirstName());
                            }
                            if (profile.getLastName() != null) {
                                editLastName.setText(profile.getLastName());
                            }
                            if (profile.getEmail() != null) {
                                editEmail.setText(profile.getEmail());
                            }
                            if (profile.getPhoneNumber() != null) {
                                editPhone.setText(profile.getPhoneNumber());
                            }
                        }
                    }
                });
    }

    private void saveProfile() {
        Map<String, Object> data = new HashMap<>();
        data.put("adminId", deviceId);
        data.put("firstName", textOrEmpty(editFirstName));
        data.put("lastName", textOrEmpty(editLastName));
        data.put("email", textOrEmpty(editEmail));
        data.put("phoneNumber", textOrEmpty(editPhone));

        db.collection(COLLECTION_ADMINS).document(deviceId).set(data, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(this, R.string.admin_profile_saved, Toast.LENGTH_SHORT).show();
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, R.string.admin_profile_save_failed, Toast.LENGTH_SHORT).show());
    }

    private static String textOrEmpty(TextInputEditText field) {
        if (field.getText() == null) return "";
        return field.getText().toString().trim();
    }
}
