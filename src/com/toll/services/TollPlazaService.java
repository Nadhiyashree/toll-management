package com.toll.services;

import com.toll.models.TollPlaza;
import com.toll.repository.TollPlazaRepository;
import com.toll.repository.TollRateRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class TollPlazaService {
    private final TollPlazaRepository tollPlazaRepository;
    private final TollRateRepository tollRateRepository;
    private final AdminService adminService;

    public TollPlazaService(TollPlazaRepository tollPlazaRepository,
                            TollRateRepository tollRateRepository,
                            AdminService adminService) {
        this.tollPlazaRepository = tollPlazaRepository;
        this.tollRateRepository = tollRateRepository;
        this.adminService = adminService;
    }

    public TollPlaza createPlaza(String name, String highwayName, String location) {
        adminService.requireAdmin();

        if (name == null || name.trim().isEmpty()) {
            throw new InvalidOperationException("Plaza name cannot be empty.");
        }
        if (highwayName == null || highwayName.trim().isEmpty()) {
            throw new InvalidOperationException("Highway name cannot be empty.");
        }
        if (location == null || location.trim().isEmpty()) {
            throw new InvalidOperationException("Location cannot be empty.");
        }
        if (tollPlazaRepository.existsByName(name.trim())) {
            throw new InvalidOperationException("A plaza with name '" + name + "' already exists.");
        }

        String id = IdGenerator.nextPlazaId();
        TollPlaza plaza = new TollPlaza(id, name.trim(), highwayName.trim(), location.trim());
        return tollPlazaRepository.save(plaza);
    }

    public void activatePlaza(String plazaId) {
        adminService.requireAdmin();

        TollPlaza plaza = tollPlazaRepository.findById(plazaId)
                .orElseThrow(() -> new EntityNotFoundException("Toll Plaza with ID " + plazaId + " not found."));

        if (plaza.getLanes().size() < 2) {
            throw new InvalidOperationException("Cannot activate plaza: At least 2 lanes are required before a plaza can become operational (currently has " + plaza.getLanes().size() + ").");
        }

        LocalDate today = LocalDate.now();
        boolean hasAllRates = tollRateRepository.hasRatesForSupportedVehicleTypes(plaza.getId(), today);
        if (!hasAllRates) {
            throw new InvalidOperationException("Cannot activate plaza: Toll rates must be configured for all supported vehicle types (CAR, VAN, BUS, TRUCK, MULTI_AXLE) for both SINGLE and RETURN trips before activation.");
        }

        plaza.setOperational(true);
        tollPlazaRepository.save(plaza);
    }

    public void deactivatePlaza(String plazaId) {
        adminService.requireAdmin();

        TollPlaza plaza = tollPlazaRepository.findById(plazaId)
                .orElseThrow(() -> new EntityNotFoundException("Toll Plaza with ID " + plazaId + " not found."));

        plaza.setOperational(false);
        tollPlazaRepository.save(plaza);
    }

    public TollPlaza getPlaza(String id) {
        return tollPlazaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Toll Plaza with ID " + id + " not found."));
    }

    public List<TollPlaza> getAllPlazas() {
        return tollPlazaRepository.findAll();
    }

    public List<TollPlaza> getOperationalPlazas() {
        return tollPlazaRepository.findAll().stream()
                .filter(TollPlaza::isOperational)
                .collect(Collectors.toList());
    }
}
