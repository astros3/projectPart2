package com.example.eventlottery;

import static org.junit.Assert.*;
import org.junit.Test;

public class EntrantTest {
    @Test
    public void testEntrantDataIntegrity() {
        Entrant entrant = new Entrant("Donald", "Trump", "donald@gmail.com", "123");
        assertEquals("Donald Trump", entrant.getFullName());
        assertEquals("123", entrant.getPhone());
    }

}