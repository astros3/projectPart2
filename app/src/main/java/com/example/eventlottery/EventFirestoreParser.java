package com.example.eventlottery;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

/**
 * Maps Firestore event documents to Event (shared by list + map screens).
 */
public final class EventFirestoreParser {

    private EventFirestoreParser() {}

    /**
     * Parses a QueryDocumentSnapshot into an Event.
     *
     * @param eachevent Firestore query document snapshot from the events collection
     * @return a populated Event model, or a partially-filled one on missing fields
     */
    public static Event fromSnapshot(QueryDocumentSnapshot eachevent) {
        return fromSnapshot((DocumentSnapshot) eachevent);
    }

    /**
     * Parses a DocumentSnapshot into an Event.
     *
     * @param eachevent Firestore document snapshot from the events collection
     * @return a populated Event model, or a partially-filled one on missing fields
     */
    public static Event fromSnapshot(DocumentSnapshot eachevent) {
        String id = eachevent.getId();
        String thecurrenteventtitle = eachevent.getString("title");
        String thecurrenteventlocation = eachevent.getString("location");
        String thecurrenteventposteruri = eachevent.getString("posterUri");
        String thecurrenteventorganizername = eachevent.getString("organizerName");
        Long thecurrenteventdateandtime = eachevent.getLong("eventDateMillis");

        String thecurrenteventdescription = eachevent.getString("description");
        String thecurrenteventorganizerid = eachevent.getString("organizerId");
        Long capLong = eachevent.getLong("capacity");
        Long waitLong = eachevent.getLong("waitingListLimit");
        int thecurrenteventcapacity = capLong != null ? capLong.intValue() : 0;
        int thecurrentwaitinglistlimit = waitLong != null ? waitLong.intValue() : 0;
        Long theregistrationstart = eachevent.getLong("registrationStartMillis");
        Long theregistrationend = eachevent.getLong("registrationEndMillis");
        Boolean geoReq = eachevent.getBoolean("geolocationRequired");
        boolean thecurrentgeolocationrequired = geoReq != null && geoReq;
        Double thecurrentprice = eachevent.getDouble("price");

        long dateMillis = thecurrenteventdateandtime != null ? thecurrenteventdateandtime : 0L;
        long regStart = theregistrationstart != null ? theregistrationstart : 0L;
        long regEnd = theregistrationend != null ? theregistrationend : 0L;
        double price = thecurrentprice != null ? thecurrentprice : 0.0;

        Event event = new Event(id, thecurrenteventtitle, thecurrenteventdescription, thecurrenteventlocation,
                thecurrenteventorganizerid, thecurrenteventorganizername, thecurrenteventcapacity,
                thecurrentwaitinglistlimit, regStart, regEnd, dateMillis, thecurrentgeolocationrequired, price);
        event.setPosterUri(thecurrenteventposteruri);
        event.setEventType(eachevent.getString("eventType"));
        event.setLatitude(eachevent.getDouble("latitude"));
        event.setLongitude(eachevent.getDouble("longitude"));
        // Backward compatibility: older events may not have the private flag in Firestore.
        // Treat missing flag as false (public event).
        Boolean isPrivate = eachevent.getBoolean("private");
        if (isPrivate == null) {
            // Defensive fallback if any older data used a different key.
            isPrivate = eachevent.getBoolean("isPrivate");
        }
        event.setPrivate(Boolean.TRUE.equals(isPrivate));

        // Co-organizer and edit lock fields (new events may not have these).
        @SuppressWarnings("unchecked")
        List<String> coOrganizerIds = (List<String>) eachevent.get("coOrganizerIds");
        event.setCoOrganizerIds(coOrganizerIds != null ? coOrganizerIds : new ArrayList<>());
        event.setEditLockHeldBy(eachevent.getString("editLockHeldBy"));
        Long lockAt = eachevent.getLong("editLockAcquiredAt");
        event.setEditLockAcquiredAt(lockAt != null ? lockAt : 0L);

        return event;
    }
}
