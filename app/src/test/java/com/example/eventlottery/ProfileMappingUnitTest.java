package com.example.eventlottery;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class ProfileMappingUnitTest {

    @Test
    public void testOrganizerToEntrantMapping() {
        // Create an organizer profile
        Organizer organizer = new Organizer("org_123", "Jane", "Doe", "jane@example.com", "123456");

        // Map to Entrant format used in the Admin list
        Entrant mappedEntrant = new Entrant(
                organizer.getOrganizerId(),
                organizer.getFullName(),
                organizer.getEmail(),
                organizer.getPhoneNumber(),
                "Organizer"
        );

        // Assert values are preserved
        assertEquals("Jane Doe", mappedEntrant.getFullName());
        assertEquals("Organizer", mappedEntrant.getRole());
        assertEquals("org_123", mappedEntrant.getDeviceID());
    }

    @Test
    public void testDefaultRoleAssignment() {
        // Verify organizer default role is set correctly
        Organizer organizer = new Organizer();
        assertEquals("organizer", organizer.getRole());
    }
}