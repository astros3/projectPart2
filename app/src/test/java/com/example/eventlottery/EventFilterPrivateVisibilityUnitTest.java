package com.example.eventlottery;

import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Private event visibility on entrant list vs map (EventFilterUtils).
 */
public class EventFilterPrivateVisibilityUnitTest {

    @Test
    public void entrantList_publicEvent_visibleWithoutWaitingListEntry() {
        Event e = new Event();
        e.setEventId("pub1");
        e.setPrivate(false);
        assertTrue(EventFilterUtils.passesEntrantPrivateListVisibility(e, Collections.emptySet()));
    }

    @Test
    public void entrantList_privateEvent_hiddenWithoutWaitingListEntry() {
        Event e = new Event();
        e.setEventId("priv1");
        e.setPrivate(true);
        assertFalse(EventFilterUtils.passesEntrantPrivateListVisibility(e, Collections.emptySet()));
    }

    @Test
    public void entrantList_privateEvent_visibleWhenUserHasWaitingListEntry() {
        Event e = new Event();
        e.setEventId("priv1");
        e.setPrivate(true);
        Set<String> ids = new HashSet<>();
        ids.add("priv1");
        assertTrue(EventFilterUtils.passesEntrantPrivateListVisibility(e, ids));
    }

    @Test
    public void entrantMap_privateEvent_neverShown() {
        Event e = new Event();
        e.setEventId("priv1");
        e.setPrivate(true);
        assertFalse(EventFilterUtils.showEventOnEntrantMap(e));
    }

    @Test
    public void entrantMap_publicEvent_shown() {
        Event e = new Event();
        e.setPrivate(false);
        assertTrue(EventFilterUtils.showEventOnEntrantMap(e));
    }

    @Test
    public void entrantList_nullEvent_notVisible() {
        assertFalse(EventFilterUtils.passesEntrantPrivateListVisibility(null, Collections.emptySet()));
    }
}
