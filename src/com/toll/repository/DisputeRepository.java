package com.toll.repository;

import com.toll.models.Dispute;

import java.util.*;
import java.util.stream.Collectors;

public class DisputeRepository {
    private final Map<String, Dispute> disputeMap = new LinkedHashMap<>();

    public Dispute save(Dispute dispute) {
        if (dispute != null) {
            disputeMap.put(dispute.getId(), dispute);
        }
        return dispute;
    }

    public Optional<Dispute> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(disputeMap.get(id));
    }

    public List<Dispute> findAll() {
        return new ArrayList<>(disputeMap.values());
    }

    public List<Dispute> findByTransactionId(String transactionId) {
        if (transactionId == null) return Collections.emptyList();
        return disputeMap.values().stream()
                .filter(d -> d.getTransactionId().equalsIgnoreCase(transactionId.trim()))
                .collect(Collectors.toList());
    }

    public List<Dispute> findByStatus(Dispute.DisputeStatus status) {
        if (status == null) return Collections.emptyList();
        return disputeMap.values().stream()
                .filter(d -> d.getStatus() == status)
                .collect(Collectors.toList());
    }
}
