package com.example.eventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

public class WelcomePageActivity extends AppCompatActivity {

    LinearLayout userbutton;
    LinearLayout organizerbutton;
    LinearLayout adminbutton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_page);

        userbutton = findViewById(R.id.userbutton);
        organizerbutton = findViewById(R.id.organizerbutton);
        adminbutton = findViewById(R.id.adminbutton);

        // Entrant/User
        userbutton.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomePageActivity.this, EntrantMainScreenActivity.class);
            startActivity(intent);
        });

        // Organizer
        organizerbutton.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomePageActivity.this, MainActivity.class);
            startActivity(intent);
        });

        // Admin
        adminbutton.setOnClickListener(v -> {
            Intent intent = new Intent(WelcomePageActivity.this, AdminEventControlScreenActivity.class);
            startActivity(intent);
        });
    }
}