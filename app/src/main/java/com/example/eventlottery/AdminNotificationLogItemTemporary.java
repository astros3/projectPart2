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
    public AdminNotificationLogItemTemporary(long notificationsenttimeinput,String notificationtitleinput,String notificationmessageinput, String notificationrelatedeventidinput, String notificationsendernameinput, String notificationrelatedeventnameinput,String notificationidinput) {
        this.notificationsenttime = notificationsenttimeinput;
        this.notificationtitle = notificationtitleinput;
        this.notificationmessage = notificationmessageinput;
        this.notificationrelatedeventid = notificationrelatedeventidinput;
        this.notificationid = notificationidinput;
        this.notificationsendername = notificationsendernameinput;
        this.notificationrelatedeventname = notificationrelatedeventnameinput
        ;
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

}