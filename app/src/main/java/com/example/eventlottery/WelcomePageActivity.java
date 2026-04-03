package com.example.eventlottery;

/**
 * Launcher: role selection (Entrant / Organizer / Admin). Profiles live in separate
 * Firestore collections ({@code users}, {@code organizers}, {@code admins}) keyed by device ID.
 * An admin device may hold at most one profile per role; each profile is edited in its own flow
 * (no syncing of name or other fields from admin into entrant/organizer).
 */
import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class WelcomePageActivity extends BaseActivity {

    LinearLayout userbutton;
    LinearLayout organizerbutton;
    LinearLayout adminbutton;

    private final ActivityResultLauncher<String> notificationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                // Permission granted or denied — proceed regardless; in-app notifications still work
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_page);

        NotificationChannelHelper.createChannel(this);
        requestNotificationPermissionIfNeeded();
        registerFcmToken();

        userbutton = findViewById(R.id.userbutton);
        organizerbutton = findViewById(R.id.organizerbutton);
        adminbutton = findViewById(R.id.adminbutton);
        adminbutton.setVisibility(View.GONE); // show only after we confirm user is in "admins" collection

        // Admin button visible only to users who have an entry in Firestore "admins" collection
        String deviceId = DeviceIdManager.getDeviceId(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Admin button only if admins/{deviceId} exists; optional role-only fix for legacy docs
        db.collection("admins").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        adminbutton.setVisibility(View.VISIBLE);
                        ensureCorrectRoleOnExistingProfiles(db, deviceId);
                    } else {
                        adminbutton.setVisibility(View.GONE);
                    }
                });

        // Entrant/User
        userbutton.setOnClickListener(v -> {



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

        // Admin: only allow if this device has an entry in Firestore "admins" collection
        adminbutton.setOnClickListener(v -> {
            String adminDeviceId = DeviceIdManager.getDeviceId(this);
            FirebaseFirestore adminDb = FirebaseFirestore.getInstance();
            adminDb.collection("admins").document(adminDeviceId).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            startActivity(new Intent(WelcomePageActivity.this, AdminMainScreenActivity.class));
                            finish();
                        } else {
                            Toast.makeText(this, "Access denied. You must be an admin to access this.", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to verify admin: " + e.getMessage(), Toast.LENGTH_LONG).show()
                    );
        });
    }

    private void requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }

    /** Fetches the FCM token and stores it on any profile documents that exist for this device. */
    private void registerFcmToken() {
        String deviceId = DeviceIdManager.getDeviceId(this);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(token -> {
            Map<String, Object> update = new HashMap<>();
            update.put("fcmToken", token);
            for (String collection : new String[] { "users", "organizers", "admins" }) {
                db.collection(collection).document(deviceId).update(update)
                        .addOnFailureListener(e -> { /* no doc for this role yet */ });
            }
        });
    }

    /**
     * If entrant/organizer documents already exist (e.g. legacy missing {@code role}),
     * merge only {@code role} so welcome routing works. Does not create stubs or copy admin fields.
     */
    private void ensureCorrectRoleOnExistingProfiles(FirebaseFirestore db, String deviceId) {
        db.collection("users").document(deviceId).get().addOnSuccessListener(usersDoc -> {
            if (!usersDoc.exists()) {
                return;
            }
            String r = usersDoc.getString("role");
            if (r == null || !"entrant".equals(r)) {
                Map<String, Object> patch = new HashMap<>();
                patch.put("role", "entrant");
                db.collection("users").document(deviceId).set(patch, SetOptions.merge());
            }
        });
        db.collection("organizers").document(deviceId).get().addOnSuccessListener(orgDoc -> {
            if (!orgDoc.exists()) {
                return;
            }
            String r = orgDoc.getString("role");
            if (r == null || !"organizer".equals(r)) {
                Map<String, Object> patch = new HashMap<>();
                patch.put("role", "organizer");
                db.collection("organizers").document(deviceId).set(patch, SetOptions.merge());
            }
        });
    }
}
