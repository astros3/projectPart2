package com.example.eventlottery;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;

/**
 * Shows in-app notifications from Firestore.
 * Tapping a row opens EventDetailsActivity for the related event and marks the row read.
 */
public class EntrantNotificationsActivity extends BaseActivity {

    /** No-arg constructor required by the Android Activity lifecycle. */
    public EntrantNotificationsActivity() {}

    private FirebaseFirestore db;
    private String deviceId;
    private ListView listView;
    private TextView emptyView;
    private EntrantNotificationsAdapter adapter;
    private ArrayList<InAppNotification> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_entrant_notifications);

        db = FirebaseFirestore.getInstance();
        deviceId = DeviceIdManager.getDeviceId(this);

        listView = findViewById(R.id.list_notifications);
        emptyView = findViewById(R.id.text_empty_notifications);
        findViewById(R.id.btn_back_notifications).setOnClickListener(v -> finish());

        items = new ArrayList<>();
        adapter = new EntrantNotificationsAdapter(this, items);
        listView.setAdapter(adapter);

        listView.setOnItemClickListener((parent, view, position, id) -> {
            InAppNotification n = items.get(position);
            if (n.getId() == null || n.getId().isEmpty()) {
                return;
            }
            db.collection("users").document(deviceId)
                    .collection("notifications").document(n.getId())
                    .update("read", true);

            String eventId = n.getEventId();
            if (eventId != null && !eventId.isEmpty()) {
                Intent intent = new Intent(this, EventDetailsActivity.class);
                intent.putExtra(EventDetailsActivity.EXTRA_EVENT_ID, eventId);
                startActivity(intent);
            }
        });

        loadNotifications();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadNotifications();
    }

    private void loadNotifications() {
        if (deviceId == null || deviceId.isEmpty()) {
            showEmpty(true);
            return;
        }

        db.collection("users").document(deviceId).collection("notifications")
                .get()
                .addOnSuccessListener(snapshot -> {
                    items.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        items.add(InAppNotification.fromSnapshot(doc));
                    }
                    Collections.sort(items, (a, b) ->
                            Long.compare(b.getTimestampMillis(), a.getTimestampMillis()));
                    adapter.notifyDataSetChanged();
                    showEmpty(items.isEmpty());
                })
                .addOnFailureListener(e -> {
                    items.clear();
                    adapter.notifyDataSetChanged();
                    showEmpty(true);
                    Toast.makeText(this, R.string.notification_load_failed, Toast.LENGTH_SHORT).show();
                });
    }

    private void showEmpty(boolean empty) {
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        listView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}
