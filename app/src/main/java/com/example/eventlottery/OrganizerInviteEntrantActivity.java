package com.example.eventlottery;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Lets an organizer search the users collection by name, phone, or email and invite
 * specific entrants to a private event's waiting list (US 02.01.03, 01.05.06).
 *
 * Entrant is written to events/{eventId}/waitingList/{deviceId} with status INVITED,
 * and a private-invite in-app notification is sent (respects entrant opt-out).
 */
public class OrganizerInviteEntrantActivity extends BaseActivity {

    /** No-arg constructor required by the Android Activity lifecycle. */
    public OrganizerInviteEntrantActivity() {}

    /** Intent extra key for the event ID passed to this activity. */
    public static final String EXTRA_EVENT_ID = "event_id";

    private FirebaseFirestore db;
    private String eventId;

    private TextInputEditText inputSearch;
    private TextView emptyView;
    private RecyclerView recyclerView;

    private final List<Entrant> allEntrants = new ArrayList<>();
    private final List<Entrant> filteredEntrants = new ArrayList<>();
    private InviteAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_organizer_invite_entrant);

        eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        if (eventId == null || eventId.isEmpty()) {
            Toast.makeText(this, "No event selected", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        db = FirebaseFirestore.getInstance();

        setupToolbar();
        bindViews();
        loadEntrants();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_invite);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        findViewById(R.id.back_button_invite).setOnClickListener(v -> finish());
    }

    private void bindViews() {
        inputSearch = findViewById(R.id.input_search_entrant);
        emptyView   = findViewById(R.id.empty_invite_view);
        recyclerView = findViewById(R.id.recycler_invite_entrants);

        adapter = new InviteAdapter(filteredEntrants, this::onInviteClicked);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        inputSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterEntrants(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadEntrants() {
        db.collection("users").get()
                .addOnSuccessListener(querySnapshot -> {
                    allEntrants.clear();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        Entrant e = doc.toObject(Entrant.class);
                        if (e.getDeviceID() == null) e.setDeviceID(doc.getId());
                        allEntrants.add(e);
                    }
                    filterEntrants(inputSearch.getText() != null
                            ? inputSearch.getText().toString() : "");
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to load entrants", Toast.LENGTH_SHORT).show());
    }

    private void filterEntrants(String query) {
        String q = query.toLowerCase().trim();
        filteredEntrants.clear();
        for (Entrant e : allEntrants) {
            if (q.isEmpty()
                    || e.getFullName().toLowerCase().contains(q)
                    || e.getEmail().toLowerCase().contains(q)
                    || e.getPhone().toLowerCase().contains(q)) {
                filteredEntrants.add(e);
            }
        }
        adapter.notifyDataSetChanged();
        emptyView.setVisibility(filteredEntrants.isEmpty() ? View.VISIBLE : View.GONE);
        recyclerView.setVisibility(filteredEntrants.isEmpty() ? View.GONE : View.VISIBLE);
    }

    /** Invite button tapped: check for duplicate then write INVITED entry + send notification. */
    private void onInviteClicked(Entrant entrant) {
        String targetDeviceId = entrant.getDeviceID();
        if (targetDeviceId == null || targetDeviceId.isEmpty()) {
            Toast.makeText(this, "Cannot invite: entrant has no device ID", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("events").document(eventId)
                .collection("waitingList").document(targetDeviceId)
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        String currentStatus = doc.getString("status");
                        Toast.makeText(this,
                                entrant.getFullName() + " is already on the waiting list ("
                                        + (currentStatus != null ? currentStatus : "unknown") + ")",
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    writeInviteAndNotify(entrant, targetDeviceId);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to check waiting list", Toast.LENGTH_SHORT).show());
    }

    private void writeInviteAndNotify(Entrant entrant, String targetDeviceId) {
        WaitingListEntry entry = new WaitingListEntry(targetDeviceId, WaitingListEntry.Status.INVITED);
        entry.setInvitationSentMillis(System.currentTimeMillis());
        db.collection("events").document(eventId)
                .collection("waitingList").document(targetDeviceId)
                .set(entry)
                .addOnSuccessListener(unused -> {
                    NotificationHelper.sendPrivateInviteNotification(db, targetDeviceId, eventId);
                    Toast.makeText(this,
                            "Invited " + entrant.getFullName(), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to send invite", Toast.LENGTH_SHORT).show());
    }

    /**
     * Creates an Intent that opens this activity for the specified event.
     *
     * @param context context used to build the intent
     * @param eventId Firestore ID of the private event to invite entrants to
     * @return configured Intent ready to start OrganizerInviteEntrantActivity
     */
    public static Intent newIntent(Context context, String eventId) {
        Intent i = new Intent(context, OrganizerInviteEntrantActivity.class);
        i.putExtra(EXTRA_EVENT_ID, eventId);
        return i;
    }

    // -------------------------------------------------------------------------
    // Inner adapter
    // -------------------------------------------------------------------------

    interface OnInviteClickListener {
        void onInvite(Entrant entrant);
    }

    static class InviteAdapter extends RecyclerView.Adapter<InviteAdapter.VH> {

        private final List<Entrant> items;
        private final OnInviteClickListener listener;

        InviteAdapter(List<Entrant> items, OnInviteClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_invite_entrant, parent, false);
            return new VH(v);
        }

        @Override
        public void onBindViewHolder(@NonNull VH holder, int position) {
            Entrant e = items.get(position);
            holder.nameView.setText(e.getFullName());

            String email = e.getEmail();
            String phone = e.getPhone();
            StringBuilder sub = new StringBuilder();
            if (!email.isEmpty()) sub.append(email);
            if (!phone.isEmpty()) {
                if (sub.length() > 0) sub.append("  ·  ");
                sub.append(phone);
            }
            holder.subView.setText(sub.length() > 0 ? sub.toString() : "No contact info");

            holder.btnInvite.setOnClickListener(v -> listener.onInvite(e));
        }

        @Override
        public int getItemCount() { return items.size(); }

        static class VH extends RecyclerView.ViewHolder {
            TextView nameView, subView;
            MaterialButton btnInvite;
            VH(@NonNull View itemView) {
                super(itemView);
                nameView  = itemView.findViewById(R.id.text_entrant_name);
                subView   = itemView.findViewById(R.id.text_entrant_sub);
                btnInvite = itemView.findViewById(R.id.btn_invite_entrant);
            }
        }
    }
}
