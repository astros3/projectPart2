package com.example.eventlottery;

/**
 * Class is used to temporarily store admin notification item
 * records all the notification information
 * title, message, sender, related event, send time
 * used when displaying admin notification log information screen
 */
//US 03.08.01
public class AdminNotificationLogItemTemporary {

    //declearation
    private long notificationsenttime;
    private String notificationtitle;
    private String notificationmessage;
    private String notificationrelatedeventid;
    private String notificationsendername;
    private String notificationrelatedeventname;
    private String notificationid;
    private String notificationreceivername;

    /**
     * constructor
     *
     * @param notificationsenttimeinput time when the notification was sent
     * @param notificationtitleinput title of the notification
     * @param notificationmessageinput message of the notification
     * @param notificationrelatedeventidinput id of the related event
     * @param notificationsendernameinput name of the sender
     * @param notificationrelatedeventnameinput name of the related event
     * @param notificationidinput id of the notification
     */
    public AdminNotificationLogItemTemporary(long notificationsenttimeinput, String notificationtitleinput, String notificationmessageinput, String notificationrelatedeventidinput, String notificationsendernameinput, String notificationrelatedeventnameinput, String notificationidinput) {
        this(notificationsenttimeinput, notificationtitleinput, notificationmessageinput, notificationrelatedeventidinput, notificationsendernameinput, notificationrelatedeventnameinput, notificationidinput, "");
    }

    /**
     * Full constructor including receiver name (resolved asynchronously after construction).
     *
     * @param notificationsenttimeinput       time (ms) when the notification was sent
     * @param notificationtitleinput          title of the notification
     * @param notificationmessageinput        body message of the notification
     * @param notificationrelatedeventidinput Firestore ID of the related event
     * @param notificationsendernameinput     display name of the organizer who sent the notification
     * @param notificationrelatedeventnameinput display name of the related event
     * @param notificationidinput             Firestore document ID of the notification
     * @param notificationreceivernameinput   display name of the entrant who received the notification
     */
    public AdminNotificationLogItemTemporary(long notificationsenttimeinput, String notificationtitleinput, String notificationmessageinput, String notificationrelatedeventidinput, String notificationsendernameinput, String notificationrelatedeventnameinput, String notificationidinput, String notificationreceivernameinput) {
        this.notificationsenttime = notificationsenttimeinput;
        this.notificationtitle = notificationtitleinput;
        this.notificationmessage = notificationmessageinput;
        this.notificationrelatedeventid = notificationrelatedeventidinput;
        this.notificationid = notificationidinput;
        this.notificationsendername = notificationsendernameinput;
        this.notificationrelatedeventname = notificationrelatedeventnameinput;
        this.notificationreceivername = notificationreceivernameinput != null ? notificationreceivernameinput : "";
    }

    /**
     * getter
     * @return the sent time of the notification
     */
    public long getTime(){
        return this.notificationsenttime;
    }
    /**
     * getter
     * @return the notification title
     */
    public String getTitle(){
        return this.notificationtitle;
    }

    /**
     * getter
     * @return the notification message
     */
    public String getMessage(){
        return this.notificationmessage;
    }
    /**
     * getter
     * @return the linked event id
     */
    public String getEventid(){
        return this.notificationrelatedeventid;
    }
    /**
     * getter
     * @return the sender name
     */
    public String getSendername(){
        return this.notificationsendername;
    }

    /**
     * getter
     * @return the linked event name
     */
    public String getEventname(){
        return this.notificationrelatedeventname;
    }

    /**
     * getter
     * @return the notification id
     */
    public String getNotificationID(){
        return this.notificationid;
    }

    /**
     * getter
     * @return the receiver (entrant) name
     */
    public String getReceiverName(){
        return this.notificationreceivername;
    }

    /**
     * Setter for receiver name (resolved async after construction).
     *
     * @param name display name of the entrant who received the notification
     */
    public void setReceiverName(String name){
        this.notificationreceivername = name != null ? name : "";
    }

}