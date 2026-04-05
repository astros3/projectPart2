package com.example.eventlottery;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

/**
 * Public event promotional QR / promo code (QR screen and QRCodeService).
 * Bitmap generation is not run here — JVM unit tests use stub android.graphics.Bitmap;
 * encoding is verified with ZXing BitMatrix only (same hints/size as QRCodeService).
 */
public class OrganizerEventMarketingAndPrivacyUnitTest {

    private static final int QR_SIZE_PX = 512;

    private static BitMatrix encodePayloadToBitMatrix(String payload) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        return new QRCodeWriter().encode(payload, BarcodeFormat.QR_CODE, QR_SIZE_PX, QR_SIZE_PX, hints);
    }

    private static String extractEventIdFromScannerResult(String scannerContents) {
        if (scannerContents == null) return null;
        String scannedValue = scannerContents.trim();
        if (scannedValue.contains("/")) {
            scannedValue = scannedValue.substring(scannedValue.lastIndexOf("/") + 1);
        }
        return scannedValue;
    }

    @Test
    public void promotionalQr_payloadUsesActivityBuilderAndParsesBackToEventId() {
        String id = "evt_promo_99";
        String payload = QRCodeActivity.buildPromotionalQrPayload(id);
        assertTrue(payload.startsWith(QRCodeActivity.PROMOTIONAL_QR_URI_PREFIX));
        assertEquals(id, extractEventIdFromScannerResult(payload));
    }

    @Test
    public void promotionalQr_payloadEncodesWithZxing_sameSettingsAsService() throws Exception {
        String payload = QRCodeActivity.buildPromotionalQrPayload("unique_event_abc123");
        BitMatrix matrix = encodePayloadToBitMatrix(payload);
        assertNotNull(matrix);
        assertEquals(QR_SIZE_PX, matrix.getWidth());
        assertEquals(QR_SIZE_PX, matrix.getHeight());
    }

    @Test
    public void publicEvent_distinctPayloads_bothEncodeToQrMatrices() throws Exception {
        BitMatrix a = encodePayloadToBitMatrix(QRCodeActivity.buildPromotionalQrPayload("event_one"));
        BitMatrix b = encodePayloadToBitMatrix(QRCodeActivity.buildPromotionalQrPayload("event_two"));
        assertNotNull(a);
        assertNotNull(b);
        assertTrue(a.getWidth() > 0 && b.getWidth() > 0);
    }

    @Test
    public void promotionalPromoCode_matchesDigitTripleSpaceDigitTriple() {
        String code = QRCodeService.generatePromoCode();
        assertTrue(code.matches("\\d{3} \\d{3}"));
    }
}
