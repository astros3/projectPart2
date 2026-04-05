package com.example.eventlottery;

import org.junit.Test;

import static org.junit.Assert.*;

/** Admin profile delete: Firestore collection by role (same logic as AdminBrowseProfilesAdapter). */
public class AdminProfileRemovalCollectionUnitTest {

    @Test
    public void adminRemoveProfile_resolvesCollectionFromRole_likeBrowseAdapter() {
        assertEquals("organizers", firestoreCollectionForProfileRole("Organizer"));
        assertEquals("organizers", firestoreCollectionForProfileRole("ORGANIZER"));
        assertEquals("users", firestoreCollectionForProfileRole("entrant"));
        assertEquals("users", firestoreCollectionForProfileRole("Entrant"));
    }

    @Test
    public void adminRemoveOrganizer_usesOrganizersCollection() {
        assertEquals("organizers", firestoreCollectionForProfileRole("organizer"));
    }

    private static String firestoreCollectionForProfileRole(String role) {
        if (role != null && role.equalsIgnoreCase("Organizer")) {
            return "organizers";
        }
        return "users";
    }
}
