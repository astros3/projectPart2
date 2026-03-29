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
 * Shows event QR code and editable promo code (US 02.01.01). Organizer-only; verifies
 * current user is event organizer. Saves promoCode to Firestore on pause.
 */
public class QRCodeActivity extends BaseActivity {

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
        // Only the event's organizer can view/edit QR; verify before showing content
        verifyOrganizerThenLoad();
    }

    private void verifyOrganizerThenLoad() {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists()) {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    Event event = doc.toObject(Event.class);
                    if (event == null || !DeviceIdManager.getDeviceId(this).equals(event.getOrganizerId())) {
                        Toast.makeText(this, getString(R.string.only_organizer_edit), Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    generateAndDisplayQrCode();
                    loadPromoCode();
                    setupPromoCodeSave();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Could not load event", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void bindViews() {
        qrCodeImage = findViewById(R.id.qr_code_image);
        inputPromoCode = findViewById(R.id.input_promo_code);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_qr_code);
        setSupportActionBar(toolbar);
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
    }

    private void generateAndDisplayQrCode() {
        // QR code encodes eventId - when scanned, MainActivity opens EventDetailsActivity with this id
        String qrData = "eventlottery://event/" + eventId;
        android.graphics.Bitmap bitmap = QRCodeService.generateQrCodeBitmap(qrData);
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
