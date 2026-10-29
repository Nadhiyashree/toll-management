package com.toll.services;

import com.toll.models.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public class DemoDataService {

    public static void initializeSampleData(
            AdminService adminService,
            TollPlazaService tollPlazaService,
            LaneService laneService,
            TollRateService tollRateService,
            VehicleService vehicleService,
            FASTagService fastagService,
            MonthlyPassService monthlyPassService) {

        adminService.login("admin", "admin123");

        try {
            LocalDate today = LocalDate.now();

            // 1. Create Toll Plaza 1: Sriperumbudur Toll Plaza
            TollPlaza plaza1 = tollPlazaService.createPlaza("Sriperumbudur Toll Plaza", "NH-48 Chennai-Bengaluru", "Sriperumbudur, TN");
            laneService.addLane(plaza1.getId(), 1, Lane.LaneType.FASTAG);
            laneService.addLane(plaza1.getId(), 2, Lane.LaneType.FASTAG);
            laneService.addLane(plaza1.getId(), 3, Lane.LaneType.CASH);
            laneService.addLane(plaza1.getId(), 4, Lane.LaneType.EMERGENCY);

            tollRateService.configureRate(plaza1.getId(), Vehicle.VehicleType.CAR, TollRate.TripType.SINGLE, new BigDecimal("85.00"), today.minusDays(30));
            tollRateService.configureRate(plaza1.getId(), Vehicle.VehicleType.VAN, TollRate.TripType.SINGLE, new BigDecimal("135.00"), today.minusDays(30));
            tollRateService.configureRate(plaza1.getId(), Vehicle.VehicleType.BUS, TollRate.TripType.SINGLE, new BigDecimal("280.00"), today.minusDays(30));
            tollRateService.configureRate(plaza1.getId(), Vehicle.VehicleType.TRUCK, TollRate.TripType.SINGLE, new BigDecimal("300.00"), today.minusDays(30));
            tollRateService.configureRate(plaza1.getId(), Vehicle.VehicleType.MULTI_AXLE, TollRate.TripType.SINGLE, new BigDecimal("450.00"), today.minusDays(30));

            tollPlazaService.activatePlaza(plaza1.getId());

            // 2. Create Toll Plaza 2: Kanchipuram Toll Plaza
            TollPlaza plaza2 = tollPlazaService.createPlaza("Kanchipuram Toll Plaza", "NH-48", "Kanchipuram, TN");
            laneService.addLane(plaza2.getId(), 1, Lane.LaneType.FASTAG);
            laneService.addLane(plaza2.getId(), 2, Lane.LaneType.CASH);
            laneService.addLane(plaza2.getId(), 3, Lane.LaneType.EMERGENCY);

            tollRateService.configureRate(plaza2.getId(), Vehicle.VehicleType.CAR, TollRate.TripType.SINGLE, new BigDecimal("65.00"), today.minusDays(30));
            tollRateService.configureRate(plaza2.getId(), Vehicle.VehicleType.VAN, TollRate.TripType.SINGLE, new BigDecimal("105.00"), today.minusDays(30));
            tollRateService.configureRate(plaza2.getId(), Vehicle.VehicleType.BUS, TollRate.TripType.SINGLE, new BigDecimal("220.00"), today.minusDays(30));
            tollRateService.configureRate(plaza2.getId(), Vehicle.VehicleType.TRUCK, TollRate.TripType.SINGLE, new BigDecimal("235.00"), today.minusDays(30));
            tollRateService.configureRate(plaza2.getId(), Vehicle.VehicleType.MULTI_AXLE, TollRate.TripType.SINGLE, new BigDecimal("360.00"), today.minusDays(30));

            tollPlazaService.activatePlaza(plaza2.getId());

            // 3. Register Vehicles and FASTags
            Vehicle v1 = vehicleService.registerVehicle("TN01AB1234", Vehicle.VehicleType.CAR, "Arun Kumar", "9876543210");
            fastagService.issueFASTag(v1.getRegistrationNumber(), new BigDecimal("1500.00"));

            Vehicle v2 = vehicleService.registerVehicle("TN02CD5678", Vehicle.VehicleType.CAR, "Priya Sharma", "9876543211");
            FASTagAccount tag2 = fastagService.issueFASTag(v2.getRegistrationNumber(), new BigDecimal("200.00"));
            tag2.setBalance(new BigDecimal("40.00"));
            tag2.setStatus(FASTagAccount.AccountStatus.BLOCKED);

            Vehicle v3 = vehicleService.registerVehicle("KA03EF9012", Vehicle.VehicleType.TRUCK, "Ramesh Logistics", "9876543212");
            fastagService.issueFASTag(v3.getRegistrationNumber(), new BigDecimal("5000.00"));

            Vehicle v4 = vehicleService.registerVehicle("TN04GH3456", Vehicle.VehicleType.VAN, "Suresh Cargo", "9876543213");
            FASTagAccount tag4 = fastagService.issueFASTag(v4.getRegistrationNumber(), new BigDecimal("2500.00"));
            fastagService.blacklistTag(tag4.getTagId(), "Suspicious fraudulent activity");

            Vehicle v5 = vehicleService.registerVehicle("TN05IJ7890", Vehicle.VehicleType.BUS, "State Express Travels", "9876543214");
            fastagService.issueFASTag(v5.getRegistrationNumber(), new BigDecimal("1000.00"));
            monthlyPassService.issuePass(v5.getRegistrationNumber(), plaza1.getId(), today.minusDays(5), 30);

            vehicleService.registerVehicle("TN09EM9999", Vehicle.VehicleType.AMBULANCE, "Apollo Hospital EMS", "9876543215");
            vehicleService.registerVehicle("TN07KL1111", Vehicle.VehicleType.CAR, "Vijay Sethupathi", "9876543216");

        } finally {
            adminService.logout();
        }
    }
}
