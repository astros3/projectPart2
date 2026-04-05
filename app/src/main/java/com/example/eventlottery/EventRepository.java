package com.example.eventlottery;

import java.util.Date;

/**
 * Singleton holding a "current" event for organizer flows. Not used elsewhere; current event
 * is tracked via EventEditActivity SharedPreferences instead.
 * Issue: Constructor uses null dummyOrganizer (NPE if getInstance() is ever called).
 */
public class EventRepository {

    private static EventRepository instance;
    private Event currentEvent;

    private EventRepository() {
        Organizer dummyOrganizer = null;

        currentEvent = new Event(
                "event1",
                "Sample Event",
                "Sample Description",
                "Edmonton",
                dummyOrganizer.getOrganizerId(),
                dummyOrganizer.getDisplayName(),
                5,
                0,
                System.currentTimeMillis() - 100000,
                System.currentTimeMillis() + 100000000,
                System.currentTimeMillis() + 200000000,
                true,
                0.0
        );
    }

    /**
     * Returns the singleton instance of EventRepository, creating it if necessary.
     * @return the single EventRepository instance
     */
    public static EventRepository getInstance() {
        if (instance == null) {
            instance = new EventRepository();
        }
        return instance;
    }

    /**
     * Returns the currently held event.
     * @return the current Event
     */
    public Event getCurrentEvent() {
        return currentEvent;
    }

    /**
     * Replaces the currently held event.
     * @param currentEvent the new Event to store
     */
    public void setCurrentEvent(Event currentEvent) {
        this.currentEvent = currentEvent;
    }
}