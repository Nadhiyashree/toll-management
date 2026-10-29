package com.toll.cli;

import com.toll.models.Payment;
import com.toll.services.PaymentService;

import java.util.List;

public class PaymentCLI {
    private final PaymentService paymentService;
    private final ConsoleReader reader;

    public PaymentCLI(PaymentService paymentService, ConsoleReader reader) {
        this.paymentService = paymentService;
        this.reader = reader;
    }

    public void displayMenu() {
        System.out.println("\n--- Payments Log ---");
        List<Payment> payments = paymentService.getAllPayments();
        if (payments.isEmpty()) {
            System.out.println(" [!] No payments recorded.");
        } else {
            System.out.println(" Total Payments: " + payments.size());
            for (Payment p : payments) {
                System.out.printf(" ID: %-18s | Txn: %-22s | Amount: Rs. %8.2f | Method: %-6s | Status: %s\n",
                        p.getId(), p.getTransactionId(), p.getAmount(), p.getPaymentMethod(), p.getStatus());
            }
        }
        reader.pressEnterToContinue();
    }
}
