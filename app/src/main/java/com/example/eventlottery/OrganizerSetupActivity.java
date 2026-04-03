package com.example.eventlottery;

/**
 * First-time organizer registration: firstName, lastName, email, phone. Writes Organizer
 * to organizers/{deviceId}, then starts MainActivity. Uses merge so fields like FCM token persist.
 */
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

public class OrganizerSetupActivity extends BaseActivity {

    private static final String COLLECTION_ORGANIZERS = "organizers";

    private TextInputEditText firstNameInput;
    private TextInputEditText lastNameInput;
    private TextInputEditText emailInput;
    private TextInputEditText phoneInput;

    private FirebaseFirestore db;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_profile);

        db = FirebaseFirestore.getInstance();
        deviceId = DeviceIdManager.getDeviceId(this);

        Toolbar toolbar = findViewById(R.id.toolbar_organizer_profile);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        findViewById(R.id.back_button).setVisibility(android.view.View.GONE);

        firstNameInput = findViewById(R.id.edit_organizer_first_name);
        lastNameInput = findViewById(R.id.edit_organizer_last_name);
        emailInput = findViewById(R.id.edit_organizer_email);
        phoneInput = findViewById(R.id.edit_organizer_phone);

        findViewById(R.id.btn_save_organizer).setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String firstName = getText(firstNameInput);
        String lastName = getText(lastNameInput);
        String email = getText(emailInput);
        String phone = getText(phoneInput);

        if (TextUtils.isEmpty(firstName)) {
            firstNameInput.setError("First name is required");
            return;
        }

        if (TextUtils.isEmpty(lastName)) {
            lastNameInput.setError("Last name is required");
            return;
        }

        if (TextUtils.isEmpty(email)) {
            emailInput.setError("Email is required");
            return;
        }

        Organizer organizer = new Organizer(deviceId, firstName, lastName, email, phone);

        db.collection(COLLECTION_ORGANIZERS)
                .document(deviceId)
                .set(organizer, SetOptions.merge())
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Organizer profile saved successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this,
                                "Failed to save profile: " + e.getMessage(),
                                Toast.LENGTH_LONG).show()
                );
    }

    private String getText(TextInputEditText editText) {
        return editText.getText() == null ? "" : editText.getText().toString().trim();
    }
}