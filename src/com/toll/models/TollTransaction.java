package com.toll.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TollTransaction {
    public enum TransactionStatus {
        SUCCESS,
        FAILED,
        PENDING
    }

    private String id;
    private String vehicleRegistrationNumber;
    private Vehicle.VehicleType registeredVehicleType;
    private Vehicle.VehicleType observedVehicleType;
    private String plazaId;
    private String plazaName;
    private String laneId;
    private int laneNumber;
    private LocalDateTime timestamp;
    private TollRate.TripType tripType;
    private BigDecimal tollAmount;
    private TransactionStatus status;
    private Payment.PaymentMethod paymentMethod;
    private String paymentId;
    private String failureReason;
    private boolean flaggedForReview;
    private String reviewReason;
    private boolean returnTripUsed;

    public TollTransaction(String id, String vehicleRegistrationNumber, Vehicle.VehicleType registeredVehicleType,
                           Vehicle.VehicleType observedVehicleType, String plazaId, String plazaName,
                           String laneId, int laneNumber, LocalDateTime timestamp, TollRate.TripType tripType,
                           BigDecimal tollAmount, TransactionStatus status, Payment.PaymentMethod paymentMethod) {
        this.id = id;
        this.vehicleRegistrationNumber = vehicleRegistrationNumber != null ? vehicleRegistrationNumber.toUpperCase().trim() : "";
        this.registeredVehicleType = registeredVehicleType;
        this.observedVehicleType = observedVehicleType;
        this.plazaId = plazaId;
        this.plazaName = plazaName;
        this.laneId = laneId;
        this.laneNumber = laneNumber;
        this.timestamp = timestamp;
        this.tripType = tripType;
        this.tollAmount = tollAmount;
        this.status = status;
        this.paymentMethod = paymentMethod;
        this.paymentId = null;
        this.failureReason = null;
        this.flaggedForReview = false;
        this.reviewReason = null;
        this.returnTripUsed = false;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVehicleRegistrationNumber() {
        return vehicleRegistrationNumber;
    }

    public void setVehicleRegistrationNumber(String vehicleRegistrationNumber) {
        this.vehicleRegistrationNumber = vehicleRegistrationNumber != null ? vehicleRegistrationNumber.toUpperCase().trim() : "";
    }

    public Vehicle.VehicleType getRegisteredVehicleType() {
        return registeredVehicleType;
    }

    public void setRegisteredVehicleType(Vehicle.VehicleType registeredVehicleType) {
        this.registeredVehicleType = registeredVehicleType;
    }

    public Vehicle.VehicleType getObservedVehicleType() {
        return observedVehicleType;
    }

    public void setObservedVehicleType(Vehicle.VehicleType observedVehicleType) {
        this.observedVehicleType = observedVehicleType;
    }

    public String getPlazaId() {
        return plazaId;
    }

    public void setPlazaId(String plazaId) {
        this.plazaId = plazaId;
    }

    public String getPlazaName() {
        return plazaName;
    }

    public void setPlazaName(String plazaName) {
        this.plazaName = plazaName;
    }

    public String getLaneId() {
        return laneId;
    }

    public void setLaneId(String laneId) {
        this.laneId = laneId;
    }

    public int getLaneNumber() {
        return laneNumber;
    }

    public void setLaneNumber(int laneNumber) {
        this.laneNumber = laneNumber;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public TollRate.TripType getTripType() {
        return tripType;
    }

    public void setTripType(TollRate.TripType tripType) {
        this.tripType = tripType;
    }

    public BigDecimal getTollAmount() {
        return tollAmount;
    }

    public void setTollAmount(BigDecimal tollAmount) {
        this.tollAmount = tollAmount;
    }

    public TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TransactionStatus status) {
        this.status = status;
    }

    public Payment.PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(Payment.PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getFailureReason() {
        return failureReason;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public boolean isFlaggedForReview() {
        return flaggedForReview;
    }

    public void setFlaggedForReview(boolean flaggedForReview) {
        this.flaggedForReview = flaggedForReview;
    }

    public String getReviewReason() {
        return reviewReason;
    }

    public void setReviewReason(String reviewReason) {
        this.reviewReason = reviewReason;
    }

    public boolean isReturnTripUsed() {
        return returnTripUsed;
    }

    public void setReturnTripUsed(boolean returnTripUsed) {
        this.returnTripUsed = returnTripUsed;
    }

    @Override
    public String toString() {
        return String.format("Txn[ID: %s, Reg: %s, Plaza: %s, Lane: %d, Amt: Rs.%.2f, Status: %s, Method: %s, Flagged: %s]",
                id, vehicleRegistrationNumber, plazaName, laneNumber, tollAmount, status, paymentMethod, flaggedForReview ? "YES" : "NO");
    }
}
