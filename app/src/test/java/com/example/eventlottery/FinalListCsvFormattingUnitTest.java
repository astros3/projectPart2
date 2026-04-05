package com.example.eventlottery;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Final list CSV export escaping and columns (FinalList.escapeCsv).
 */
public class FinalListCsvFormattingUnitTest {

    @Test
    public void csvHeaderRow_matchesExportColumnOrder() {
        String header = FinalList.escapeCsv("Name") + ","
                + FinalList.escapeCsv("Email") + ","
                + FinalList.escapeCsv("Phone") + ","
                + FinalList.escapeCsv("Status") + ","
                + FinalList.escapeCsv("Join Date");
        assertEquals("Name,Email,Phone,Status,Join Date", header);
    }

    @Test
    public void csvOneDataRow_matchesWriteAndShareCsvShape() {
        String line = FinalList.escapeCsv("Alice") + ","
                + FinalList.escapeCsv("a@b.com") + ","
                + FinalList.escapeCsv("555") + ","
                + FinalList.escapeCsv("ACCEPTED") + ","
                + FinalList.escapeCsv("2026-04-04 12:00");
        assertEquals("Alice,a@b.com,555,ACCEPTED,2026-04-04 12:00", line);
    }

    @Test
    public void escapeCsv_nullBecomesEmptyQuoted() {
        assertEquals("\"\"", FinalList.escapeCsv(null));
    }

    @Test
    public void escapeCsv_commasQuotesAndNewlinesEscaped() {
        assertEquals("\"Smith, Jr.\"", FinalList.escapeCsv("Smith, Jr."));
        assertEquals("\"Say \"\"hi\"\"\"", FinalList.escapeCsv("Say \"hi\""));
        assertEquals("\"line1\nline2\"", FinalList.escapeCsv("line1\nline2"));
    }

    @Test
    public void escapeCsv_plainValueUnquoted() {
        assertEquals("Alice", FinalList.escapeCsv("Alice"));
    }
}
