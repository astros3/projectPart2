package com.example.eventlottery;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.materialswitch.MaterialSwitch;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Calendar;
import java.util.Locale;

/**
 * Create/edit event screen (US 02.01.01, 02.01.02, 02.01.04, 01.05.05). Create: no
 * EXTRA_EVENT_ID; Edit: pass EXTRA_EVENT_ID. Enforces organizer-only; saves geolocation,
 * optional waiting list limit, lottery selection criteria, and private event flag.
 */
public class EventEditActivity extends BaseActivity {

    public static final String EXTRA_EVENT_ID = "event_id";

    private static final String PREFS_NAME = "EventLotteryPrefs";
    private static final String KEY_CURRENT_EVENT_ID = "organizer_current_event_id";
    private static final String COLLECTION_ORGANIZERS = "organizers";

    private FirebaseFirestore db;
    private String eventId;
    private String deviceId;
    private String organizerName;

    private TextInputEditText inputName, inputDescription, inputLocation,
            inputRegStart, inputRegEnd, inputEventDate, inputLimit, inputSelectionCriteria;
    private TextInputLayout inputLayoutLocation;
    private Spinner spinnerEventType;
    private MaterialSwitch switchGeolocation;
    private MaterialSwitch switchPrivateEvent; // US 02.01.02
    private MaterialButton btnConfirm;

    private boolean locationSelectedFromPlaces;
    private Double selectedPlaceLat;
    private Double selectedPlaceLng;

    private android.widget.FrameLayout eventImageContainer;
    private android.widget.ImageView posterImageView;
    private android.widget.TextView posterPlaceholderText;
    private android.widget.ImageButton btnRemovePoster;
    private android.net.Uri selectedPosterUri;
    private String existingPosterUri;
    private boolean posterRemovedByUser;

    private long registrationStartMillis = 0;
    private long registrationEndMillis = 0;
    private long eventDateMillis = 0;

