package com.example.eventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;

/**
 * This activity is the organizer hub: it hosts NavHostFragment (dashboard, navigation, lottery, waiting, selected).
 * It exposes a QR scanner that opens EventDetailsActivity with the scanned event id.
 */
public class MainActivity extends BaseActivity {

    /** No-arg constructor required by the Android Activity lifecycle. */
    public MainActivity() {}

    private final ActivityResultLauncher<ScanOptions> qrScanner =
            registerForActivityResult(new ScanContract(), result -> {
                if (result.getContents() != null) {
                    String scannedValue = result.getContents().trim();
                    if (scannedValue.contains("/")) {
                        scannedValue = scannedValue.substring(scannedValue.lastIndexOf("/") + 1);
                    }
                    Log.d("QR_SCAN", scannedValue);

                    Intent intent = new Intent(MainActivity.this, EventDetailsActivity.class);
                    intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, scannedValue);
                    startActivity(intent);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.nav_host_fragment), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    /**
     * Launches the barcode scanner restricted to QR codes for event check-in / browse flow.
     */
    public void launchQrScanner() {
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Scan event QR code");
        qrScanner.launch(options);
    }
}
