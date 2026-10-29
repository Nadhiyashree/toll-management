package com.toll.models;

public class Lane {
    public enum LaneType {
        FASTAG,
        CASH,
        EMERGENCY
    }

    public enum LaneStatus {
        OPEN,
        CLOSED
    }

    private String id;
    private String plazaId;
    private int laneNumber;
    private LaneType laneType;
    private LaneStatus laneStatus;
    private boolean processing;

    public Lane(String id, String plazaId, int laneNumber, LaneType laneType, LaneStatus laneStatus) {
        this.id = id;
        this.plazaId = plazaId;
        this.laneNumber = laneNumber;
        this.laneType = laneType;
        this.laneStatus = laneStatus;
        this.processing = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPlazaId() {
        return plazaId;
    }

    public void setPlazaId(String plazaId) {
        this.plazaId = plazaId;
    }

    public int getLaneNumber() {
        return laneNumber;
    }

    public void setLaneNumber(int laneNumber) {
        this.laneNumber = laneNumber;
    }

    public LaneType getLaneType() {
        return laneType;
    }

    public void setLaneType(LaneType laneType) {
        this.laneType = laneType;
    }

    public LaneStatus getLaneStatus() {
        return laneStatus;
    }

    public void setLaneStatus(LaneStatus laneStatus) {
        this.laneStatus = laneStatus;
    }

    public boolean isProcessing() {
        return processing;
    }

    public void setProcessing(boolean processing) {
        this.processing = processing;
    }

    @Override
    public String toString() {
        return String.format("Lane[No: %d, Type: %s, Status: %s, Processing: %s]",
                laneNumber, laneType, laneStatus, processing ? "YES" : "NO");
    }
}
