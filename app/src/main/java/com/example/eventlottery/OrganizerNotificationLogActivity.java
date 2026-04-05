package com.example.eventlottery;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

/**
 * Shows all notifications sent (automatically by lottery/invite, or manually) for a specific event.
 * Reads from the notificationStorageAdmin top-level collection filtered by eventId.
 * Each row shows: notification title, message, who it was sent to (entrant name + device ID),
 * and the date + time it was sent.
 */
public class OrganizerNotificationLogActivity extends BaseActivity {

    /** No-arg constructor required by the Android Activity lifecycle. */
    public OrganizerNotificationLogActivity() {}

    /** Intent extra key for the event ID passed to this activity. */
    public static final String EXTRA_EVENT_ID = "event_id";

    private static final class LogEntry {
        final String title;
        final String message;
        final String receiverId;
        final long timestampMillis;
        String recipientName; // resolved async

        LogEntry(String title, String message, String receiverId, long timestampMillis) {
            this.title = title != null ? title : "";
            this.message = message != null ? message : "";
            this.receiverId = receiverId != null ? receiverId : "";
            this.timestampMillis = timestampMillis;
            this.recipientName = "Loading..."; // resolved async
        }
    }

    private static final class LogAdapter extends ArrayAdapter<LogEntry> {
        LogAdapter(Context ctx, ArrayList<LogEntry> items) {
            super(ctx, 0, items);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.item_organizer_notif_log, parent, false);
            }
            LogEntry e = getItem(position);
            if (e == null) return convertView;

            ((TextView) convertView.findViewById(R.id.text_log_title)).setText(e.title);
            ((TextView) convertView.findViewById(R.id.text_log_message)).setText(e.message);
            ((TextView) convertView.findViewById(R.id.text_log_recipient))
                    .setText("Sent to: " + e.recipientName);

            DateFormat fmt = DateFormat.getDateTimeInstance(
                    DateFormat.MEDIUM, DateFormat.SHORT, Locale.getDefault());
            ((TextView) convertView.findViewById(R.id.text_log_time))
                    .setText(fmt.format(new Date(e.timestampMillis)));

            return convertView;
        }
    }

    private FirebaseFirestore db;
    private String eventId;
    private ListView listView;
    private TextView emptyView;
    private LogAdapter adapter;
    private final ArrayList<LogEntry> items = new ArrayList<>();

    /**
     * Creates an Intent that opens this activity for the specified event.
     *
     * @param context context used to build the intent
     * @param eventId Firestore ID of the event whose notification log to display
     * @return configured Intent ready to start OrganizerNotificationLogActivity
     */
    public static Intent newIntent(Context context, String eventId) {
        Intent i = new Intent(context, OrganizerNotificationLogActivity.class);
        i.putExtra(EXTRA_EVENT_ID, eventId);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_notification_log);

        eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        db = FirebaseFirestore.getInstance();

        findViewById(R.id.btn_back_org_notif_log).setOnClickListener(v -> finish());

        listView = findViewById(R.id.list_org_notif_log);
        emptyView = findViewById(R.id.text_empty_org_notif_log);

        adapter = new LogAdapter(this, items);
        listView.setAdapter(adapter);

        loadLog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadLog();
    }

    private void loadLog() {
        if (eventId == null || eventId.isEmpty()) {
            showEmpty(true);
            return;
        }

        db.collection("notificationStorageAdmin")
                .whereEqualTo("eventId", eventId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    items.clear();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String title    = doc.getString("title");
                        String message  = doc.getString("message");
                        String receiver = doc.getString("receiverID");
                        Long ts         = doc.getLong("timestampMillis");
                        items.add(new LogEntry(title, message, receiver, ts != null ? ts : 0L));
                    }
                    // Sort newest-first in memory (avoids needing a composite Firestore index)
                    items.sort((a, b) -> Long.compare(b.timestampMillis, a.timestampMillis));
                    adapter.notifyDataSetChanged();
                    showEmpty(items.isEmpty());
                    // Resolve recipient names asynchronously
                    resolveRecipientNames();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load notification log", Toast.LENGTH_SHORT).show());
    }

    /**
     * For each log entry look up the entrant's name from users/{receiverId}.
     * Updates the adapter as each name arrives so the list refreshes without a full reload.
     */
    private void resolveRecipientNames() {
        for (int i = 0; i < items.size(); i++) {
            final int idx = i;
            LogEntry entry = items.get(idx);
            if (entry.receiverId.isEmpty()) continue;

            db.collection("users").document(entry.receiverId).get()
                    .addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            String name = doc.getString("fullName");
                            if (name == null || name.trim().isEmpty()) {
                                name = doc.getString("name");
                            }
                            if (name != null && !name.trim().isEmpty()) {
                                entry.recipientName = name;
                            }
                        }
                        adapter.notifyDataSetChanged();
                    });
        }
    }

    private void showEmpty(boolean empty) {
        emptyView.setVisibility(empty ? View.VISIBLE : View.GONE);
        listView.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}
