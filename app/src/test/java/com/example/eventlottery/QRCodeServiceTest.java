package com.example.eventlottery;

import android.graphics.Bitmap;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link QRCodeService}.
 */
public class QRCodeServiceTest {

    /** Promo code is shown in UI and used for manual entry; must never be null or empty. */
    @Test
    public void generatePromoCode_returnsNonEmpty() {
        String code = QRCodeService.generatePromoCode();
        assertNotNull(code);
        assertFalse(code.isEmpty());
    }

    /** Documented format is "ddd ddd" (e.g. "123 456"); manual entry relies on it. */
    @Test
    public void generatePromoCode_matchesFormatThreeDigitsSpaceThreeDigits() {
        for (int i = 0; i < 20; i++) {
            String code = QRCodeService.generatePromoCode();
            assertTrue("Expected format 'ddd ddd', got: " + code,
                    code.matches("\\d{3} \\d{3}"));
        }
    }

    /** Implementation uses 100–999 per part; ensure no off-by-one or wrong range. */
    @Test
    public void generatePromoCode_eachPartInRange100To999() {
        for (int i = 0; i < 30; i++) {
            String code = QRCodeService.generatePromoCode();
            String[] parts = code.split(" ");
            assertEquals(2, parts.length);
            int a = Integer.parseInt(parts[0]);
            int b = Integer.parseInt(parts[1]);
            assertTrue("First part in [100,999]: " + a, a >= 100 && a <= 999);
            assertTrue("Second part in [100,999]: " + b, b >= 100 && b <= 999);
        }
    }

    /** Defensive: null input must not crash; caller (e.g. QR screen) can handle null. */
    @Test
    public void generateQrCodeBitmap_nullInput_returnsNull() {
        Bitmap b = QRCodeService.generateQrCodeBitmap(null);
        assertNull(b);
    }

    /** Empty string is invalid; service should return null rather than generate garbage. */
    @Test
    public void generateQrCodeBitmap_emptyString_returnsNull() {
        Bitmap b = QRCodeService.generateQrCodeBitmap("");
        assertNull(b);
    }

    /** Valid event ID must produce a displayable QR image for the organizer screen. */
    @Test
    public void generateQrCodeBitmap_validInput_returnsNonNullBitmap() {
        Bitmap b = QRCodeService.generateQrCodeBitmap("evt_abc123");
        assertNotNull(b);
        assertTrue(b.getWidth() > 0);
        assertTrue(b.getHeight() > 0);
    }

    // --- QR scanner result parsing: same logic as MainActivity / EntrantMainScreenActivity ---
    // When the app's scanner returns a string, it trims and takes substring after last "/" if any.
    // This helper mirrors that logic so we can test the event ID extraction without changing app code.

    private static String extractEventIdFromScannerResult(String scannerContents) {
        if (scannerContents == null) return null;
        String scannedValue = scannerContents.trim();
        if (scannedValue.contains("/")) {
            scannedValue = scannedValue.substring(scannedValue.lastIndexOf("/") + 1);
        }
        return scannedValue;
    }

    /** App encodes as "eventlottery://event/{id}"; scanner returns that string; we extract id after last "/". */
    @Test
    public void scannerResult_fullUri_extractsEventIdAfterLastSlash() {
        String scannerResult = "eventlottery://event/evt_abc123";
        assertEquals("evt_abc123", extractEventIdFromScannerResult(scannerResult));
    }

    /** If user scans a QR that contains only the raw event ID, we use it as-is. */
    @Test
    public void scannerResult_plainEventId_returnsSameId() {
        assertEquals("evt_abc123", extractEventIdFromScannerResult("evt_abc123"));
    }

    /** Scanner callback checks result.getContents() != null; test mirrors null handling. */
    @Test
    public void scannerResult_null_returnsNull() {
        assertNull(extractEventIdFromScannerResult(null));
    }

    /** Edge case: URI with no path after last "/" yields empty string (no crash). */
    @Test
    public void scannerResult_uriWithTrailingSlash_returnsEmpty() {
        assertEquals("", extractEventIdFromScannerResult("eventlottery://event/"));
    }

    /** Same logic as MainActivity: substring after last "/" works for any URI-style or deep link. */
    @Test
    public void scannerResult_multipleSlashes_takesAfterLastSlash() {
        assertEquals("evt_xyz", extractEventIdFromScannerResult("https://example.com/events/evt_xyz"));
        assertEquals("evt_xyz", extractEventIdFromScannerResult("eventlottery://event/evt_xyz"));
    }

    /** Scanner may return content with leading/trailing space; app trims before parsing. */
    @Test
    public void scannerResult_trimsWhitespace() {
        assertEquals("evt_abc123", extractEventIdFromScannerResult("  eventlottery://event/evt_abc123  "));
    }

    /** End-to-end: QRCodeActivity encodes eventId this way; scan result must parse back to same id. */
    @Test
    public void scannerResult_encodedByQRCodeActivity_format_extractsEventId() {
        String eventId = TestData.EVENT_ID_1;
        String qrContent = "eventlottery://event/" + eventId;
        assertEquals(eventId, extractEventIdFromScannerResult(qrContent));
    }
}
