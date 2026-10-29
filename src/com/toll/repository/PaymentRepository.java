package com.toll.repository;

import com.toll.models.Payment;

import java.util.*;

public class PaymentRepository {
    private final Map<String, Payment> paymentMap = new LinkedHashMap<>();

    public Payment save(Payment payment) {
        if (payment != null) {
            paymentMap.put(payment.getId(), payment);
        }
        return payment;
    }

    public Optional<Payment> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(paymentMap.get(id));
    }

    public Optional<Payment> findByTransactionId(String transactionId) {
        if (transactionId == null) return Optional.empty();
        return paymentMap.values().stream()
                .filter(p -> p.getTransactionId().equalsIgnoreCase(transactionId.trim()))
                .findFirst();
    }

    public List<Payment> findAll() {
        return new ArrayList<>(paymentMap.values());
    }

    public boolean existsByTransactionId(String transactionId) {
        return findByTransactionId(transactionId).isPresent();
    }
}
