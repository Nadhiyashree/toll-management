package com.toll.services;

import com.toll.models.MonthlyPass;
import com.toll.models.TollPlaza;
import com.toll.models.Vehicle;
import com.toll.repository.MonthlyPassRepository;
import com.toll.repository.TollPlazaRepository;
import com.toll.repository.VehicleRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class MonthlyPassService {
    private final MonthlyPassRepository monthlyPassRepository;
    private final VehicleRepository vehicleRepository;
    private final TollPlazaRepository tollPlazaRepository;
    private final AdminService adminService;
    private final Map<Vehicle.VehicleType, BigDecimal> passFeeConfig = new EnumMap<>(Vehicle.VehicleType.class);

    public MonthlyPassService(MonthlyPassRepository monthlyPassRepository,
                              VehicleRepository vehicleRepository,
                              TollPlazaRepository tollPlazaRepository,
                              AdminService adminService) {
        this.monthlyPassRepository = monthlyPassRepository;
        this.vehicleRepository = vehicleRepository;
        this.tollPlazaRepository = tollPlazaRepository;
        this.adminService = adminService;

        passFeeConfig.put(Vehicle.VehicleType.CAR, new BigDecimal("1500.00"));
        passFeeConfig.put(Vehicle.VehicleType.VAN, new BigDecimal("2500.00"));
        passFeeConfig.put(Vehicle.VehicleType.BUS, new BigDecimal("4500.00"));
        passFeeConfig.put(Vehicle.VehicleType.TRUCK, new BigDecimal("5000.00"));
        passFeeConfig.put(Vehicle.VehicleType.MULTI_AXLE, new BigDecimal("7500.00"));
    }

    public void configurePassFee(Vehicle.VehicleType vehicleType, BigDecimal fee) {
        adminService.requireAdmin();
        if (vehicleType == null || vehicleType.isEmergency()) {
            throw new InvalidOperationException("Invalid vehicle type for pass fee configuration.");
        }
        if (fee == null || fee.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidOperationException("Pass fee must be greater than zero.");
        }
        passFeeConfig.put(vehicleType, TollCalculator.roundToNearestFive(fee));
    }

    public BigDecimal getPassFee(Vehicle.VehicleType vehicleType) {
        BigDecimal fee = passFeeConfig.get(vehicleType);
        if (fee == null) {
            return new BigDecimal("2000.00");
        }
        return fee;
    }

    public MonthlyPass issuePass(String registrationNumber, String plazaId, LocalDate startDate, int durationDays) {
        if (registrationNumber == null) {
            throw new InvalidOperationException("Vehicle registration number is required.");
        }
        String cleanRegNo = registrationNumber.toUpperCase().trim();
        Vehicle vehicle = vehicleRepository.findByRegistrationNumber(cleanRegNo)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle " + cleanRegNo + " is not registered."));

        if (vehicle.isEmergencyVehicle()) {
            throw new InvalidOperationException("Emergency vehicles are already exempt and do not need a pass.");
        }

        TollPlaza plaza = tollPlazaRepository.findById(plazaId)
                .orElseThrow(() -> new EntityNotFoundException("Plaza with ID " + plazaId + " not found."));

        if (startDate == null) {
            startDate = LocalDate.now();
        }
        if (durationDays < 1) {
            durationDays = 30;
        }
        LocalDate endDate = startDate.plusDays(durationDays);

        if (monthlyPassRepository.hasOverlappingPass(cleanRegNo, plaza.getId(), startDate, endDate)) {
            throw new InvalidOperationException("Cannot issue pass: Vehicle " + cleanRegNo + " already has an active overlapping pass for plaza '" + plaza.getName() + "'.");
        }

        BigDecimal fee = getPassFee(vehicle.getVehicleType());
        String passId = IdGenerator.nextPassId();
        MonthlyPass pass = new MonthlyPass(passId, cleanRegNo, plaza.getId(), vehicle.getVehicleType(), startDate, endDate, fee);
        return monthlyPassRepository.save(pass);
    }

    public Optional<MonthlyPass> getValidPass(String registrationNumber, String plazaId, LocalDate date) {
        return monthlyPassRepository.findValidPass(registrationNumber, plazaId, date);
    }

    public List<MonthlyPass> getAllPasses() {
        return monthlyPassRepository.findAll();
    }

    public Map<Vehicle.VehicleType, BigDecimal> getPassFeeConfig() {
        return passFeeConfig;
    }
}
