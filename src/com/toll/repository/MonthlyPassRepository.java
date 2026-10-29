package com.toll.repository;

import com.toll.models.MonthlyPass;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class MonthlyPassRepository {
    private final Map<String, MonthlyPass> passMap = new LinkedHashMap<>();

    public MonthlyPass save(MonthlyPass pass) {
        if (pass != null) {
            passMap.put(pass.getId(), pass);
        }
        return pass;
    }

    public Optional<MonthlyPass> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(passMap.get(id));
    }

    public List<MonthlyPass> findAll() {
        return new ArrayList<>(passMap.values());
    }

    public List<MonthlyPass> findByVehicleRegistration(String regNo) {
        if (regNo == null) return Collections.emptyList();
        return passMap.values().stream()
                .filter(p -> p.getVehicleRegistrationNumber().equalsIgnoreCase(regNo.trim()))
                .collect(Collectors.toList());
    }

    public Optional<MonthlyPass> findValidPass(String regNo, String plazaId, LocalDate asOfDate) {
        if (regNo == null || plazaId == null || asOfDate == null) return Optional.empty();
        return passMap.values().stream()
                .filter(p -> p.getVehicleRegistrationNumber().equalsIgnoreCase(regNo.trim()))
                .filter(p -> p.getPlazaId().equalsIgnoreCase(plazaId.trim()))
                .filter(p -> p.isValidOn(asOfDate))
                .findFirst();
    }

    public boolean hasOverlappingPass(String regNo, String plazaId, LocalDate startDate, LocalDate endDate) {
        if (regNo == null || plazaId == null || startDate == null || endDate == null) return false;
        return passMap.values().stream()
                .filter(MonthlyPass::isActive)
                .filter(p -> p.getVehicleRegistrationNumber().equalsIgnoreCase(regNo.trim()))
                .filter(p -> p.getPlazaId().equalsIgnoreCase(plazaId.trim()))
                .anyMatch(p -> !(endDate.isBefore(p.getStartDate()) || startDate.isAfter(p.getEndDate())));
    }
}
