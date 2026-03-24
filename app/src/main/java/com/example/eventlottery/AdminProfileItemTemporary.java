package com.example.eventlottery;

/**
 * temporarily store profile information
 * stores either a organizer profile or entrant profile
 * organizers the id stores the organizer id
 * entrants the id stores the device id
 */
//US 03.02.01
//US 03.07.01
public class AdminProfileItemTemporary {
    //declearation
    private String id; //for organizer it will be storing the organizer id, for entrant it will be storing the device id
    private String name;
    private String isitorganzierorentrantinput;

    /**
     * constructor
     * @param idinput id of the profile
     * @param nameinput name of the profile
     * @param isitorganzierorentrantinput either organizer or entrant
     */
    public AdminProfileItemTemporary(String idinput, String nameinput, String isitorganzierorentrantinput) {//constructor
        this.id = idinput;
        this.name = nameinput;
        this.isitorganzierorentrantinput = isitorganzierorentrantinput;
    }


    /**
     * getter
     * @return profile name
     */
    public String getProfileName(){
        return name;
    }

    /**
     * getter
     * @return profile type
     */
    public String getType() {//either "organizer" or "entrant"
        return isitorganzierorentrantinput;
    }
    /**
     * getter
     * @return profile id
     */
    public String getId(){
        return this.id;
    }

}
