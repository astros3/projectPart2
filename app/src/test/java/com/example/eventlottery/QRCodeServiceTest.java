package com.example.eventlottery;

import static org.junit.Assert.*;

import android.graphics.Bitmap;

import org.junit.Test;

public class QRCodeServiceTest {

    //This verifies the promo code generator works.
    @Test
    public void generatePromoCode_returnsValidCode() {
        String promoCode = QRCodeService.generatePromoCode();

        assertNotNull(promoCode);
        assertTrue(promoCode.length() > 0);
    }
}