    /** True while this activity holds the Firestore edit lock on the current event. */
    private boolean editLockHeld = false;
    /** 10-minute timeout for stale locks left by crashes. */
    private static final long LOCK_TIMEOUT_MS = 10 * 60 * 1000L;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_edit);

        eventId = getIntent().getStringExtra(EXTRA_EVENT_ID);
        db = FirebaseFirestore.getInstance();
        deviceId = DeviceIdManager.getDeviceId(this);
        organizerName = null;

        bindViews();
        setupToolbar();
        setupEventTypeSpinner();
        setupDatePickers();
        setupLocationAutocomplete();
        setupPosterPicker();
        setupConfirmButton();

        if (eventId != null && !eventId.isEmpty()) {
            loadEvent();
        } else {
            ensureOrganizerAccountThenAllowCreate();
        }
    }

    private void bindViews() {
        inputName              = findViewById(R.id.input_event_name);
        inputDescription       = findViewById(R.id.input_event_description);
        inputLocation          = findViewById(R.id.input_event_location);
        inputRegStart          = findViewById(R.id.input_registration_start);
        inputRegEnd            = findViewById(R.id.input_registration_end);
        inputEventDate         = findViewById(R.id.input_event_date);
        inputLimit             = findViewById(R.id.input_waiting_list_limit);
        inputSelectionCriteria = findViewById(R.id.input_selection_criteria);
        spinnerEventType       = findViewById(R.id.spinner_event_type);
        switchGeolocation      = findViewById(R.id.switch_geolocation);
        switchPrivateEvent     = findViewById(R.id.switch_private_event); // US 02.01.02
        btnConfirm             = findViewById(R.id.btn_confirm);
        eventImageContainer    = findViewById(R.id.event_image_container);
        posterImageView        = findViewById(R.id.event_poster_placeholder);
        posterPlaceholderText  = findViewById(R.id.event_image_placeholder_text);
        btnRemovePoster        = findViewById(R.id.btn_remove_poster);
        inputLayoutLocation    = findViewById(R.id.input_layout_event_location);
    }

    private void setupPosterPicker() {
        ActivityResultLauncher<String> getContent = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        selectedPosterUri = uri;
                        posterRemovedByUser = false;
                        posterImageView.setImageURI(uri);
                        posterPlaceholderText.setVisibility(android.view.View.GONE);
                        btnRemovePoster.setVisibility(android.view.View.VISIBLE);
                    }
                });
        eventImageContainer.setOnClickListener(v -> getContent.launch("image/*"));
        btnRemovePoster.setOnClickListener(v -> {
            selectedPosterUri = null;
            posterRemovedByUser = true;
            posterImageView.setImageDrawable(null);
            posterPlaceholderText.setVisibility(android.view.View.VISIBLE);
            btnRemovePoster.setVisibility(android.view.View.GONE);
        });
    }

    private void setupLocationAutocomplete() {
        String apiKey = getPlacesApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            inputLayoutLocation.setHelperText(getString(R.string.event_location_helper) + " (Places API key not set.)");
            inputLocation.setClickable(true);
            inputLocation.setOnClickListener(v ->
                    Toast.makeText(this, "Places API key required.", Toast.LENGTH_LONG).show());
            return;
        }
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        List<Place.Field> fields = Arrays.asList(Place.Field.ADDRESS, Place.Field.NAME, Place.Field.LAT_LNG);
        Intent autocompleteIntent = new Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(this);

        ActivityResultLauncher<Intent> placeAutocompleteLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) return;
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    String address = place.getAddress();
                    if (address == null || address.trim().isEmpty()) address = place.getName();
                    if (address != null && !address.trim().isEmpty()) {
                        inputLocation.setText(address.trim());
                        locationSelectedFromPlaces = true;
                        if (inputLayoutLocation != null) inputLayoutLocation.setError(null);
                    }
                    if (place.getLatLng() != null) {
                        selectedPlaceLat = place.getLatLng().latitude;
                        selectedPlaceLng = place.getLatLng().longitude;
                    }
                });

        inputLocation.setClickable(true);
        inputLocation.setOnClickListener(v -> placeAutocompleteLauncher.launch(autocompleteIntent));
    }

    private String getPlacesApiKey() {
        try {
            android.content.pm.ApplicationInfo ai = getPackageManager().getApplicationInfo(
                    getPackageName(), PackageManager.GET_META_DATA);
            if (ai != null && ai.metaData != null) {
                return ai.metaData.getString("com.google.android.geo.API_KEY");
            }
        } catch (PackageManager.NameNotFoundException ignored) {}
        return null;
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar_event_edit);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowHomeEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }
        findViewById(R.id.back_button).setOnClickListener(v -> finish());
    }

    private void setupEventTypeSpinner() {
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.event_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerEventType.setAdapter(adapter);
    }

    private void setupDatePickers() {
        inputRegStart.setOnClickListener(v -> showDatePicker(true));
        findViewById(R.id.btn_calendar_start).setOnClickListener(v -> showDatePicker(true));
        inputRegEnd.setOnClickListener(v -> showDatePicker(false));
        findViewById(R.id.btn_calendar_end).setOnClickListener(v -> showDatePicker(false));
        inputEventDate.setOnClickListener(v -> showEventDatePicker());
        findViewById(R.id.btn_calendar_event_date).setOnClickListener(v -> showEventDatePicker());
    }

    private void showEventDatePicker() {
        if (registrationEndMillis <= 0) {
            Toast.makeText(this, "Please select a Registration End date first", Toast.LENGTH_SHORT).show();
            return;
        }
        // Event Date must be strictly after Registration End
        long minDate = registrationEndMillis + 86_400_000L;
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(eventDateMillis > minDate ? eventDateMillis : minDate);
        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    cal.set(year, month, dayOfMonth, 0, 0, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    eventDateMillis = cal.getTimeInMillis();
                    inputEventDate.setText(String.format(Locale.getDefault(),
                            "%04d-%02d-%02d", year, month + 1, dayOfMonth));
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMinDate(minDate);
        dialog.show();
    }

    private void showDatePicker(boolean isStart) {
        // For Reg End, require Reg Start to be set first
        if (!isStart && registrationStartMillis <= 0) {
            Toast.makeText(this, "Please select a Registration Start date first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Determine the minimum selectable date
        long todayStart = todayMidnightMillis();
        long minDate = isStart
                ? todayStart                  // Reg Start: not before today
                : registrationStartMillis + 86_400_000L; // Reg End: day after Reg Start

        Calendar cal = Calendar.getInstance();
        if (isStart && registrationStartMillis >= minDate) {
            cal.setTimeInMillis(registrationStartMillis);
        } else if (!isStart && registrationEndMillis > registrationStartMillis) {
            cal.setTimeInMillis(registrationEndMillis);
        } else {
            cal.setTimeInMillis(minDate);
        }

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    cal.set(year, month, dayOfMonth, 0, 0, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    String formatted = String.format(Locale.getDefault(),
                            "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    if (isStart) {
                        registrationStartMillis = cal.getTimeInMillis();
                        inputRegStart.setText(formatted);
                        // If Reg End or Event Date are now invalid, clear them
                        if (registrationEndMillis > 0 && registrationEndMillis <= registrationStartMillis) {
                            registrationEndMillis = 0;
                            inputRegEnd.setText("");
                        }
                        if (eventDateMillis > 0 && registrationEndMillis <= 0) {
                            eventDateMillis = 0;
                            inputEventDate.setText("");
                        }
                    } else {
                        registrationEndMillis = cal.getTimeInMillis();
                        inputRegEnd.setText(formatted);
                        // If Event Date is now before the new Reg End, clear it
                        if (eventDateMillis > 0 && eventDateMillis < registrationEndMillis) {
                            eventDateMillis = 0;
                            inputEventDate.setText("");
                        }
                    }
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.getDatePicker().setMinDate(minDate);
        dialog.show();
    }

    /** Midnight (00:00:00.000) of today in local time, as epoch millis. */
    private long todayMidnightMillis() {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);
        return today.getTimeInMillis();
    }

    private void setupConfirmButton() {
        btnConfirm.setOnClickListener(v -> saveEvent());
    }

    @Override
    protected void onDestroy() {
        releaseEditLock();
        super.onDestroy();
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, 1001);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001 && resultCode == RESULT_OK && data != null) {
            selectedPosterUri = data.getData();
            posterImageView.setImageURI(selectedPosterUri);
        }
    }

    private void loadEvent() {
        db.collection("events").document(eventId).get()
                .addOnSuccessListener(doc -> {
                    if (!doc.exists() || doc.getData() == null) {
                        Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    Event event = doc.toObject(Event.class);
                    if (event == null) {
                        Toast.makeText(this, "Could not load event", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    event.setEventId(doc.getId());
                    // Allow primary organizer OR co-organizer to edit
                    if (!deviceId.equals(event.getOrganizerId()) && !event.isCoOrganizer(deviceId)) {
                        Toast.makeText(this, R.string.only_organizer_edit, Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    organizerName = event.getOrganizerName();
                    acquireEditLock(event);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Could not load event", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    /**
     * Acquires the Firestore edit lock on the event using a transaction.
     * If another user holds the lock (within the 10-min window), shows a toast and finishes.
     */
    private void acquireEditLock(Event event) {
        com.google.firebase.firestore.DocumentReference eventRef =
                db.collection("events").document(eventId);

        db.runTransaction(transaction -> {
            DocumentSnapshot doc = transaction.get(eventRef);
            String lockHolder = doc.getString("editLockHeldBy");
            Long lockAcquiredAt = doc.getLong("editLockAcquiredAt");
            long now = System.currentTimeMillis();

            boolean lockFree = lockHolder == null || lockHolder.isEmpty()
                    || deviceId.equals(lockHolder)
                    || (lockAcquiredAt != null && (now - lockAcquiredAt) > LOCK_TIMEOUT_MS);

            if (!lockFree) {
                throw new FirebaseFirestoreException(
                        "Event is being edited by another organizer",
                        FirebaseFirestoreException.Code.ABORTED);
            }

            Map<String, Object> lockUpdate = new HashMap<>();
            lockUpdate.put("editLockHeldBy", deviceId);
            lockUpdate.put("editLockAcquiredAt", now);
            transaction.update(eventRef, lockUpdate);
            return null;
        }).addOnSuccessListener(unused -> {
            editLockHeld = true;
            populateForm(event);
        }).addOnFailureListener(e -> {
            if (e instanceof FirebaseFirestoreException
                    && ((FirebaseFirestoreException) e).getCode()
                    == FirebaseFirestoreException.Code.ABORTED) {
                Toast.makeText(this, R.string.edit_lock_held, Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, R.string.edit_lock_acquire_failed, Toast.LENGTH_SHORT).show();
            }
            finish();
        });
    }

    /** Releases the edit lock. Safe to call even if not held. */
    private void releaseEditLock() {
        if (!editLockHeld || eventId == null) return;
        editLockHeld = false;
        Map<String, Object> release = new HashMap<>();
        release.put("editLockHeldBy", null);
        release.put("editLockAcquiredAt", 0L);
        db.collection("events").document(eventId).update(release);
    }

    private void ensureOrganizerAccountThenAllowCreate() {
        db.collection(COLLECTION_ORGANIZERS).document(deviceId).get()
                .addOnSuccessListener(this::onOrganizerDocFetched)
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Could not verify organizer account", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void onOrganizerDocFetched(DocumentSnapshot doc) {
        if (doc.exists()) {
            Organizer organizer = doc.toObject(Organizer.class);
            organizerName = organizer != null ? organizer.getFullName() : "Organizer";
            return;
        }
        new AlertDialog.Builder(this)
                .setMessage(getString(R.string.only_organizers_create) + " " + getString(R.string.register_as_organizer))
                .setNegativeButton(android.R.string.cancel, (d, w) -> finish())
                .setPositiveButton(R.string.register, (d, w) -> registerAsOrganizer())
                .setCancelable(false)
                .show();
    }

    private void registerAsOrganizer() {
        Organizer organizer = new Organizer(deviceId, "Organizer");
        db.collection(COLLECTION_ORGANIZERS).document(deviceId).set(organizer)
                .addOnSuccessListener(aVoid -> {
                    organizerName = organizer.getFullName();
                    Toast.makeText(this, "Registered as organizer", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to register as organizer", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void populateForm(@NonNull Event event) {
        inputName.setText(event.getTitle());
        inputDescription.setText(event.getDescription());
        inputLocation.setText(event.getLocation());
        locationSelectedFromPlaces = (event.getLocation() != null
                && !event.getLocation().trim().isEmpty());

        existingPosterUri = event.getPosterUri();
        if (existingPosterUri != null && !existingPosterUri.isEmpty()) {
            if (existingPosterUri.startsWith("content://")) {
                posterImageView.setImageURI(android.net.Uri.parse(existingPosterUri));
            } else {
                Glide.with(this).load(existingPosterUri).centerCrop().into(posterImageView);
            }
            posterPlaceholderText.setVisibility(android.view.View.GONE);
            btnRemovePoster.setVisibility(android.view.View.VISIBLE);
        } else {
            existingPosterUri = null;
        }

        inputLimit.setText(event.getWaitingListLimit() > 0
                ? String.valueOf(event.getWaitingListLimit()) : "");
        switchGeolocation.setChecked(event.isGeolocationRequired());

        // US 02.01.02 — load private event flag
        switchPrivateEvent.setChecked(event.isPrivate());

        registrationStartMillis = event.getRegistrationStartMillis();
        registrationEndMillis   = event.getRegistrationEndMillis();
        eventDateMillis         = event.getEventDateMillis();
        if (registrationStartMillis > 0) inputRegStart.setText(formatDateForDisplay(registrationStartMillis));
        if (registrationEndMillis > 0) inputRegEnd.setText(formatDateForDisplay(registrationEndMillis));
        if (eventDateMillis > 0) inputEventDate.setText(formatDateForDisplay(eventDateMillis));

        selectedPlaceLat = event.getLatitude();
        selectedPlaceLng = event.getLongitude();
        selectEventTypeOnSpinner(event.getEventType());

        List<String> criteria = event.getSelectionCriteria();
        if (criteria != null && !criteria.isEmpty()) {
            inputSelectionCriteria.setText(android.text.TextUtils.join("\n", criteria));
        }
    }

    private void selectEventTypeOnSpinner(String type) {
        if (type == null || type.isEmpty()) return;
        ArrayAdapter<?> adapter = (ArrayAdapter<?>) spinnerEventType.getAdapter();
        if (adapter == null) return;
        for (int i = 0; i < adapter.getCount(); i++) {
            Object item = adapter.getItem(i);
            if (item != null && type.equals(item.toString())) {
                spinnerEventType.setSelection(i);
                return;
            }
        }
    }

    private String formatDateForDisplay(long millis) {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(millis);
        return String.format(Locale.getDefault(), "%04d-%02d-%02d",
                c.get(Calendar.YEAR), c.get(Calendar.MONTH) + 1, c.get(Calendar.DAY_OF_MONTH));
    }

    private void saveEvent() {
        String title = inputName.getText() != null
                ? inputName.getText().toString().trim() : "";
        if (title.isEmpty()) {
            Toast.makeText(this, R.string.fill_required_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        if (registrationStartMillis <= 0 || registrationEndMillis <= 0) {
            Toast.makeText(this, R.string.fill_required_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        if (registrationStartMillis < todayMidnightMillis()) {
            Toast.makeText(this, "Registration Start cannot be in the past", Toast.LENGTH_SHORT).show();
            return;
        }
        if (registrationEndMillis <= registrationStartMillis) {
            Toast.makeText(this, R.string.registration_end_after_start, Toast.LENGTH_SHORT).show();
            return;
        }
        if (eventDateMillis <= 0) {
            Toast.makeText(this, "Please select an Event Date", Toast.LENGTH_SHORT).show();
            return;
        }
        if (eventDateMillis <= registrationEndMillis) {
            Toast.makeText(this, "Event Date must be after Registration End date", Toast.LENGTH_SHORT).show();
            return;
        }

        String location = inputLocation.getText() != null
                ? inputLocation.getText().toString().trim() : "";
        if (location.isEmpty() || !locationSelectedFromPlaces) {
            if (inputLayoutLocation != null) {
                inputLayoutLocation.setError(getString(R.string.event_location_required));
            }
            Toast.makeText(this, R.string.event_location_required, Toast.LENGTH_LONG).show();
            return;
        }
        if (inputLayoutLocation != null) inputLayoutLocation.setError(null);

        String description = inputDescription.getText() != null
                ? inputDescription.getText().toString().trim() : "";

        int limit = 0;
        String limitStr = inputLimit.getText() != null
                ? inputLimit.getText().toString().trim() : "";
        if (!limitStr.isEmpty()) {
            try { limit = Integer.parseInt(limitStr); }
            catch (NumberFormatException ignored) {}
        }

        // US 01.05.05 — parse selection criteria (one per line)
        List<String> criteriaList = new ArrayList<>();
        String criteriaText = inputSelectionCriteria.getText() != null
                ? inputSelectionCriteria.getText().toString().trim() : "";
        if (!criteriaText.isEmpty()) {
            for (String line : criteriaText.split("\n")) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) criteriaList.add(trimmed);
            }
        }

        boolean isCreate = eventId == null || eventId.isEmpty();
        if (isCreate && (organizerName == null || organizerName.isEmpty())) {
            Toast.makeText(this, "Organizer account not ready. Please try again.", Toast.LENGTH_SHORT).show();
            return;
        }

        Event event = new Event();
        event.setTitle(title);
        event.setDescription(description);
        event.setLocation(location);
        event.setOrganizerId(deviceId);
        event.setOrganizerName(organizerName != null ? organizerName : "Organizer");
        event.setWaitingListLimit(limit);
        event.setRegistrationStartMillis(registrationStartMillis);
        event.setRegistrationEndMillis(registrationEndMillis);
        event.setEventDateMillis(eventDateMillis);
        event.setGeolocationRequired(switchGeolocation.isChecked());
        event.setPrivate(switchPrivateEvent.isChecked()); // US 02.01.02
        event.setSelectionCriteria(criteriaList);
        event.setLatitude(selectedPlaceLat);
        event.setLongitude(selectedPlaceLng);
        if (spinnerEventType.getSelectedItem() != null) {
            event.setEventType(spinnerEventType.getSelectedItem().toString());
        }

        if (isCreate) {
            eventId = db.collection("events").document().getId();
            event.setEventId(eventId);
            // Private events have no QR code and no promo code (US 02.01.02)
            if (!event.isPrivate()) {
                event.setPromoCode(QRCodeService.generatePromoCode());
            }
        }

        if (selectedPosterUri != null) {
            uploadPosterAndThenSave(event, isCreate);
        } else {
            persistEvent(event, isCreate);
        }
    }

    private void uploadPosterAndThenSave(Event event, boolean isCreate) {
        StorageReference posterRef = FirebaseStorage.getInstance().getReference()
                .child("events").child(eventId).child("poster.jpg");
        posterRef.putFile(selectedPosterUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        return Tasks.forException(task.getException() != null
                                ? task.getException() : new Exception("Upload failed"));
                    }
                    return posterRef.getDownloadUrl();
                })
                .addOnSuccessListener(downloadUri -> {
                    event.setPosterUri(downloadUri.toString());
                    persistEvent(event, isCreate);
                })
                .addOnFailureListener(e ->
                        Toast.makeText(this, "Failed to upload poster: "
                                        + (e.getMessage() != null ? e.getMessage() : "unknown"),
                                Toast.LENGTH_LONG).show());
    }

    private void persistEvent(Event event, boolean isCreate) {
        if (isCreate) {
            // New events have no lock; ensure lock fields are blank
            event.setEditLockHeldBy(null);
            event.setEditLockAcquiredAt(0L);
            db.collection("events").document(eventId).set(event)
                    .addOnSuccessListener(aVoid -> {
                        editLockHeld = false; // no lock to release for new events
                        saveCurrentEventId(eventId);
                        Toast.makeText(this, R.string.event_created_success, Toast.LENGTH_SHORT).show();
                        // Only open QR code screen for public events
                        if (!event.isPrivate()) {
                            openQRCodeScreen();
                        }
                        finish();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Failed to create event", Toast.LENGTH_SHORT).show());
        } else {
            db.collection("events").document(eventId).get()
                    .addOnSuccessListener(doc -> {
                        if (!doc.exists()) {
                            Toast.makeText(this, "Event not found", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Event existing = doc.toObject(Event.class);
                        if (existing == null) {
                            Toast.makeText(this, "Could not load event", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        existing.setEventId(eventId);
                        existing.setTitle(event.getTitle());
                        existing.setDescription(event.getDescription());
                        existing.setLocation(event.getLocation());
                        existing.setWaitingListLimit(event.getWaitingListLimit());
                        existing.setRegistrationStartMillis(event.getRegistrationStartMillis());
                        existing.setRegistrationEndMillis(event.getRegistrationEndMillis());
                        existing.setEventDateMillis(event.getEventDateMillis());
                        existing.setGeolocationRequired(event.isGeolocationRequired());
                        existing.setPrivate(event.isPrivate()); // US 02.01.02
                        existing.setSelectionCriteria(event.getSelectionCriteria());
                        existing.setLatitude(event.getLatitude());
                        existing.setLongitude(event.getLongitude());
                        existing.setEventType(event.getEventType());
                        if (event.getPosterUri() != null) {
                            existing.setPosterUri(event.getPosterUri());
                        }
                        // Clear lock as part of save so the doc is unlocked after write
                        existing.setEditLockHeldBy(null);
                        existing.setEditLockAcquiredAt(0L);

                        db.collection("events").document(eventId).set(existing)
                                .addOnSuccessListener(aVoid -> {
                                    editLockHeld = false; // lock already cleared in set()
                                    Toast.makeText(this, R.string.event_updated_success,
                                            Toast.LENGTH_SHORT).show();
                                    finish();
                                })
                                .addOnFailureListener(e ->
                                        Toast.makeText(this, "Failed to update event",
                                                Toast.LENGTH_SHORT).show());
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Could not load event", Toast.LENGTH_SHORT).show());
        }
    }

    private void openQRCodeScreen() {
        startActivity(QRCodeActivity.newIntent(this, eventId));
    }

    private void saveCurrentEventId(String id) {
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_CURRENT_EVENT_ID, id)
                .apply();
    }

    public static String getCurrentEventId(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .getString(KEY_CURRENT_EVENT_ID, null);
    }

    public static void setCurrentEventId(Context context, String eventId) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
                .edit()
                .putString(KEY_CURRENT_EVENT_ID, eventId)
                .apply();
    }

    public static Intent newIntent(Context context, String eventId) {
        Intent i = new Intent(context, EventEditActivity.class);
        i.putExtra(EXTRA_EVENT_ID, eventId);
        return i;
    }
}