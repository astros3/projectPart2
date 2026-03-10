package com.example.projectpart_3;

import java.util.ArrayList;
import java.util.Collections;

public class EntrantListManager {

    private static EntrantListManager instance;

    private final ArrayList<String> waitingList = new ArrayList<>();
    private final ArrayList<String> selectedList = new ArrayList<>();

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

    public ArrayList<String> getWaitingList() {
        return waitingList;
    }

    public ArrayList<String> getSelectedList() {
        return selectedList;
    }

    public void setSelectedList(ArrayList<String> selected) {
        selectedList.clear();
        selectedList.addAll(selected);
    }

    public ArrayList<String> drawEntrants(int count) {
        ArrayList<String> shuffledList = new ArrayList<>(waitingList);
        Collections.shuffle(shuffledList);

        ArrayList<String> selected = new ArrayList<>(shuffledList.subList(0, count));

        selectedList.clear();
        selectedList.addAll(selected);

        waitingList.removeAll(selected);

        return selected;
    }
}