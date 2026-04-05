package com.example.eventlottery;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Private event invite: search entrants by name, email, or phone ).
 */
public class OrganizerPrivateInviteSearchFilterUnitTest {

    @Test
    public void inviteSearch_emptyQuery_includesAll() {
        Entrant e = entrant("dev1", "Pat Lee", "pat@x.com", "7805550100");
        assertTrue(matchesInviteSearch(e, ""));
        assertTrue(matchesInviteSearch(e, "   "));
    }

    @Test
    public void inviteSearch_matchesFullNameSubstring() {
        Entrant e = entrant("d", "Jordan Smith", "j@j.com", "");
        assertTrue(matchesInviteSearch(e, "smith"));
        assertTrue(matchesInviteSearch(e, "JORDAN"));
        assertFalse(matchesInviteSearch(e, "lee"));
    }

    @Test
    public void inviteSearch_matchesEmailOrPhone() {
        Entrant e = entrant("d", "A", "sam@school.edu", "4035550199");
        assertTrue(matchesInviteSearch(e, "school.edu"));
        assertTrue(matchesInviteSearch(e, "403555"));
        assertFalse(matchesInviteSearch(e, "780"));
    }

    private static Entrant entrant(String id, String name, String email, String phone) {
        Entrant e = new Entrant(id, name, email, phone, "entrant");
        return e;
    }

    private static boolean matchesInviteSearch(Entrant e, String query) {
        String q = query.toLowerCase().trim();
        return q.isEmpty()
                || e.getFullName().toLowerCase().contains(q)
                || e.getEmail().toLowerCase().contains(q)
                || e.getPhone().toLowerCase().contains(q);
    }
}
