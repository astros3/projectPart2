package com.example.eventlottery;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Final list PDF export truncation helper (FinalList.truncate).
 */
public class FinalListPdfFormattingUnitTest {

    @Test
    public void truncate_shortString_returnedUnchanged() {
        assertEquals("Alice", FinalList.truncate("Alice", 22));
    }

    @Test
    public void truncate_longString_shortenedWithEllipsis() {
        String result = FinalList.truncate("AVeryLongNameThatExceedsColumnWidth", 22);
        assertEquals(22, result.length());
        assertTrue(result.endsWith(".."));
    }

    @Test
    public void truncate_nullInput_returnsEmptyString() {
        assertEquals("", FinalList.truncate(null, 22));
    }

    @Test
    public void truncate_exactLengthString_returnedUnchanged() {
        String s = "ExactlyTwentyTwoChar!!"; // 22 chars
        assertEquals(s, FinalList.truncate(s, 22));
    }

    @Test
    public void truncate_plainValue_noEllipsis() {
        assertEquals("Bob", FinalList.truncate("Bob", 10));
    }
}
