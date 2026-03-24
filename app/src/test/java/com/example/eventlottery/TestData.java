package com.example.eventlottery;

/**
 * Realistic test data for unit tests. All timestamps and IDs are fixed so tests are deterministic
 * and don't depend on "current time" or random values.
 */
public final class TestData {

    private TestData() {}

    /** Fixed "now" for registration-window logic: 12 March 2026 00:00:00 UTC (epoch millis). */
    public static final long NOW_MS = 1773360000000L;  // 2026-03-12
    /** One hour in ms; used to build registration start/end around NOW_MS. */
    public static final long HOUR_MS = 3600_000L;

    /** Sample event and organizer IDs / details used across Event, Organizer, and related tests. */
    public static final String EVENT_ID_1 = "evt_abc123";
    public static final String EVENT_TITLE = "Community Swim Meet";
    public static final String EVENT_DESCRIPTION = "Annual swimming event at Central Pool.";
    public static final String EVENT_LOCATION = "Edmonton, AB";
    public static final String ORGANIZER_ID = "org_device_xyz";
    public static final String ORGANIZER_NAME = "Jane Smith";
    public static final int CAPACITY = 50;
    public static final int WAITING_LIST_LIMIT = 20;
    public static final double PRICE = 0.0;

    /** Sample entrant (user) profile data for Entrant and WaitingListEntry tests. */
    public static final String ENTRANT_DEVICE_ID = "usr_device_001";
    public static final String ENTRANT_NAME = "Alex Johnson";
    public static final String ENTRANT_EMAIL = "alex.j@example.com";
    public static final String ENTRANT_PHONE = "+1-780-555-0100";

    /** Registration open: start before NOW, end after NOW. */
    public static long regStartOpen() { return NOW_MS - HOUR_MS; }
    public static long regEndOpen()   { return NOW_MS + HOUR_MS; }
    /** Registration closed: end before NOW. */
    public static long regStartClosed() { return NOW_MS - 2 * HOUR_MS; }
    public static long regEndClosed()   { return NOW_MS - HOUR_MS; }
}
