package com.example.eventlottery;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Allows the primary organizer of an event to assign or remove co-organizers.
 * Co-organizers get full organizer privileges for the event and are removed
 * from the waiting list when assigned.
 * Only accessible by the primary event organizer (organizerId == deviceId).
 */
public class CoOrganizerManagementActivity extends BaseActivity {

    public static final String EXTRA_EVENT_ID = "co_org_event_id";

    private static final String TAG = "CoOrgMgmt";

    private FirebaseFirestore db;
    private String eventId;
    /** Cached from event document for assignment notifications. */
    private String eventTitle = "";
    private String deviceId;

    private RecyclerView recyclerView;
    private TextView textNoUsers;
    private TextInputEditText inputSearch;

    private UserListAdapter adapter;

    private final List<UserItem> allUsers = new ArrayList<>();
    private final List<UserItem> filteredUsers = new ArrayList<>();
    private final List<String> currentCoOrganizerIds = new ArrayList<>();

    public static Intent newIntent(Context context, String eventId) {
        Intent i = new Intent(context, CoOrganizerManagementActivity.class);
        i.putExtra(EXTRA_EVENT_ID, eventId);
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_co_organizer_management);

        eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "No event selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();
        deviceId = DeviceIdManager.getDeviceId(this);

        setupToolbar();
        bindViews();
        loadCoOrganizersAndUsers();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_co_organizer);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void bindViews() {
        recyclerView = findViewById(R.id.recycler_users);
        textNoUsers = findViewById(R.id.text_no_users);
        inputSearch = findViewById(R.id.input_search_users);

        adapter = new UserListAdapter(filteredUsers, this::onAssignRemoveClicked);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s != null ? s.toString() : "");
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadCoOrganizersAndUsers() {
        // First load event to get current co-organizer list
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(eventDoc -> {
                    if (!eventDoc.exists()) {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    Event event = eventDoc.toObject(Event.class);
                    if (event == null) {
                        Toast.makeText(this, "Could not load event", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    // Only primary organizer can manage co-organizers
                    if (!deviceId.equals(event.getOrganizerId())) {
                        Toast.makeText(this, "Only the event organizer can manage co-organizers",
                                Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    String t = event.getTitle();
                    eventTitle = (t != null && !t.isEmpty()) ? t : "";
                    currentCoOrganizerIds.clear();
                    currentCoOrganizerIds.addAll(event.getCoOrganizerIds());
                    loadAllUsers();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load event", e);
                    Toast.makeText(this, "Could not load event", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void loadAllUsers() {
        db.collection("users").get()
                .addOnSuccessListener(snapshot -> {
                    allUsers.clear();
                    for (QueryDocumentSnapshot doc : snapshot) {
                        String uid = doc.getId();
                        // Exclude the event organizer (current user) from the list
                        if (deviceId.equals(uid)) continue;

                        String fullName = doc.getString("fullName");
                        String email = doc.getString("email");
                        boolean isCoOrg = currentCoOrganizerIds.contains(uid);

                        allUsers.add(new UserItem(uid,
                                fullName != null ? fullName : "Unknown User",
                                email != null ? email : "",
                                isCoOrg));
                    }
                    filterUsers("");
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to load users", e);
                    Toast.makeText(this, "Could not load users", Toast.LENGTH_SHORT).show();
                });
    }

    private void filterUsers(String query) {
        filteredUsers.clear();
        String lower = query.toLowerCase().trim();
        for (UserItem user : allUsers) {
            if (lower.isEmpty()
                    || user.fullName.toLowerCase().contains(lower)
                    || user.email.toLowerCase().contains(lower)) {
                filteredUsers.add(user);
            }
        }
        adapter.notifyDataSetChanged();
        textNoUsers.setVisibility(filteredUsers.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(filteredUsers.isEmpty() ? View.GONE : View.VISIBLE);
    }

    private void onAssignRemoveClicked(UserItem user) {
        if (user.isCoOrganizer) {
            removeCoOrganizer(user);
        } else {
            assignCoOrganizer(user);
        }
    }

    private void assignCoOrganizer(UserItem user) {
        // Add to coOrganizerIds on the event document
        db.collection("events").document(eventId)
                .update("coOrganizerIds", FieldValue.arrayUnion(user.deviceId))
                .addOnSuccessListener(unused -> {
                    // Remove from waiting list if present
                    db.collection("events").document(eventId)
                            .collection("waitingList").document(user.deviceId)
                            .delete()
                            .addOnSuccessListener(v -> Log.d(TAG, "Removed from waiting list: " + user.deviceId))
                            .addOnFailureListener(e -> Log.d(TAG, "Not on waiting list or already removed: " + user.deviceId));

                    String displayEventName = eventTitle.isEmpty()
                            ? getString(R.string.co_organizer_notification_event_fallback)
                            : eventTitle;
                    String notifTitle = getString(R.string.co_organizer_notification_title);
                    String notifBody = getString(R.string.co_organizer_notification_body, displayEventName);
                    NotificationHelper.sendCoOrganizerAssignedNotification(db, user.deviceId, eventId,
                            notifTitle, notifBody);

                    user.isCoOrganizer = true;
                    currentCoOrganizerIds.add(user.deviceId);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, getString(R.string.co_organizer_assigned), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to assign co-organizer", e);
                    Toast.makeText(this, getString(R.string.co_organizer_assign_failed), Toast.LENGTH_SHORT).show();
                });
    }

    private void removeCoOrganizer(UserItem user) {
        db.collection("events").document(eventId)
                .update("coOrganizerIds", FieldValue.arrayRemove(user.deviceId))
                .addOnSuccessListener(unused -> {
                    user.isCoOrganizer = false;
                    currentCoOrganizerIds.remove(user.deviceId);
                    adapter.notifyDataSetChanged();
                    Toast.makeText(this, getString(R.string.co_organizer_removed), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to remove co-organizer", e);
                    Toast.makeText(this, getString(R.string.co_organizer_remove_failed), Toast.LENGTH_SHORT).show();
                });
    }

    // ─── Data model ────────────────────────────────────────────────────────────

    static class UserItem {
        final String deviceId;
        final String fullName;
        final String email;
        boolean isCoOrganizer;

        UserItem(String deviceId, String fullName, String email, boolean isCoOrganizer) {
            this.deviceId = deviceId;
            this.fullName = fullName;
            this.email = email;
            this.isCoOrganizer = isCoOrganizer;
        }
    }

    // ─── RecyclerView Adapter ──────────────────────────────────────────────────

    interface OnUserActionListener {
        void onAction(UserItem user);
    }

    static class UserListAdapter extends RecyclerView.Adapter<UserListAdapter.UserViewHolder> {

        private final List<UserItem> items;
        private final OnUserActionListener listener;

        UserListAdapter(List<UserItem> items, OnUserActionListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_co_organizer_user, parent, false);
            return new UserViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
            holder.bind(items.get(position), listener);
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class UserViewHolder extends RecyclerView.ViewHolder {
            private final TextView nameView;
            private final TextView emailView;
            private final TextView badgeView;
            private final MaterialButton btnAssignRemove;

            UserViewHolder(@NonNull View itemView) {
                super(itemView);
                nameView = itemView.findViewById(R.id.user_name);
                emailView = itemView.findViewById(R.id.user_email);
                badgeView = itemView.findViewById(R.id.user_co_organizer_badge);
                btnAssignRemove = itemView.findViewById(R.id.btn_assign_remove);
            }

            void bind(UserItem user, OnUserActionListener listener) {
                nameView.setText(user.fullName);
                emailView.setText(user.email.isEmpty() ? "No email" : user.email);

                if (user.isCoOrganizer) {
                    badgeView.setVisibility(View.VISIBLE);
                    btnAssignRemove.setText(R.string.remove_co_organizer);
                } else {
                    badgeView.setVisibility(View.GONE);
                    btnAssignRemove.setText(R.string.assign_co_organizer);
                }

                btnAssignRemove.setOnClickListener(v -> {
                    if (listener != null) listener.onAction(user);
                });
            }
        }
    }
}
