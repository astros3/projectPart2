package com.example.eventlottery;

import android.content.Context;

import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

/**
 * Single Firestore listener on users/{deviceId}/notifications so organizer-sent notifications
 * (via NotificationHelper) also post as high-importance OS alerts whenever the app process
 * is alive — not only while EntrantMainScreenActivity is on screen.
 */
public final class EntrantNotificationBridge {

    private static final Object LOCK = new Object();
    private static ListenerRegistration registration;
    private static long attachTimeMillis;

    private EntrantNotificationBridge() {}

    /**
     * Verifies the entrant profile exists and notifications are enabled before registering the listener.
     * Safe to call at app startup; the Firestore check is performed asynchronously.
     *
     * @param appContext application context used to access Firestore and DeviceIdManager
     */
    public static void tryRegisterAfterEntrantCheck(Context appContext) {
        Context ctx = appContext.getApplicationContext();
        String deviceId = DeviceIdManager.getDeviceId(ctx);
        FirebaseFirestore.getInstance()
                .collection("users")
                .document(deviceId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc == null || !doc.exists()) {
                        return;
                    }
                    String role = doc.getString("role");
                    if (role == null || !"entrant".equalsIgnoreCase(role.trim())) {
                        return;
                    }
                    Entrant entrant = doc.toObject(Entrant.class);
                    if (entrant != null && !entrant.isNotificationsEnabled()) {
                        return;
                    }
                    ensureRegistered(ctx, deviceId);
                });
    }

    /**
     * Attaches the Firestore notification listener if it is not already active.
     * Idempotent — safe to call repeatedly (e.g. from onResume of the entrant home screen).
     *
     * @param context any Context; the application context is extracted internally
     */
    public static void ensureRegistered(Context context) {
        Context ctx = context.getApplicationContext();
        ensureRegistered(ctx, DeviceIdManager.getDeviceId(ctx));
    }

    private static void ensureRegistered(Context appContext, String deviceId) {
        synchronized (LOCK) {
            if (registration != null) {
                return;
            }
            attachTimeMillis = System.currentTimeMillis();
            FirebaseFirestore db = FirebaseFirestore.getInstance();
            registration = db.collection("users")
                    .document(deviceId)
                    .collection("notifications")
                    .whereEqualTo("read", false)
                    .addSnapshotListener((snapshots, error) -> {
                        if (error != null || snapshots == null) {
                            return;
                        }
                        for (DocumentChange change : snapshots.getDocumentChanges()) {
                            if (change.getType() != DocumentChange.Type.ADDED) {
                                continue;
                            }
                            DocumentSnapshot doc = change.getDocument();
                            Long ts = doc.getLong("timestampMillis");
                            if (ts == null || ts < attachTimeMillis) {
                                continue;
                            }
                            String title = doc.getString("title");
                            String message = doc.getString("message");
                            if (title == null) {
                                title = appContext.getString(R.string.app_name);
                            }
                            if (message == null) {
                                message = "";
                            }
                            MyFirebaseMessagingService.postNotification(
                                    appContext,
                                    title,
                                    message,
                                    doc.getId().hashCode());
                        }
                    });
        }
    }
}
