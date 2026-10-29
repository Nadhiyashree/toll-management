package com.toll.models;

import java.time.LocalDateTime;

public class Dispute {
    public enum DisputeStatus {
        OPEN,
        UNDER_REVIEW,
        RESOLVED,
        REJECTED
    }

    private String id;
    private String transactionId;
    private String reason;
    private String description;
    private LocalDateTime createdAt;
    private DisputeStatus status;
    private String resolutionNotes;
    private LocalDateTime resolvedAt;
    private String resolvedBy;

    public Dispute(String id, String transactionId, String reason, String description) {
        this.id = id;
        this.transactionId = transactionId;
        this.reason = reason;
        this.description = description;
        this.createdAt = LocalDateTime.now();
        this.status = DisputeStatus.OPEN;
        this.resolutionNotes = null;
        this.resolvedAt = null;
        this.resolvedBy = null;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public DisputeStatus getStatus() {
        return status;
    }

    public void setStatus(DisputeStatus status) {
        this.status = status;
    }

    public String getResolutionNotes() {
        return resolutionNotes;
    }

    public void setResolutionNotes(String resolutionNotes) {
        this.resolutionNotes = resolutionNotes;
    }

    public LocalDateTime getResolvedAt() {
        return resolvedAt;
    }

    public void setResolvedAt(LocalDateTime resolvedAt) {
        this.resolvedAt = resolvedAt;
    }

    public String getResolvedBy() {
        return resolvedBy;
    }

    public void setResolvedBy(String resolvedBy) {
        this.resolvedBy = resolvedBy;
    }

    @Override
    public String toString() {
        return String.format("Dispute[ID: %s, TxnId: %s, Reason: %s, Status: %s, Created: %s]",
                id, transactionId, reason, status, createdAt);
    }
}
