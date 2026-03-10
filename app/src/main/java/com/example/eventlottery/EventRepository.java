package com.example.eventlottery;

import java.util.Date;

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
                dummyOrganizer,
                5,
                0,
                new Date(System.currentTimeMillis() - 100000),
                new Date(System.currentTimeMillis() + 100000000),
                new Date(System.currentTimeMillis() + 200000000),
                true,
                0.0
        );
    }

    public static EventRepository getInstance() {
        if (instance == null) {
            instance = new EventRepository();
        }
        return instance;
    }

    public Event getCurrentEvent() {
        return currentEvent;
    }

    public void setCurrentEvent(Event currentEvent) {
        this.currentEvent = currentEvent;
    }
}