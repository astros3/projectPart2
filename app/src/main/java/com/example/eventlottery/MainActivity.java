package com.example.eventlottery;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

//QR scanner imports
import com.journeyapps.barcodescanner.ScanContract;
import com.journeyapps.barcodescanner.ScanOptions;
import androidx.activity.result.ActivityResultLauncher;

public class MainActivity extends AppCompatActivity {

//creates a QR scanner launcher that opens the camera and gives us the scanned QR code when the scan finishes
    private ActivityResultLauncher<ScanOptions> qrScanner =
        registerForActivityResult(new ScanContract(), result -> {
            if (result.getContents() != null) {
                String scannedValue = result.getContents();
            }
        });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //asks DeviceIdManager for the device's unique ID when the app starts, and prints to AndroidStudio's logcat
        String deviceId = DeviceIdManager.getDeviceId(this);
        Log.d("DEVICE_ID", deviceId);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        //QR scanner configuration
        ScanOptions options = new ScanOptions();
        options.setDesiredBarcodeFormats(ScanOptions.QR_CODE);
        options.setPrompt("Scan event QR code");

        //opens the QR scanner camera
        qrScanner.launch(options);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}