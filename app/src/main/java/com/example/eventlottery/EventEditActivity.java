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
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Calendar;
import java.util.Locale;

/**
 * Create/edit event screen (US 02.01.01, 02.01.04, 01.05.05). Create: no EXTRA_EVENT_ID;
 * Edit: pass EXTRA_EVENT_ID. Enforces organizer-only; saves geolocation, optional waiting
 * list limit, and lottery selection criteria (US 01.05.05).
 */
public class EventEditActivity extends AppCompatActivity {

    public static final String EXTRA_EVENT_ID = "event_id";

    private static final String PREFS_NAME = "EventLotteryPrefs";
    private static final String KEY_CURRENT_EVENT_ID = "organizer_current_event_id";
    /** Firestore collection for organizer accounts (separate from entrant users). */
    private static final String COLLECTION_ORGANIZERS = "organizers";

    private FirebaseFirestore db;
    private String eventId; // null = create mode
    private String deviceId;
    private String organizerName;

    private TextInputEditText inputName, inputDescription, inputLocation,
            inputRegStart, inputRegEnd, inputLimit, inputSelectionCriteria;
    private TextInputLayout inputLayoutLocation;
    private Spinner spinnerEventType;
    private MaterialSwitch switchGeolocation;
    private MaterialButton btnConfirm;

    /** True when location was set via Google Places Autocomplete (required to save). */
    private boolean locationSelectedFromPlaces;
    /** Last place coordinates from Places (persisted on save for entrant map). */
    private Double selectedPlaceLat;
    private Double selectedPlaceLng;

    private android.widget.FrameLayout eventImageContainer;
    private android.widget.ImageView posterImageView;
    private android.net.Uri selectedPosterUri;

