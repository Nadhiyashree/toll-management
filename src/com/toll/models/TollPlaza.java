package com.toll.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TollPlaza {
    private String id;
    private String name;
    private String highwayName;
    private String location;
    private final List<Lane> lanes;
    private boolean operational;

    public TollPlaza(String id, String name, String highwayName, String location) {
        this.id = id;
        this.name = name;
        this.highwayName = highwayName;
        this.location = location;
        this.lanes = new ArrayList<>();
        this.operational = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHighwayName() {
        return highwayName;
    }

    public void setHighwayName(String highwayName) {
        this.highwayName = highwayName;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public List<Lane> getLanes() {
        return Collections.unmodifiableList(lanes);
    }

    public void addLane(Lane lane) {
        this.lanes.add(lane);
    }

    public void removeLane(Lane lane) {
        this.lanes.remove(lane);
    }

    public Lane getLaneByNumber(int laneNumber) {
        for (Lane lane : lanes) {
            if (lane.getLaneNumber() == laneNumber) {
                return lane;
            }
        }
        return null;
    }

    public boolean isOperational() {
        return operational;
    }

    public void setOperational(boolean operational) {
        this.operational = operational;
    }

    @Override
    public String toString() {
        return String.format("Plaza[ID: %s, Name: %s, Highway: %s, Location: %s, Lanes: %d, Operational: %s]",
                id, name, highwayName, location, lanes.size(), operational ? "YES" : "NO");
    }
}
