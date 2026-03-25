package com.example.eventlottery;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.QueryDocumentSnapshot;

/**
 * Maps Firestore event documents to {@link Event} (shared by list + map screens).
 */
public final class EventFirestoreParser {

    private EventFirestoreParser() {}

    public static Event fromSnapshot(QueryDocumentSnapshot eachevent) {
        return fromSnapshot((DocumentSnapshot) eachevent);
    }

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
        return event;
    }
}
