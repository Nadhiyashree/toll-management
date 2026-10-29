package com.toll.models;

import java.math.BigDecimal;

public class CrossingResult {
    private boolean success;
    private boolean barrierOpened;
    private String transactionId;
    private String registrationNumber;
    private String plazaName;
    private int laneNumber;
    private Lane.LaneType laneType;
    private BigDecimal tollAmount;
    private Payment.PaymentMethod paymentMethod;
    private TollTransaction.TransactionStatus status;
    private String message;
    private boolean redirectedToCash;
    private boolean flaggedForReview;
    private String reviewReason;

    public CrossingResult(boolean success, boolean barrierOpened, String transactionId,
                          String registrationNumber, String plazaName, int laneNumber,
                          Lane.LaneType laneType, BigDecimal tollAmount, Payment.PaymentMethod paymentMethod,
                          TollTransaction.TransactionStatus status, String message) {
        this.success = success;
        this.barrierOpened = barrierOpened;
        this.transactionId = transactionId;
        this.registrationNumber = registrationNumber;
        this.plazaName = plazaName;
        this.laneNumber = laneNumber;
        this.laneType = laneType;
        this.tollAmount = tollAmount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.message = message;
        this.redirectedToCash = false;
        this.flaggedForReview = false;
        this.reviewReason = null;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public boolean isBarrierOpened() {
        return barrierOpened;
    }

    public void setBarrierOpened(boolean barrierOpened) {
        this.barrierOpened = barrierOpened;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber;
    }

    public String getPlazaName() {
        return plazaName;
    }

    public void setPlazaName(String plazaName) {
        this.plazaName = plazaName;
    }

    public int getLaneNumber() {
        return laneNumber;
    }

    public void setLaneNumber(int laneNumber) {
        this.laneNumber = laneNumber;
    }

    public Lane.LaneType getLaneType() {
        return laneType;
    }

    public void setLaneType(Lane.LaneType laneType) {
        this.laneType = laneType;
    }

    public BigDecimal getTollAmount() {
        return tollAmount;
    }

    public void setTollAmount(BigDecimal tollAmount) {
        this.tollAmount = tollAmount;
    }

    public Payment.PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(Payment.PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public TollTransaction.TransactionStatus getStatus() {
        return status;
    }

    public void setStatus(TollTransaction.TransactionStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isRedirectedToCash() {
        return redirectedToCash;
    }

    public void setRedirectedToCash(boolean redirectedToCash) {
        this.redirectedToCash = redirectedToCash;
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
}
