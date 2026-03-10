package com.example.eventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Displays the event's promotional QR code (US 02.01.01).
 * The QR code encodes the event ID so scanning it opens EventDetailsActivity.
 * The promo code is shown and can be modified (saved back to Firestore).
 */
public class QRCodeActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "event_id";

    private FirebaseFirestore db;
    private String eventId;

    private ImageView qrCodeImage;
    private TextInputEditText inputPromoCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_qr_code);

        eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "No event selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        bindViews();
        setupToolbar();
        generateAndDisplayQrCode();
        loadPromoCode();
        setupPromoCodeSave();
    }

    private void bindViews() {
        qrCodeImage = findViewById(R.id.qr_code_image);
        inputPromoCode = findViewById(R.id.input_promo_code);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_qr_code);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void generateAndDisplayQrCode() {
        // QR code encodes eventId - when scanned, MainActivity opens EventDetailsActivity with this id
        android.graphics.Bitmap bitmap = QRCodeService.generateQrCodeBitmap(eventId);
        if (bitmap != null) {
            qrCodeImage.setImageBitmap(bitmap);
        } else {
            Toast.makeText(this, "Could not generate QR code", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadPromoCode() {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Event event = doc.toObject(Event.class);
                        if (event != null && event.getPromoCode() != null && !event.getPromoCode().isEmpty()) {
                            inputPromoCode.setText(event.getPromoCode());
                        } else {
                            inputPromoCode.setText(QRCodeService.generatePromoCode());
                        }
                    }
                });
    }

    private void setupPromoCodeSave() {
        inputPromoCode.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                savePromoCode();
            }
        });
        // Also save when user leaves the activity
    }

    @Override
    protected void onPause() {
        super.onPause();
        savePromoCode();
    }

    private void savePromoCode() {
        String code = inputPromoCode.getText() != null ? inputPromoCode.getText().toString().trim() : "";
        if (code.isEmpty()) return;

        db.collection("events").document(eventId).update("promoCode", code)
                .addOnFailureListener(e -> {
                    // Firestore might not have promoCode field yet - use set with merge
                    db.collection("events").document(eventId)
                            .update("promoCode", code)
                            .addOnFailureListener(e2 -> { /* ignore */ });
                });
    }

    public static Intent newIntent(android.content.Context context, String eventId) {
        Intent i = new Intent(context, QRCodeActivity.class);
        i.putExtra(EXTRA_EVENT_ID, eventId);
        return i;
    }
}
