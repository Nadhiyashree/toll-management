package com.toll.models;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class Payment {
    public enum PaymentMethod {
        FASTAG,
        CASH,
        EXEMPT
    }

    public enum PaymentStatus {
        SUCCESS,
        FAILED,
        PENDING
    }

    private String id;
    private String transactionId;
    private BigDecimal amount;
    private PaymentMethod paymentMethod;
    private PaymentStatus status;
    private LocalDateTime timestamp;
    private String reference;

    public Payment(String id, String transactionId, BigDecimal amount, PaymentMethod paymentMethod,
                   PaymentStatus status, String reference) {
        this.id = id;
        this.transactionId = transactionId;
        this.amount = amount;
        this.paymentMethod = paymentMethod;
        this.status = status;
        this.timestamp = LocalDateTime.now();
        this.reference = reference;
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

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public PaymentStatus getStatus() {
        return status;
    }

    public void setStatus(PaymentStatus status) {
        this.status = status;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    @Override
    public String toString() {
        return String.format("Payment[ID: %s, TxnId: %s, Amount: Rs.%.2f, Method: %s, Status: %s, Ref: %s]",
                id, transactionId, amount, paymentMethod, status, reference);
    }
}
