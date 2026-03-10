package com.example.eventlottery;


/*
Android uses something called "SharedPreferences" that lets apps store small data locally.
 */

import android.content.Context;
import android.content.SharedPreferences;

import java.util.UUID;

public class DeviceIdManager {
    //name of the storage file where we will save the device ID
    private static final String STORAGE_NAME = "EventLotteryDeviceStorage";
    private static final String DEVICE_ID_KEY = "device_id";

    // returns the device's unique ID, creating one if it does not already exist
    public static String getDeviceId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
        String deviceId = prefs.getString(DEVICE_ID_KEY, null);

        //generating and storing a unique device ID if it doesn't exist yet
        if (deviceId == null) {
            deviceId = UUID.randomUUID().toString();
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(DEVICE_ID_KEY, deviceId);
            editor.apply();
        }
        return deviceId;
    }
}
