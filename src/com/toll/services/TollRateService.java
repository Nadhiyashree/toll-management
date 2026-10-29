package com.toll.services;

import com.toll.models.TollPlaza;
import com.toll.models.TollRate;
import com.toll.models.Vehicle;
import com.toll.repository.TollPlazaRepository;
import com.toll.repository.TollRateRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TollRateService {
    private final TollRateRepository tollRateRepository;
    private final TollPlazaRepository tollPlazaRepository;
    private final AdminService adminService;

    public TollRateService(TollRateRepository tollRateRepository,
                           TollPlazaRepository tollPlazaRepository,
                           AdminService adminService) {
        this.tollRateRepository = tollRateRepository;
        this.tollPlazaRepository = tollPlazaRepository;
        this.adminService = adminService;
    }

    public TollRate configureRate(String plazaId, Vehicle.VehicleType vehicleType, TollRate.TripType tripType, BigDecimal rate, LocalDate effectiveDate) {
        adminService.requireAdmin();

        if (plazaId == null || plazaId.trim().isEmpty()) {
            throw new InvalidOperationException("Plaza ID is required.");
        }
        TollPlaza plaza = tollPlazaRepository.findById(plazaId.trim())
                .orElseThrow(() -> new EntityNotFoundException("Plaza with ID '" + plazaId + "' not found."));

        if (vehicleType == null) {
            throw new InvalidOperationException("Vehicle type cannot be null.");
        }
        if (vehicleType.isEmergency()) {
            throw new InvalidOperationException("Emergency vehicles (Ambulance, Fire Engine) are exempt from toll rates.");
        }
        if (rate == null || rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Rate must be a positive amount greater than zero.");
        }
        if (effectiveDate == null) {
            throw new InvalidOperationException("Effective date cannot be null.");
        }

        BigDecimal roundedRate = TollCalculator.roundToNearestFive(rate);
        String rateId = IdGenerator.nextRateId();
        TollRate tollRate = new TollRate(rateId, plaza.getId(), vehicleType, tripType, roundedRate, effectiveDate);
        tollRateRepository.save(tollRate);

        if (tripType == TollRate.TripType.SINGLE) {
            BigDecimal returnRateAmt = TollCalculator.calculateReturnRate(roundedRate);
            String returnRateId = IdGenerator.nextRateId();
            TollRate returnTollRate = new TollRate(returnRateId, plaza.getId(), vehicleType, TollRate.TripType.RETURN, returnRateAmt, effectiveDate);
            tollRateRepository.save(returnTollRate);
        }

        return tollRate;
    }

    public TollRate getEffectiveRate(String plazaId, Vehicle.VehicleType vehicleType, TollRate.TripType tripType, LocalDate asOfDate) {
        return tollRateRepository.findEffectiveRate(plazaId, vehicleType, tripType, asOfDate)
                .orElseThrow(() -> new EntityNotFoundException("No effective toll rate found for Plaza: " + plazaId +
                        ", Vehicle: " + vehicleType + ", Trip: " + tripType + " as of " + asOfDate));
    }

    public List<TollRate> getRatesByPlaza(String plazaId) {
        return tollRateRepository.findByPlazaId(plazaId);
    }

    public List<TollRate> getAllRates() {
        return tollRateRepository.findAll();
    }
}
