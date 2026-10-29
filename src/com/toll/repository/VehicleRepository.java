package com.toll.repository;

import com.toll.models.Vehicle;
import java.util.*;

public class VehicleRepository {
    private final Map<String, Vehicle> vehicleMap = new LinkedHashMap<>();

    public Vehicle save(Vehicle vehicle) {
        if (vehicle != null) {
            vehicleMap.put(vehicle.getRegistrationNumber().toUpperCase().trim(), vehicle);
        }
        return vehicle;
    }

    public Optional<Vehicle> findByRegistrationNumber(String regNo) {
        if (regNo == null) return Optional.empty();
        return Optional.ofNullable(vehicleMap.get(regNo.toUpperCase().trim()));
    }

    public Optional<Vehicle> findByFastagId(String tagId) {
        if (tagId == null) return Optional.empty();
        return vehicleMap.values().stream()
                .filter(v -> v.getFastagId() != null && v.getFastagId().equalsIgnoreCase(tagId.trim()))
                .findFirst();
    }

    public List<Vehicle> findAll() {
        return new ArrayList<>(vehicleMap.values());
    }

    public boolean existsByRegistrationNumber(String regNo) {
        if (regNo == null) return false;
        return vehicleMap.containsKey(regNo.toUpperCase().trim());
    }
}