    private long registrationStartMillis = 0;
    private long registrationEndMillis = 0;

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
        inputLimit             = findViewById(R.id.input_waiting_list_limit);
        inputSelectionCriteria = findViewById(R.id.input_selection_criteria); // US 01.05.05
        spinnerEventType       = findViewById(R.id.spinner_event_type);
        switchGeolocation      = findViewById(R.id.switch_geolocation);
        btnConfirm             = findViewById(R.id.btn_confirm);
        eventImageContainer    = findViewById(R.id.event_image_container);
        posterImageView        = findViewById(R.id.event_poster_placeholder);
        inputLayoutLocation    = findViewById(R.id.input_layout_event_location);
    }

    /**
     * Initializes Places (if API key is present) and makes the location field open
     * Google Places Autocomplete. Only locations selected from the search are accepted.
     */
    private void setupLocationAutocomplete() {
        String apiKey = getPlacesApiKey();
        if (apiKey == null || apiKey.isEmpty()) {
            inputLayoutLocation.setHelperText(getString(R.string.event_location_helper) + " (Places API key not set.)");
            inputLocation.setClickable(true);
            inputLocation.setOnClickListener(v ->
                    Toast.makeText(this, "Places API key required. Set com.google.android.geo.API_KEY in AndroidManifest.", Toast.LENGTH_LONG).show());
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
                    if (result.getResultCode() != Activity.RESULT_OK || result.getData() == null) {
                        return;
                    }
                    Place place = Autocomplete.getPlaceFromIntent(result.getData());
                    String address = place.getAddress();
                    if (address == null || address.trim().isEmpty()) {
                        address = place.getName();
                    }
                    if (address != null && !address.trim().isEmpty()) {
                        inputLocation.setText(address.trim());
                        locationSelectedFromPlaces = true;
                        if (inputLayoutLocation != null) {
                            inputLayoutLocation.setError(null);
                        }
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
        } catch (PackageManager.NameNotFoundException ignored) {
        }
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
    }

    private void showDatePicker(boolean isStart) {
        Calendar cal = Calendar.getInstance();
        if (isStart && registrationStartMillis > 0) cal.setTimeInMillis(registrationStartMillis);
        else if (!isStart && registrationEndMillis > 0) cal.setTimeInMillis(registrationEndMillis);

        DatePickerDialog dialog = new DatePickerDialog(this,
                (view, year, month, dayOfMonth) -> {
                    cal.set(year, month, dayOfMonth, 0, 0, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    String formatted = String.format(Locale.getDefault(),
                            "%04d-%02d-%02d", year, month + 1, dayOfMonth);
                    if (isStart) {
                        registrationStartMillis = cal.getTimeInMillis();
                        inputRegStart.setText(formatted);
                    } else {
                        registrationEndMillis = cal.getTimeInMillis();
                        inputRegEnd.setText(formatted);
                    }
                },
                cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        dialog.show();
    }

    private void setupConfirmButton() {
        btnConfirm.setOnClickListener(v -> saveEvent());
        eventImageContainer.setOnClickListener(v -> openImagePicker());
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
                    if (!deviceId.equals(event.getOrganizerId())) {
                        Toast.makeText(this, R.string.only_organizer_edit, Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }
                    organizerName = event.getOrganizerName();
                    populateForm(event);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Could not load event", Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    /**
     * Create mode: ensure current device has an organizer account; if not, offer to register.
     */
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

        // Load existing poster when editing
        if (event.getPosterUri() != null && !event.getPosterUri().isEmpty()) {
            String uri = event.getPosterUri();
            if (uri.startsWith("content://")) {
                selectedPosterUri = android.net.Uri.parse(uri);
                posterImageView.setImageURI(selectedPosterUri);
            } else {
                Glide.with(this).load(uri).centerCrop().into(posterImageView);
            }
        }

        inputLimit.setText(event.getWaitingListLimit() > 0
                ? String.valueOf(event.getWaitingListLimit()) : "");
        switchGeolocation.setChecked(event.isGeolocationRequired());

        registrationStartMillis = event.getRegistrationStartMillis();
        registrationEndMillis   = event.getRegistrationEndMillis();
        if (registrationStartMillis > 0) {
            inputRegStart.setText(formatDateForDisplay(registrationStartMillis));
        }
        if (registrationEndMillis > 0) {
            inputRegEnd.setText(formatDateForDisplay(registrationEndMillis));
        }

        selectedPlaceLat = event.getLatitude();
        selectedPlaceLng = event.getLongitude();
        selectEventTypeOnSpinner(event.getEventType());

        // US 01.05.05 — load existing selection criteria into the field
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
        if (registrationEndMillis <= registrationStartMillis) {
            Toast.makeText(this, R.string.registration_end_after_start, Toast.LENGTH_SHORT).show();
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
        if (inputLayoutLocation != null) {
            inputLayoutLocation.setError(null);
        }

        String description = inputDescription.getText() != null
                ? inputDescription.getText().toString().trim() : "";

        int limit = 0;
        String limitStr = inputLimit.getText() != null
                ? inputLimit.getText().toString().trim() : "";
        if (!limitStr.isEmpty()) {
            try {
                limit = Integer.parseInt(limitStr);
            } catch (NumberFormatException ignored) { }
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
        event.setEventDateMillis(registrationEndMillis);
        event.setGeolocationRequired(switchGeolocation.isChecked());
        event.setSelectionCriteria(criteriaList); // US 01.05.05
        event.setLatitude(selectedPlaceLat);
        event.setLongitude(selectedPlaceLng);
        if (spinnerEventType.getSelectedItem() != null) {
            event.setEventType(spinnerEventType.getSelectedItem().toString());
        }

        if (isCreate) {
            eventId = db.collection("events").document().getId();
            event.setEventId(eventId);
            event.setPromoCode(QRCodeService.generatePromoCode());
        }

        if (selectedPosterUri != null) {
            uploadPosterAndThenSave(event, isCreate);
        } else {
            persistEvent(event, isCreate);
        }
    }

    /** Uploads selected image to Storage at events/{eventId}/poster, then persists event. */
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
            db.collection("events").document(eventId).set(event)
                    .addOnSuccessListener(aVoid -> {
                        saveCurrentEventId(eventId);
                        Toast.makeText(this, R.string.event_created_success, Toast.LENGTH_SHORT).show();
                        openQRCodeScreen();
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
                        existing.setSelectionCriteria(event.getSelectionCriteria()); // US 01.05.05
                        existing.setLatitude(event.getLatitude());
                        existing.setLongitude(event.getLongitude());
                        existing.setEventType(event.getEventType());
                        if (event.getPosterUri() != null) {
                            existing.setPosterUri(event.getPosterUri());
                        }

                        db.collection("events").document(eventId).set(existing)
                                .addOnSuccessListener(aVoid -> {
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

    /** Call when organizer selects an event (e.g. from dashboard) so QR / nav use it. */
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