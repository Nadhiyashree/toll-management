package com.toll.cli;

import com.toll.models.Vehicle;
import com.toll.services.VehicleService;

import java.util.List;

public class VehicleCLI {
    private final VehicleService vehicleService;
    private final ConsoleReader reader;

    public VehicleCLI(VehicleService vehicleService, ConsoleReader reader) {
        this.vehicleService = vehicleService;
        this.reader = reader;
    }

    public void displayMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [3] Vehicle Management ---");
            System.out.println(" 1. View All Registered Vehicles");
            System.out.println(" 2. Register New Vehicle");
            System.out.println(" 3. Search Vehicle by Registration Number");
            System.out.println(" 4. Return to Main Menu");

            int c = reader.readInt(" Choice: ", 1, 4);
            try {
                switch (c) {
                    case 1:
                        List<Vehicle> list = vehicleService.getAllVehicles();
                        System.out.println("\n--- Registered Vehicles (" + list.size() + ") ---");
                        for (Vehicle v : list) {
                            System.out.printf(" Reg: %-12s | Type: %-10s | Owner: %-15s | Phone: %-12s | FASTag: %s\n",
                                    v.getRegistrationNumber(), v.getVehicleType(), v.getOwnerName(), v.getPhoneNumber(),
                                    v.getFastagId() != null ? v.getFastagId() : "Not Issued");
                        }
                        break;
                    case 2:
                        String reg = reader.readNonEmptyString(" Vehicle Registration Number: ", "Registration Number");
                        Vehicle.VehicleType vt = reader.readEnum(" Select Vehicle Type: ", Vehicle.VehicleType.class);
                        String owner = reader.readNonEmptyString(" Owner Full Name: ", "Owner Name");
                        String phone = reader.readNonEmptyString(" Owner Phone Number: ", "Phone Number");
                        Vehicle v = vehicleService.registerVehicle(reg, vt, owner, phone);
                        System.out.println(" [✓] Vehicle registered successfully: " + v.getRegistrationNumber());
                        break;
                    case 3:
                        String searchReg = reader.readNonEmptyString(" Enter Registration Number: ", "Registration Number");
                        Vehicle found = vehicleService.getVehicle(searchReg);
                        System.out.println(" [✓] " + found);
                        break;
                    case 4:
                        back = true;
                        break;
                }
            } catch (Exception e) {
                System.out.println("\n [!] ERROR: " + e.getMessage());
            }
            if (!back) reader.pressEnterToContinue();
        }
    }
}
