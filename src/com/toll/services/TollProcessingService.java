package com.toll.services;

import com.toll.models.*;
import com.toll.repository.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public class TollProcessingService {
    private final TollPlazaRepository tollPlazaRepository;
    private final VehicleRepository vehicleRepository;
    private final FASTagAccountRepository fastagAccountRepository;
    private final TollRateRepository tollRateRepository;
    private final MonthlyPassRepository monthlyPassRepository;
    private final TollTransactionRepository tollTransactionRepository;
    private final PaymentRepository paymentRepository;

    public TollProcessingService(TollPlazaRepository tollPlazaRepository,
                                 VehicleRepository vehicleRepository,
                                 FASTagAccountRepository fastagAccountRepository,
                                 TollRateRepository tollRateRepository,
                                 MonthlyPassRepository monthlyPassRepository,
                                 TollTransactionRepository tollTransactionRepository,
                                 PaymentRepository paymentRepository) {
        this.tollPlazaRepository = tollPlazaRepository;
        this.vehicleRepository = vehicleRepository;
        this.fastagAccountRepository = fastagAccountRepository;
        this.tollRateRepository = tollRateRepository;
        this.monthlyPassRepository = monthlyPassRepository;
        this.tollTransactionRepository = tollTransactionRepository;
        this.paymentRepository = paymentRepository;
    }

    public CrossingResult processCrossing(String plazaId, int laneNumber, String vehicleIdentifier, Vehicle.VehicleType observedType) {
        LocalDateTime crossingTime = LocalDateTime.now();
        LocalDate crossingDate = crossingTime.toLocalDate();

        TollPlaza plaza = tollPlazaRepository.findById(plazaId)
                .orElseThrow(() -> new EntityNotFoundException("Toll Plaza with ID " + plazaId + " not found."));

        if (!plaza.isOperational()) {
            throw new InvalidOperationException("Plaza '" + plaza.getName() + "' is currently NOT operational. Processing rejected.");
        }

        Lane lane = plaza.getLaneByNumber(laneNumber);
        if (lane == null) {
            throw new EntityNotFoundException("Lane " + laneNumber + " does not exist at plaza '" + plaza.getName() + "'.");
        }

        if (lane.getLaneStatus() == Lane.LaneStatus.CLOSED) {
            throw new InvalidOperationException("Lane " + laneNumber + " is currently CLOSED. Vehicle cannot enter.");
        }

        lane.setProcessing(true);
        try {
            Vehicle vehicle = resolveVehicle(vehicleIdentifier);
            if (vehicle == null) {
                return failCrossing(plaza, lane, null, observedType, crossingTime, BigDecimal.ZERO,
                        Payment.PaymentMethod.CASH, "Vehicle not registered in the system. Entry rejected.");
            }

            Vehicle.VehicleType registeredType = vehicle.getVehicleType();
            if (observedType == null) {
                observedType = registeredType;
            }

            // Duplicate crossing rule (< 2 minutes)
            Optional<TollTransaction> recentCrossing = tollTransactionRepository.findRecentSuccessfulCrossing(
                    vehicle.getRegistrationNumber(), plaza.getId(), crossingTime, 2);
            if (recentCrossing.isPresent()) {
                String errorMsg = "Duplicate crossing detected: Vehicle " + vehicle.getRegistrationNumber() +
                        " was already successfully processed at this plaza within the last 2 minutes (Txn: " +
                        recentCrossing.get().getId() + "). Barrier remains CLOSED.";
                return failCrossing(plaza, lane, vehicle, observedType, crossingTime, BigDecimal.ZERO,
                        Payment.PaymentMethod.CASH, errorMsg);
            }

            // Emergency Vehicle
            if (vehicle.isEmergencyVehicle() || observedType.isEmergency()) {
                if (lane.getLaneType() != Lane.LaneType.EMERGENCY) {
                    return failCrossing(plaza, lane, vehicle, observedType, crossingTime, BigDecimal.ZERO,
                            Payment.PaymentMethod.EXEMPT, "Emergency vehicles must use the dedicated EMERGENCY lane.");
                }
                return completeSuccessfulCrossing(plaza, lane, vehicle, observedType, crossingTime,
                        TollRate.TripType.SINGLE, BigDecimal.ZERO, Payment.PaymentMethod.EXEMPT, false, null, null,
                        "Emergency vehicle (" + observedType.getDisplayName() + ") granted free passage through Emergency Lane.");
            }

            if (lane.getLaneType() == Lane.LaneType.EMERGENCY) {
                return failCrossing(plaza, lane, vehicle, observedType, crossingTime, BigDecimal.ZERO,
                        Payment.PaymentMethod.CASH, "Emergency lane is strictly reserved for ambulances and fire engines.");
            }

            // Monthly Pass
            Optional<MonthlyPass> validPass = monthlyPassRepository.findValidPass(
                    vehicle.getRegistrationNumber(), plaza.getId(), crossingDate);
            if (validPass.isPresent()) {
                return completeSuccessfulCrossing(plaza, lane, vehicle, observedType, crossingTime,
                        TollRate.TripType.SINGLE, BigDecimal.ZERO, Payment.PaymentMethod.EXEMPT, false, null, null,
                        "Valid Monthly Pass (" + validPass.get().getId() + ") verified. Toll exempted.");
            }

            // Toll calculation & return trip
            TollRate.TripType tripType = TollRate.TripType.SINGLE;
            Optional<TollTransaction> qualifyingSingle = tollTransactionRepository.findQualifyingSingleTrip(
                    vehicle.getRegistrationNumber(), plaza.getId(), crossingTime);
            if (qualifyingSingle.isPresent()) {
                tripType = TollRate.TripType.RETURN;
            }

            boolean typeMismatch = (registeredType != observedType);
            String mismatchReason = null;
            BigDecimal tollAmount;

            if (typeMismatch) {
                BigDecimal registeredRate = getRateFor(plaza.getId(), registeredType, tripType, crossingDate);
                BigDecimal observedRate = getRateFor(plaza.getId(), observedType, tripType, crossingDate);
                tollAmount = registeredRate.max(observedRate);
                mismatchReason = "Vehicle type mismatch: Registered=" + registeredType + ", Observed=" + observedType +
                        ". Charged higher toll of Rs. " + tollAmount + " and flagged for review.";
            } else {
                tollAmount = getRateFor(plaza.getId(), registeredType, tripType, crossingDate);
            }

            Optional<FASTagAccount> fastagAccountOpt = fastagAccountRepository.findByVehicleRegistrationNumber(vehicle.getRegistrationNumber());

            if (lane.getLaneType() == Lane.LaneType.FASTAG) {
                if (fastagAccountOpt.isEmpty()) {
                    String msg = "No FASTag registered for vehicle " + vehicle.getRegistrationNumber() +
                            ". Barrier remains CLOSED. Directed to CASH lane (double toll applies).";
                    recordFailedTransaction(plaza, lane, vehicle, observedType, crossingTime, tollAmount,
                            Payment.PaymentMethod.FASTAG, msg, typeMismatch, mismatchReason);
                    CrossingResult res = new CrossingResult(false, false, null, vehicle.getRegistrationNumber(),
                            plaza.getName(), lane.getLaneNumber(), lane.getLaneType(), tollAmount, Payment.PaymentMethod.FASTAG,
                            TollTransaction.TransactionStatus.FAILED, msg);
                    res.setRedirectedToCash(true);
                    return res;
                }

                FASTagAccount account = fastagAccountOpt.get();

                if (account.isBlacklisted()) {
                    String msg = "FASTag " + account.getTagId() + " is BLACKLISTED. Barrier remains CLOSED. Passage rejected at plaza.";
                    recordFailedTransaction(plaza, lane, vehicle, observedType, crossingTime, tollAmount,
                            Payment.PaymentMethod.FASTAG, msg, typeMismatch, mismatchReason);
                    return new CrossingResult(false, false, null, vehicle.getRegistrationNumber(),
                            plaza.getName(), lane.getLaneNumber(), lane.getLaneType(), tollAmount, Payment.PaymentMethod.FASTAG,
                            TollTransaction.TransactionStatus.FAILED, msg);
                }

                if (account.getStatus() != FASTagAccount.AccountStatus.ACTIVE) {
                    String msg = "FASTag " + account.getTagId() + " status is " + account.getStatus() +
                            " (Inactive/Blocked). Directed to CASH lane.";
                    recordFailedTransaction(plaza, lane, vehicle, observedType, crossingTime, tollAmount,
                            Payment.PaymentMethod.FASTAG, msg, typeMismatch, mismatchReason);
                    CrossingResult res = new CrossingResult(false, false, null, vehicle.getRegistrationNumber(),
                            plaza.getName(), lane.getLaneNumber(), lane.getLaneType(), tollAmount, Payment.PaymentMethod.FASTAG,
                            TollTransaction.TransactionStatus.FAILED, msg);
                    res.setRedirectedToCash(true);
                    return res;
                }

                if (account.getBalance().compareTo(tollAmount) < 0) {
                    String msg = "Insufficient FASTag balance: Available Rs. " + account.getBalance() +
                            ", Required Rs. " + tollAmount + ". Directed to CASH lane (normal cash toll applies).";
                    recordFailedTransaction(plaza, lane, vehicle, observedType, crossingTime, tollAmount,
                            Payment.PaymentMethod.FASTAG, msg, typeMismatch, mismatchReason);
                    CrossingResult res = new CrossingResult(false, false, null, vehicle.getRegistrationNumber(),
                            plaza.getName(), lane.getLaneNumber(), lane.getLaneType(), tollAmount, Payment.PaymentMethod.FASTAG,
                            TollTransaction.TransactionStatus.FAILED, msg);
                    res.setRedirectedToCash(true);
                    return res;
                }

                // Atomic deduction
                BigDecimal newBalance = account.getBalance().subtract(tollAmount);
                account.setBalance(newBalance);
                if (newBalance.compareTo(FASTagService.MIN_ACTIVE_BALANCE) < 0) {
                    account.setStatus(FASTagAccount.AccountStatus.BLOCKED);
                }
                fastagAccountRepository.save(account);

                String notes = "FASTag debited Rs. " + tollAmount + " successfully. Remaining Balance: Rs. " + newBalance;
                if (account.getStatus() == FASTagAccount.AccountStatus.BLOCKED) {
                    notes += " (Account automatically BLOCKED as balance fell below Rs. 100).";
                }

                return completeSuccessfulCrossing(plaza, lane, vehicle, observedType, crossingTime, tripType,
                        tollAmount, Payment.PaymentMethod.FASTAG, typeMismatch, mismatchReason, qualifyingSingle.orElse(null), notes);

            } else if (lane.getLaneType() == Lane.LaneType.CASH) {
                boolean hasValidActiveTag = fastagAccountOpt.isPresent() &&
                        fastagAccountOpt.get().getStatus() == FASTagAccount.AccountStatus.ACTIVE &&
                        !fastagAccountOpt.get().isBlacklisted();

                BigDecimal payableCashToll;
                String cashNote;

                if (!hasValidActiveTag) {
                    payableCashToll = TollCalculator.calculateDoubleToll(tollAmount);
                    cashNote = "No valid active FASTag detected. Penalty applied: Double toll charged (Rs. " + payableCashToll + "). Paid in cash.";
                } else {
                    payableCashToll = tollAmount;
                    cashNote = "Standard cash toll of Rs. " + payableCashToll + " collected in Cash Lane.";
                }

                return completeSuccessfulCrossing(plaza, lane, vehicle, observedType, crossingTime, tripType,
                        payableCashToll, Payment.PaymentMethod.CASH, typeMismatch, mismatchReason, qualifyingSingle.orElse(null), cashNote);
            }

            return failCrossing(plaza, lane, vehicle, observedType, crossingTime, BigDecimal.ZERO,
                    Payment.PaymentMethod.CASH, "Unsupported lane configuration.");

        } finally {
            lane.setProcessing(false);
        }
    }

    private BigDecimal getRateFor(String plazaId, Vehicle.VehicleType vehicleType, TollRate.TripType tripType, LocalDate date) {
        return tollRateRepository.findEffectiveRate(plazaId, vehicleType, tripType, date)
                .map(TollRate::getRate)
                .orElseThrow(() -> new InvalidOperationException("No configured toll rate found for Plaza: " +
                        plazaId + ", Vehicle: " + vehicleType + ", Trip: " + tripType));
    }

    private Vehicle resolveVehicle(String identifier) {
        if (identifier == null || identifier.trim().isEmpty()) {
            return null;
        }
        String clean = identifier.trim();
        Optional<Vehicle> byReg = vehicleRepository.findByRegistrationNumber(clean);
        if (byReg.isPresent()) {
            return byReg.get();
        }
        Optional<FASTagAccount> byTag = fastagAccountRepository.findByTagId(clean);
        if (byTag.isPresent()) {
            return vehicleRepository.findByRegistrationNumber(byTag.get().getVehicleRegistrationNumber()).orElse(null);
        }
        return vehicleRepository.findByFastagId(clean).orElse(null);
    }

    private CrossingResult completeSuccessfulCrossing(TollPlaza plaza, Lane lane, Vehicle vehicle,
                                                      Vehicle.VehicleType observedType, LocalDateTime timestamp,
                                                      TollRate.TripType tripType, BigDecimal amount, Payment.PaymentMethod method,
                                                      boolean flagged, String reviewReason,
                                                      TollTransaction qualifyingSingleTxn, String message) {
        String txnId = IdGenerator.nextTransactionId();
        TollTransaction txn = new TollTransaction(txnId, vehicle.getRegistrationNumber(), vehicle.getVehicleType(),
                observedType, plaza.getId(), plaza.getName(), lane.getId(), lane.getLaneNumber(),
                timestamp, tripType, amount, TollTransaction.TransactionStatus.SUCCESS, method);

        if (flagged) {
            txn.setFlaggedForReview(true);
            txn.setReviewReason(reviewReason);
        }

        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            Payment pmt = new Payment(IdGenerator.nextPaymentId(), txnId, amount, method, Payment.PaymentStatus.SUCCESS, message);
            paymentRepository.save(pmt);
            txn.setPaymentId(pmt.getId());
        }

        if (tripType == TollRate.TripType.RETURN && qualifyingSingleTxn != null) {
            qualifyingSingleTxn.setReturnTripUsed(true);
            tollTransactionRepository.save(qualifyingSingleTxn);
        }

        tollTransactionRepository.save(txn);

        CrossingResult result = new CrossingResult(true, true, txnId, vehicle.getRegistrationNumber(),
                plaza.getName(), lane.getLaneNumber(), lane.getLaneType(), amount, method,
                TollTransaction.TransactionStatus.SUCCESS, message);
        result.setFlaggedForReview(flagged);
        result.setReviewReason(reviewReason);
        return result;
    }

    private CrossingResult failCrossing(TollPlaza plaza, Lane lane, Vehicle vehicle, Vehicle.VehicleType observedType,
                                        LocalDateTime timestamp, BigDecimal amount, Payment.PaymentMethod method, String reason) {
        recordFailedTransaction(plaza, lane, vehicle, observedType, timestamp, amount, method, reason, false, null);
        return new CrossingResult(false, false, null, vehicle != null ? vehicle.getRegistrationNumber() : "UNKNOWN",
                plaza.getName(), lane.getLaneNumber(), lane.getLaneType(), amount, method,
                TollTransaction.TransactionStatus.FAILED, reason);
    }

    private void recordFailedTransaction(TollPlaza plaza, Lane lane, Vehicle vehicle, Vehicle.VehicleType observedType,
                                         LocalDateTime timestamp, BigDecimal amount, Payment.PaymentMethod method,
                                         String failureReason, boolean flagged, String reviewReason) {
        String txnId = IdGenerator.nextTransactionId();
        String regNo = vehicle != null ? vehicle.getRegistrationNumber() : "UNKNOWN";
        Vehicle.VehicleType regType = vehicle != null ? vehicle.getVehicleType() : observedType;

        TollTransaction txn = new TollTransaction(txnId, regNo, regType, observedType,
                plaza.getId(), plaza.getName(), lane.getId(), lane.getLaneNumber(), timestamp,
                TollRate.TripType.SINGLE, amount, TollTransaction.TransactionStatus.FAILED, method);
        txn.setFailureReason(failureReason);
        txn.setFlaggedForReview(flagged);
        txn.setReviewReason(reviewReason);

        if (amount.compareTo(BigDecimal.ZERO) > 0) {
            Payment pmt = new Payment(IdGenerator.nextPaymentId(), txnId, amount, method, Payment.PaymentStatus.FAILED, failureReason);
            paymentRepository.save(pmt);
            txn.setPaymentId(pmt.getId());
        }

        tollTransactionRepository.save(txn);
    }
}
