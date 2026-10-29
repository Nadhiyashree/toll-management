package com.toll.repository;

import com.toll.models.TollRate;
import com.toll.models.Vehicle;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class TollRateRepository {
    private final Map<String, TollRate> rateMap = new LinkedHashMap<>();

    public TollRate save(TollRate rate) {
        if (rate != null) {
            rateMap.put(rate.getId(), rate);
        }
        return rate;
    }

    public Optional<TollRate> findById(String id) {
        if (id == null) return Optional.empty();
        return Optional.ofNullable(rateMap.get(id));
    }

    public List<TollRate> findAll() {
        return new ArrayList<>(rateMap.values());
    }

    public List<TollRate> findByPlazaId(String plazaId) {
        if (plazaId == null) return Collections.emptyList();
        return rateMap.values().stream()
                .filter(r -> r.getPlazaId().equalsIgnoreCase(plazaId.trim()))
                .collect(Collectors.toList());
    }

    public Optional<TollRate> findEffectiveRate(String plazaId, Vehicle.VehicleType vehicleType, TollRate.TripType tripType, LocalDate asOfDate) {
        if (plazaId == null || vehicleType == null || tripType == null || asOfDate == null) {
            return Optional.empty();
        }
        return rateMap.values().stream()
                .filter(r -> r.getPlazaId().equalsIgnoreCase(plazaId.trim()))
                .filter(r -> r.getVehicleType() == vehicleType)
                .filter(r -> r.getTripType() == tripType)
                .filter(r -> !r.getEffectiveDate().isAfter(asOfDate))
                .max(Comparator.comparing(TollRate::getEffectiveDate));
    }

    public boolean hasRatesForSupportedVehicleTypes(String plazaId, LocalDate asOfDate) {
        Vehicle.VehicleType[] chargeableTypes = {
                Vehicle.VehicleType.CAR,
                Vehicle.VehicleType.VAN,
                Vehicle.VehicleType.BUS,
                Vehicle.VehicleType.TRUCK,
                Vehicle.VehicleType.MULTI_AXLE
        };

        for (Vehicle.VehicleType vt : chargeableTypes) {
            boolean hasSingle = findEffectiveRate(plazaId, vt, TollRate.TripType.SINGLE, asOfDate).isPresent();
            boolean hasReturn = findEffectiveRate(plazaId, vt, TollRate.TripType.RETURN, asOfDate).isPresent();
            if (!hasSingle || !hasReturn) {
                return false;
            }
        }
        return true;
    }
}
