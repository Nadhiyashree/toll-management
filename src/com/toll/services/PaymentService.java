package com.toll.services;

import com.toll.models.Payment;
import com.toll.repository.PaymentRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public class PaymentService {
    private final PaymentRepository paymentRepository;

    public PaymentService(PaymentRepository paymentRepository) {
        this.paymentRepository = paymentRepository;
    }

    public Payment recordPayment(String transactionId, BigDecimal amount, Payment.PaymentMethod paymentMethod,
                                 Payment.PaymentStatus status, String reference) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            throw new InvalidOperationException("Transaction ID is required to record payment.");
        }

        Optional<Payment> existingPayment = paymentRepository.findByTransactionId(transactionId);
        if (existingPayment.isPresent() && existingPayment.get().getStatus() == Payment.PaymentStatus.SUCCESS) {
            throw new InvalidOperationException("Payment already processed and completed for transaction " + transactionId + ".");
        }

        String paymentId = IdGenerator.nextPaymentId();
        Payment payment = new Payment(paymentId, transactionId, amount, paymentMethod, status, reference);
        return paymentRepository.save(payment);
    }

    public Optional<Payment> getPaymentByTransactionId(String transactionId) {
        return paymentRepository.findByTransactionId(transactionId);
    }

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }
}
