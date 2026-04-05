package com.example.eventlottery;

import org.junit.Test;

import static org.junit.Assert.*;

/** Admin browse profiles list row model (AdminProfileItemTemporary). */
public class AdminBrowseProfilesItemUnitTest {

    @Test
    public void adminBrowseProfilesItem_holdsIdNameAndType() {
        AdminProfileItemTemporary organizerRow =
                new AdminProfileItemTemporary("org_doc_1", "Pat Organizer", "organizer");
        assertEquals("org_doc_1", organizerRow.getId());
        assertEquals("Pat Organizer", organizerRow.getProfileName());
        assertEquals("organizer", organizerRow.getType());

        AdminProfileItemTemporary entrantRow =
                new AdminProfileItemTemporary("dev_entrant_2", "Sam Entrant", "entrant");
        assertEquals("entrant", entrantRow.getType());
    }
}
