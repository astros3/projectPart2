package com.example.eventlottery;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;

import java.util.UUID;

/**
 * Provides a persistent device ID used as identity for both entrant and organizer flows.
 * Singleton-like: one ID per app install.
 */
public class DeviceIdManager {

    private DeviceIdManager() {}

    private static final String STORAGE_NAME = "EventLotteryDeviceStorage";
    private static final String DEVICE_ID_KEY = "device_id";

    // Known bad ANDROID_ID returned by some emulators/unconfigured devices
    private static final String BAD_ANDROID_ID = "9774d56d682e549c";

    /**
     * Returns the device's unique ID.
     * Prefers Settings.Secure.ANDROID_ID (the real hardware-based ID). Falls back to a
     * randomly generated UUID (stored persistently) if ANDROID_ID is unavailable or invalid.
     *
     * @param context any Context used to read Settings.Secure and SharedPreferences
     * @return a non-null, non-empty unique device identifier string
     */
    public static String getDeviceId(Context context) {
        String androidId = Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ANDROID_ID);

        if (androidId != null && !androidId.isEmpty() && !androidId.equals(BAD_ANDROID_ID)) {
            return androidId;
        }

        // Fallback: persist a random UUID for devices that don't expose a valid ANDROID_ID
        SharedPreferences prefs = context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
        String deviceId = prefs.getString(DEVICE_ID_KEY, null);
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            prefs.edit().putString(DEVICE_ID_KEY, deviceId).apply();
        }
        return deviceId;
    }
}
