package com.example.eventlottery;

import java.util.ArrayList;
import java.util.Collections;

public class EntrantListManager {
    private static EntrantListManager instance;

    private final ArrayList<String> waitingList = new ArrayList<>();
    private final ArrayList<String> selectedList = new ArrayList<>();
    // New list for those not picked during the draw
    private final ArrayList<String> rejectedList = new ArrayList<>();
    private final ArrayList<String> acceptedList = new ArrayList<>();

    private EntrantListManager() {
        waitingList.add("Entrant 1");
        waitingList.add("Entrant 2");
        waitingList.add("Entrant 3");
        waitingList.add("Entrant 4");
        waitingList.add("Entrant 5");
    }

    public static EntrantListManager getInstance() {
        if (instance == null) {
            instance = new EntrantListManager();
        }
        return instance;
    }

    public ArrayList<String> getWaitingList() { return waitingList; }
    public ArrayList<String> getSelectedList() { return selectedList; }
    public ArrayList<String> getRejectedList() { return rejectedList; }
    public ArrayList<String> getAcceptedList() { return acceptedList; }


    public void requestToJoinAgain(String entrantName) {
        if (rejectedList.contains(entrantName)) {
            rejectedList.remove(entrantName);
            waitingList.add(entrantName);
        }
    }


    public void declineInvitation(String entrantName) {
            selectedList.remove(entrantName);
    }

    public void acceptInvitation(String entrantName) {
        if (selectedList.contains(entrantName)) {
            selectedList.remove(entrantName);
            acceptedList.add(entrantName);
        }
    }

    public ArrayList<String> drawEntrants(int count) {
        ArrayList<String> shuffledList = new ArrayList<>(waitingList);
        Collections.shuffle(shuffledList);

        // Ensure we don't out-of-bounds if count > waiting list size
        int actualCount = Math.min(count, shuffledList.size());
        ArrayList<String> selected = new ArrayList<>(shuffledList.subList(0, actualCount));

        selectedList.addAll(selected);
        waitingList.removeAll(selected);

        // Everyone left in waitingList after a draw becomes "Rejected"
        rejectedList.addAll(waitingList);
        waitingList.clear();

        return selected;
    }
}