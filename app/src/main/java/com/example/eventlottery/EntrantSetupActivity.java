package com.example.eventlottery;

/**
 * First-time entrant registration: name, email, phone. Writes Entrant to users/{deviceId}
 * with role "entrant", then starts EntrantMainScreenActivity.
 */
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

public class EntrantSetupActivity extends BaseActivity {

    private TextInputEditText firstNameInput;
    private TextInputEditText lastNameInput;
    private TextInputEditText phoneInput;
    private TextInputEditText emailInput;
    private CheckBox rememberCheck;
    private MaterialButton enterButton;

    private FirebaseFirestore db;
    private String deviceId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_profile);

        db = FirebaseFirestore.getInstance();
        deviceId = DeviceIdManager.getDeviceId(this);

        firstNameInput = findViewById(R.id.edit_first_name);
        lastNameInput = findViewById(R.id.edit_last_name);
        phoneInput = findViewById(R.id.edit_phone);
        emailInput = findViewById(R.id.edit_email);
        enterButton = findViewById(R.id.btn_enter);

        enterButton.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String firstName = getText(firstNameInput);
        String lastName = getText(lastNameInput);
        String phone = getText(phoneInput);
        String email = getText(emailInput);

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

        String fullName = firstName + " " + lastName;

        Entrant entrant = new Entrant(
                deviceId,
                fullName,
                email,
                phone,
                "entrant"
        );

        db.collection("users")
                .document(deviceId)
                .set(entrant)
                .addOnSuccessListener(unused -> {
                    Toast.makeText(this, "Profile saved successfully", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(this, EntrantMainScreenActivity.class));
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