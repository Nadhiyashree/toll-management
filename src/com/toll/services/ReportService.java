package com.toll.services;

import com.toll.models.*;
import com.toll.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class ReportService {
    private final TollTransactionRepository tollTransactionRepository;
    private final PaymentRepository paymentRepository;
    private final MonthlyPassRepository monthlyPassRepository;
    private final DisputeRepository disputeRepository;
    private final TollPlazaRepository tollPlazaRepository;
    private final FASTagAccountRepository fastagAccountRepository;

    public ReportService(TollTransactionRepository tollTransactionRepository,
                         PaymentRepository paymentRepository,
                         MonthlyPassRepository monthlyPassRepository,
                         DisputeRepository disputeRepository,
                         TollPlazaRepository tollPlazaRepository,
                         FASTagAccountRepository fastagAccountRepository) {
        this.tollTransactionRepository = tollTransactionRepository;
        this.paymentRepository = paymentRepository;
        this.monthlyPassRepository = monthlyPassRepository;
        this.disputeRepository = disputeRepository;
        this.tollPlazaRepository = tollPlazaRepository;
        this.fastagAccountRepository = fastagAccountRepository;
    }

    private List<TollTransaction> filterTransactions(String plazaId, LocalDate startDate, LocalDate endDate) {
        return tollTransactionRepository.findAll().stream()
                .filter(t -> plazaId == null || plazaId.trim().isEmpty() || t.getPlazaId().equalsIgnoreCase(plazaId.trim()))
                .filter(t -> startDate == null || !t.getTimestamp().toLocalDate().isBefore(startDate))
                .filter(t -> endDate == null || !t.getTimestamp().toLocalDate().isAfter(endDate))
                .collect(Collectors.toList());
    }

    public String generateCrossingsAndRevenueSummary(String plazaId, LocalDate startDate, LocalDate endDate) {
        List<TollTransaction> txns = filterTransactions(plazaId, startDate, endDate);
        StringBuilder sb = new StringBuilder();

        sb.append("\n========================================================================================\n");
        sb.append("                   CROSSINGS & REVENUE SUMMARY REPORT\n");
        sb.append(String.format(" Filter: Plaza=%s | Date Range: %s to %s\n",
                (plazaId == null || plazaId.isEmpty()) ? "ALL" : plazaId,
                startDate != null ? startDate.toString() : "Beginning",
                endDate != null ? endDate.toString() : "Current"));
        sb.append("========================================================================================\n");

        if (txns.isEmpty()) {
            sb.append(" [!] No crossing transactions found for the specified criteria.\n");
            return sb.toString();
        }

        long totalCrossings = txns.size();
        long successCount = txns.stream().filter(t -> t.getStatus() == TollTransaction.TransactionStatus.SUCCESS).count();
        long failedCount = txns.stream().filter(t -> t.getStatus() == TollTransaction.TransactionStatus.FAILED).count();
        long pendingCount = txns.stream().filter(t -> t.getStatus() == TollTransaction.TransactionStatus.PENDING).count();

        BigDecimal fastagCollection = BigDecimal.ZERO;
        BigDecimal cashCollection = BigDecimal.ZERO;
        BigDecimal totalRevenue = BigDecimal.ZERO;

        Map<String, BigDecimal> revenueByPlaza = new TreeMap<>();
        Map<String, BigDecimal> revenueByLane = new TreeMap<>();
        Map<Vehicle.VehicleType, BigDecimal> revenueByVehicle = new EnumMap<>(Vehicle.VehicleType.class);

        for (TollTransaction t : txns) {
            if (t.getStatus() == TollTransaction.TransactionStatus.SUCCESS) {
                BigDecimal amt = t.getTollAmount();
                totalRevenue = totalRevenue.add(amt);
                if (t.getPaymentMethod() == Payment.PaymentMethod.FASTAG) {
                    fastagCollection = fastagCollection.add(amt);
                } else if (t.getPaymentMethod() == Payment.PaymentMethod.CASH) {
                    cashCollection = cashCollection.add(amt);
                }

                revenueByPlaza.merge(t.getPlazaName(), amt, BigDecimal::add);
                String laneKey = t.getPlazaName() + " - Lane " + t.getLaneNumber();
                revenueByLane.merge(laneKey, amt, BigDecimal::add);
                Vehicle.VehicleType vt = t.getObservedVehicleType() != null ? t.getObservedVehicleType() : t.getRegisteredVehicleType();
                if (vt != null) {
                    revenueByVehicle.merge(vt, amt, BigDecimal::add);
                }
            }
        }

        sb.append(String.format(" Total Crossings Recorded : %d\n", totalCrossings));
        sb.append(String.format("  - Successful Crossings  : %d (%.1f%%)\n", successCount, (successCount * 100.0 / totalCrossings)));
        sb.append(String.format("  - Failed Attempts       : %d (%.1f%%)\n", failedCount, (failedCount * 100.0 / totalCrossings)));
        sb.append(String.format("  - Pending Processing    : %d\n", pendingCount));
        sb.append("----------------------------------------------------------------------------------------\n");
        sb.append(String.format(" Total Revenue Collected  : Rs. %.2f\n", totalRevenue));
        sb.append(String.format("  - FASTag Revenue (Electronic) : Rs. %.2f\n", fastagCollection));
        sb.append(String.format("  - Cash Revenue (Manual)       : Rs. %.2f\n", cashCollection));
        sb.append("----------------------------------------------------------------------------------------\n");

        sb.append("\n [REVENUE BREAKDOWN BY TOLL PLAZA]\n");
        if (revenueByPlaza.isEmpty()) {
            sb.append("  None\n");
        } else {
            revenueByPlaza.forEach((plaza, rev) ->
                    sb.append(String.format("  * %-30s : Rs. %10.2f\n", plaza, rev)));
        }

        sb.append("\n [REVENUE BREAKDOWN BY LANE]\n");
        if (revenueByLane.isEmpty()) {
            sb.append("  None\n");
        } else {
            revenueByLane.forEach((lane, rev) ->
                    sb.append(String.format("  * %-30s : Rs. %10.2f\n", lane, rev)));
        }

        sb.append("\n [REVENUE BREAKDOWN BY VEHICLE TYPE]\n");
        if (revenueByVehicle.isEmpty()) {
            sb.append("  None\n");
        } else {
            revenueByVehicle.forEach((vt, rev) ->
                    sb.append(String.format("  * %-30s : Rs. %10.2f\n", vt.getDisplayName(), rev)));
        }

        sb.append("========================================================================================\n");
        return sb.toString();
    }

    public String generateExceptionsAndAuditReport(String plazaId, LocalDate startDate, LocalDate endDate) {
        List<TollTransaction> txns = filterTransactions(plazaId, startDate, endDate);
        StringBuilder sb = new StringBuilder();

        sb.append("\n========================================================================================\n");
        sb.append("               AUDIT & EXCEPTION EVENTS REPORT (FAILURES / MISMATCHES / RESTRICTIONS)\n");
        sb.append("========================================================================================\n");

        List<TollTransaction> failedTxns = txns.stream()
                .filter(t -> t.getStatus() == TollTransaction.TransactionStatus.FAILED)
                .collect(Collectors.toList());

        List<TollTransaction> insufficientBalanceEvents = failedTxns.stream()
                .filter(t -> t.getFailureReason() != null && t.getFailureReason().toLowerCase().contains("insufficient"))
                .collect(Collectors.toList());

        List<TollTransaction> blacklistedAttempts = failedTxns.stream()
                .filter(t -> t.getFailureReason() != null && t.getFailureReason().toLowerCase().contains("blacklisted"))
                .collect(Collectors.toList());

        List<TollTransaction> duplicateAttempts = failedTxns.stream()
                .filter(t -> t.getFailureReason() != null && t.getFailureReason().toLowerCase().contains("duplicate"))
                .collect(Collectors.toList());

        List<TollTransaction> mismatches = txns.stream()
                .filter(TollTransaction::isFlaggedForReview)
                .collect(Collectors.toList());

        sb.append(String.format(" 1. Insufficient Balance Incidents: %d\n", insufficientBalanceEvents.size()));
        if (!insufficientBalanceEvents.isEmpty()) {
            insufficientBalanceEvents.forEach(t -> sb.append(String.format("    - [%s] Reg: %-12s Plaza: %-15s Reason: %s\n",
                    t.getTimestamp().toLocalDate(), t.getVehicleRegistrationNumber(), t.getPlazaName(), t.getFailureReason())));
        }

        sb.append(String.format("\n 2. Blacklisted FASTag Crossing Attempts: %d\n", blacklistedAttempts.size()));
        if (!blacklistedAttempts.isEmpty()) {
            blacklistedAttempts.forEach(t -> sb.append(String.format("    - [!] [%s] Reg: %-12s Plaza: %-15s Details: %s\n",
                    t.getTimestamp().toLocalDate(), t.getVehicleRegistrationNumber(), t.getPlazaName(), t.getFailureReason())));
        } else {
            sb.append("    (No blacklisted tag attempts recorded)\n");
        }

        sb.append(String.format("\n 3. Vehicle Type Mismatch Flagged for Review: %d\n", mismatches.size()));
        if (!mismatches.isEmpty()) {
            mismatches.forEach(t -> sb.append(String.format("    - [?] [%s] Txn: %-20s Reg: %-12s Registered: %s | Observed: %s | Notes: %s\n",
                    t.getTimestamp().toLocalDate(), t.getId(), t.getVehicleRegistrationNumber(),
                    t.getRegisteredVehicleType(), t.getObservedVehicleType(), t.getReviewReason())));
        } else {
            sb.append("    (No vehicle type mismatches recorded)\n");
        }

        sb.append(String.format("\n 4. Duplicate Crossing Violations (< 2 minutes): %d\n", duplicateAttempts.size()));
        if (!duplicateAttempts.isEmpty()) {
            duplicateAttempts.forEach(t -> sb.append(String.format("    - [%s] Reg: %-12s Plaza: %-15s Reason: %s\n",
                    t.getTimestamp().toLocalDate(), t.getVehicleRegistrationNumber(), t.getPlazaName(), t.getFailureReason())));
        } else {
            sb.append("    (No duplicate crossing violations recorded)\n");
        }

        sb.append("========================================================================================\n");
        return sb.toString();
    }

    public String generateMonthlyPassesReport(String plazaId) {
        List<MonthlyPass> passes = monthlyPassRepository.findAll().stream()
                .filter(p -> plazaId == null || plazaId.trim().isEmpty() || p.getPlazaId().equalsIgnoreCase(plazaId.trim()))
                .collect(Collectors.toList());

        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================================================================\n");
        sb.append("                         MONTHLY PASSES STATUS REPORT\n");
        sb.append("========================================================================================\n");

        if (passes.isEmpty()) {
            sb.append(" [!] No monthly passes found.\n");
            return sb.toString();
        }

        LocalDate today = LocalDate.now();
        long activeCount = passes.stream().filter(p -> p.isValidOn(today)).count();
        long expiredCount = passes.size() - activeCount;

        sb.append(String.format(" Total Monthly Passes Issued: %d | Active Today: %d | Expired: %d\n\n",
                passes.size(), activeCount, expiredCount));

        sb.append(String.format(" %-12s | %-12s | %-10s | %-12s | %-12s | %-10s | %-8s\n",
                "Pass ID", "Vehicle Reg", "Plaza ID", "Start Date", "End Date", "Fee (Rs.)", "Valid?"));
        sb.append("----------------------------------------------------------------------------------------\n");

        for (MonthlyPass p : passes) {
            sb.append(String.format(" %-12s | %-12s | %-10s | %-12s | %-12s | %10.2f | %-8s\n",
                    p.getId(), p.getVehicleRegistrationNumber(), p.getPlazaId(), p.getStartDate(),
                    p.getEndDate(), p.getAmountPaid(), p.isValidOn(today) ? "YES" : "NO"));
        }

        sb.append("========================================================================================\n");
        return sb.toString();
    }

    public String generateDisputesReport() {
        List<Dispute> disputes = disputeRepository.findAll();
        StringBuilder sb = new StringBuilder();

        sb.append("\n========================================================================================\n");
        sb.append("                            DISPUTES SUMMARY REPORT\n");
        sb.append("========================================================================================\n");

        if (disputes.isEmpty()) {
            sb.append(" [!] No disputes have been raised.\n");
            return sb.toString();
        }

        long openCount = disputes.stream().filter(d -> d.getStatus() == Dispute.DisputeStatus.OPEN).count();
        long underReviewCount = disputes.stream().filter(d -> d.getStatus() == Dispute.DisputeStatus.UNDER_REVIEW).count();
        long resolvedCount = disputes.stream().filter(d -> d.getStatus() == Dispute.DisputeStatus.RESOLVED).count();
        long rejectedCount = disputes.stream().filter(d -> d.getStatus() == Dispute.DisputeStatus.REJECTED).count();

        sb.append(String.format(" Total Disputes: %d | OPEN: %d | UNDER REVIEW: %d | RESOLVED: %d | REJECTED: %d\n\n",
                disputes.size(), openCount, underReviewCount, resolvedCount, rejectedCount));

        sb.append(String.format(" %-10s | %-24s | %-15s | %-14s | %-20s\n",
                "Dispute ID", "Transaction ID", "Reason", "Status", "Resolution Notes"));
        sb.append("----------------------------------------------------------------------------------------\n");

        for (Dispute d : disputes) {
            String notes = d.getResolutionNotes() != null ? d.getResolutionNotes() : "-";
            if (notes.length() > 20) notes = notes.substring(0, 17) + "...";
            sb.append(String.format(" %-10s | %-24s | %-15s | %-14s | %-20s\n",
                    d.getId(), d.getTransactionId(), d.getReason(), d.getStatus(), notes));
        }

        sb.append("========================================================================================\n");
        return sb.toString();
    }
}
