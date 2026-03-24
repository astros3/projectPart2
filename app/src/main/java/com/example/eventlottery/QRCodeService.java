package com.example.eventlottery;

import android.graphics.Bitmap;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

import java.util.HashMap;
import java.util.Map;

/**
 * Generates QR code bitmaps and human-readable promo codes for events. Encoded format
 * is event ID (or "eventlottery://event/{id}") so scanning opens event details (US 02.01.01).
 */
public class QRCodeService {

    private static final int QR_SIZE_PX = 512;

    /**
     * Generates a QR code bitmap for the given event ID.
     * The encoded content is the eventId so scanners can open the event details.
     *
     * @param eventId The event ID to encode (used when scanning).
     * @return Bitmap of the QR code, or null if generation failed.
     */
    public static Bitmap generateQrCodeBitmap(String eventId) {
        if (eventId == null || eventId.isEmpty()) {
            return null;
        }

        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        QRCodeWriter writer = new QRCodeWriter();
        try {
            BitMatrix bitMatrix = writer.encode(eventId, BarcodeFormat.QR_CODE, QR_SIZE_PX, QR_SIZE_PX, hints);
            return bitMatrixToBitmap(bitMatrix);
        } catch (WriterException e) {
            return null;
        }
    }

    /**
     * Generates a human-readable promo code (e.g. "555 555") for manual entry.
     * Format: 3 digits, space, 3 digits.
     */
    public static String generatePromoCode() {
        int part1 = (int) (Math.random() * 900) + 100; // 100-999
        int part2 = (int) (Math.random() * 900) + 100;
        return part1 + " " + part2;
    }

    private static Bitmap bitMatrixToBitmap(BitMatrix bitMatrix) {
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        int[] pixels = new int[width * height];

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixels[y * width + x] = bitMatrix.get(x, y) ? 0xFF000000 : 0xFFFFFFFF;
            }
        }

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
        return bitmap;
    }
}
