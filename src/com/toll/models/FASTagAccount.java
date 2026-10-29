package com.toll.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class FASTagAccount {
    public enum AccountStatus {
        ACTIVE,
        BLOCKED,
        CLOSED
    }

    private String tagId;
    private String vehicleRegistrationNumber;
    private BigDecimal balance;
    private AccountStatus status;
    private boolean blacklisted;
    private LocalDateTime createdAt;
    private LocalDateTime lastRechargedAt;

    public FASTagAccount(String tagId, String vehicleRegistrationNumber, BigDecimal initialBalance) {
        this.tagId = tagId;
        this.vehicleRegistrationNumber = vehicleRegistrationNumber.toUpperCase().trim();
        this.balance = initialBalance != null ? initialBalance : BigDecimal.ZERO;
        this.status = this.balance.compareTo(new BigDecimal("100.00")) >= 0 ? AccountStatus.ACTIVE : AccountStatus.BLOCKED;
        this.blacklisted = false;
        this.createdAt = LocalDateTime.now();
        this.lastRechargedAt = LocalDateTime.now();
    }

    public String getTagId() {
        return tagId;
    }

    public void setTagId(String tagId) {
        this.tagId = tagId;
    }

    public String getVehicleRegistrationNumber() {
        return vehicleRegistrationNumber;
    }

    public void setVehicleRegistrationNumber(String vehicleRegistrationNumber) {
        this.vehicleRegistrationNumber = vehicleRegistrationNumber.toUpperCase().trim();
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public void setStatus(AccountStatus status) {
        this.status = status;
    }

    public boolean isBlacklisted() {
        return blacklisted;
    }

    public void setBlacklisted(boolean blacklisted) {
        this.blacklisted = blacklisted;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastRechargedAt() {
        return lastRechargedAt;
    }

    public void setLastRechargedAt(LocalDateTime lastRechargedAt) {
        this.lastRechargedAt = lastRechargedAt;
    }

    public boolean isValidForCrossing() {
        return !blacklisted && status == AccountStatus.ACTIVE;
    }

    @Override
    public String toString() {
        return String.format("FASTag[TagId: %s, RegNo: %s, Balance: Rs.%.2f, Status: %s, Blacklisted: %s]",
                tagId, vehicleRegistrationNumber, balance, status, blacklisted ? "YES" : "NO");
    }
}
