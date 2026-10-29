package com.toll.repository;

import com.toll.models.TollRate;
import com.toll.models.TollTransaction;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class TollTransactionRepository {
    private final Map<String, TollTransaction> transactionMap = new LinkedHashMap<>();

    public TollTransaction save(TollTransaction transaction) {
        if (transaction != null) {
            transactionMap.put(transaction.getId(), transaction);
        }
        return transaction;
    }

    public Optional<TollTransaction> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(transactionMap.get(id));
    }

    public List<TollTransaction> findAll() {
        return new ArrayList<>(transactionMap.values());
    }

    public List<TollTransaction> findByPlazaId(String plazaId) {
        if (plazaId == null) return Collections.emptyList();
        return transactionMap.values().stream()
                .filter(t -> t.getPlazaId().equalsIgnoreCase(plazaId.trim()))
                .collect(Collectors.toList());
    }

    public List<TollTransaction> findByVehicleRegistration(String regNo) {
        if (regNo == null) return Collections.emptyList();
        return transactionMap.values().stream()
                .filter(t -> t.getVehicleRegistrationNumber().equalsIgnoreCase(regNo.trim()))
                .collect(Collectors.toList());
    }

    public Optional<TollTransaction> findRecentSuccessfulCrossing(String regNo, String plazaId, LocalDateTime currentTime, long withinMinutes) {
        if (regNo == null || plazaId == null || currentTime == null) return Optional.empty();
        LocalDateTime threshold = currentTime.minusMinutes(withinMinutes);

        return transactionMap.values().stream()
                .filter(t -> t.getVehicleRegistrationNumber().equalsIgnoreCase(regNo.trim()))
                .filter(t -> t.getPlazaId().equalsIgnoreCase(plazaId.trim()))
                .filter(t -> t.getStatus() == TollTransaction.TransactionStatus.SUCCESS)
                .filter(t -> !t.getTimestamp().isBefore(threshold) && !t.getTimestamp().isAfter(currentTime))
                .max(Comparator.comparing(TollTransaction::getTimestamp));
    }

    public Optional<TollTransaction> findQualifyingSingleTrip(String regNo, String plazaId, LocalDateTime currentCrossingTime) {
        if (regNo == null || plazaId == null || currentCrossingTime == null) return Optional.empty();
        LocalDateTime past24Hours = currentCrossingTime.minusHours(24);

        return transactionMap.values().stream()
                .filter(t -> t.getVehicleRegistrationNumber().equalsIgnoreCase(regNo.trim()))
                .filter(t -> t.getPlazaId().equalsIgnoreCase(plazaId.trim()))
                .filter(t -> t.getStatus() == TollTransaction.TransactionStatus.SUCCESS)
                .filter(t -> t.getTripType() == TollRate.TripType.SINGLE)
                .filter(t -> !t.isReturnTripUsed())
                .filter(t -> t.getTimestamp().isAfter(past24Hours) && !t.getTimestamp().isAfter(currentCrossingTime))
                .max(Comparator.comparing(TollTransaction::getTimestamp));
    }
}
