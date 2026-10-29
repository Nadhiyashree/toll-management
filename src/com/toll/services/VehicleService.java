package com.toll.services;

import com.toll.models.Vehicle;
import com.toll.repository.VehicleRepository;

import java.util.List;

public class VehicleService {
    private final VehicleRepository vehicleRepository;

    public VehicleService(VehicleRepository vehicleRepository) {
        this.vehicleRepository = vehicleRepository;
    }

    public Vehicle registerVehicle(String registrationNumber, Vehicle.VehicleType vehicleType, String ownerName, String phoneNumber) {
        if (registrationNumber == null || registrationNumber.trim().isEmpty()) {
            throw new InvalidOperationException("Registration number cannot be empty.");
        }
        String cleanRegNo = registrationNumber.toUpperCase().trim();
        if (vehicleRepository.existsByRegistrationNumber(cleanRegNo)) {
            throw new InvalidOperationException("Vehicle with registration number '" + cleanRegNo + "' is already registered.");
        }
        if (vehicleType == null) {
            throw new InvalidOperationException("Vehicle type cannot be null.");
        }
        if (ownerName == null || ownerName.trim().isEmpty()) {
            throw new InvalidOperationException("Owner name cannot be empty.");
        }
        if (phoneNumber == null || phoneNumber.trim().length() < 10) {
            throw new InvalidOperationException("Valid contact phone number (at least 10 digits) is required.");
        }

        Vehicle vehicle = new Vehicle(cleanRegNo, vehicleType, ownerName.trim(), phoneNumber.trim());
        return vehicleRepository.save(vehicle);
    }

    public Vehicle getVehicle(String registrationNumber) {
        if (registrationNumber == null) {
            throw new EntityNotFoundException("Registration number is required.");
        }
        return vehicleRepository.findByRegistrationNumber(registrationNumber.trim())
                .orElseThrow(() -> new EntityNotFoundException("Vehicle with registration number '" + registrationNumber.toUpperCase().trim() + "' not found."));
    }

    public Vehicle getVehicleByTagId(String tagId) {
        if (tagId == null) {
            throw new EntityNotFoundException("FASTag ID is required.");
        }
        return vehicleRepository.findByFastagId(tagId.trim())
                .orElseThrow(() -> new EntityNotFoundException("No vehicle linked to FASTag ID '" + tagId.trim() + "'."));
    }

    public List<Vehicle> getAllVehicles() {
        return vehicleRepository.findAll();
    }
}
