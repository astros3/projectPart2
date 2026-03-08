package com.example.eventlottery;

import static org.junit.Assert.*;
import org.junit.Test;

public class EntrantTest {
    @Test
    public void testEntrantCreation() {
        Entrant entrant = new Entrant("Jane Doe", "jane@example.com");
        assertEquals("Jane Doe", entrant.getName());
        assertEquals("jane@example.com", entrant.getEmail());
        assertNull(entrant.getPhoneNumber()); // Verify phone remains optional/null
    }
}