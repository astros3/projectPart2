package com.example.eventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class AdminMainScreenActivity extends BaseActivity {
    private ImageView backButton;
    private LinearLayout adminReviewEventsButton;
    private LinearLayout adminReviewProfileButton;
    private LinearLayout adminReviewImageButton;
    private LinearLayout adminReviewNotificationLogButton;
    private LinearLayout adminProfileButton;
    private LinearLayout adminMakeAnnouncementButton;
    /**
     * the following will run when this activity/screen is opened
     * @param savedInstanceState *
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // Restrict access: only devices with an entry in Firestore "admins" collection may open this screen
        String deviceId = DeviceIdManager.getDeviceId(this);
        FirebaseFirestore dbCheck = FirebaseFirestore.getInstance();
        dbCheck.collection("admins").document(deviceId).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (!documentSnapshot.exists()) {
                        Toast.makeText(this, "Access denied. You must be an admin to access this.", Toast.LENGTH_LONG).show();
                        finish();
                        return;
                    }
                    setContentView(R.layout.admin_main_screen);
                    MainNavigation();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to verify admin: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    finish();
                });
    }
    public void MainNavigation(){
        backButton = findViewById(R.id.back_button);
        adminReviewEventsButton = findViewById(R.id.admin_review_events_button);
        adminReviewProfileButton = findViewById(R.id.admin_review_profile_button);
        adminReviewImageButton = findViewById(R.id.admin_review_image_button);
        adminReviewNotificationLogButton = findViewById(R.id.admin_review_notification_log_button);
        adminProfileButton = findViewById(R.id.admin_profile_button);

        backButton.setOnClickListener(v -> {
            startActivity(new Intent(AdminMainScreenActivity.this, WelcomePageActivity.class));
            finish();
        });

        adminReviewEventsButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainScreenActivity.this, AdminEventControlScreenActivity.class);
            startActivity(intent);
        });

        adminReviewProfileButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainScreenActivity.this, AdminBrowseProfilesActivity.class);
            startActivity(intent);
        });

        adminReviewImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainScreenActivity.this, AdminBrowseImagesActivity.class);
            startActivity(intent);
        });

        adminReviewNotificationLogButton.setOnClickListener(v -> {
            Intent intent = new Intent(AdminMainScreenActivity.this, AdminNotificationLogControlScreenActivity.class);
            startActivity(intent);
        });

        adminProfileButton.setOnClickListener(v ->
                startActivity(new Intent(AdminMainScreenActivity.this, AdminProfileActivity.class)));

    }
}
