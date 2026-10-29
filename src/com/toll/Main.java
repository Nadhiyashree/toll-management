package com.toll;

import com.toll.cli.*;
import com.toll.repository.*;
import com.toll.services.*;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        ConsoleReader reader = new ConsoleReader(new Scanner(System.in));

        // Initialize repositories
        AdminRepository adminRepository = new AdminRepository();
        TollPlazaRepository tollPlazaRepository = new TollPlazaRepository();
        VehicleRepository vehicleRepository = new VehicleRepository();
        FASTagAccountRepository fastagAccountRepository = new FASTagAccountRepository();
        TollRateRepository tollRateRepository = new TollRateRepository();
        MonthlyPassRepository monthlyPassRepository = new MonthlyPassRepository();
        TollTransactionRepository tollTransactionRepository = new TollTransactionRepository();
        PaymentRepository paymentRepository = new PaymentRepository();
        DisputeRepository disputeRepository = new DisputeRepository();

        // Initialize services
        AdminService adminService = new AdminService(adminRepository);
        TollPlazaService tollPlazaService = new TollPlazaService(tollPlazaRepository, tollRateRepository, adminService);
        LaneService laneService = new LaneService(tollPlazaRepository, adminService);
        VehicleService vehicleService = new VehicleService(vehicleRepository);
        FASTagService fastagService = new FASTagService(fastagAccountRepository, vehicleRepository, adminService);
        TollRateService tollRateService = new TollRateService(tollRateRepository, tollPlazaRepository, adminService);
        MonthlyPassService monthlyPassService = new MonthlyPassService(monthlyPassRepository, vehicleRepository, tollPlazaRepository, adminService);
        TollProcessingService tollProcessingService = new TollProcessingService(
                tollPlazaRepository, vehicleRepository, fastagAccountRepository,
                tollRateRepository, monthlyPassRepository, tollTransactionRepository, paymentRepository);
        PaymentService paymentService = new PaymentService(paymentRepository);
        DisputeService disputeService = new DisputeService(disputeRepository, tollTransactionRepository, adminService);
        ReportService reportService = new ReportService(
                tollTransactionRepository, paymentRepository, monthlyPassRepository,
                disputeRepository, tollPlazaRepository, fastagAccountRepository);

        // Preload rich sample demonstration data
        DemoDataService.initializeSampleData(adminService, tollPlazaService, laneService,
                tollRateService, vehicleService, fastagService, monthlyPassService);

        // Initialize entity CLI handlers
        AdminCLI adminCLI = new AdminCLI(adminService, reader);
        TollPlazaCLI tollPlazaCLI = new TollPlazaCLI(tollPlazaService, laneService, adminService, reader);
        VehicleCLI vehicleCLI = new VehicleCLI(vehicleService, reader);
        FASTagCLI fastagCLI = new FASTagCLI(fastagService, adminService, reader);
        TollRateCLI tollRateCLI = new TollRateCLI(tollRateService, adminService, reader);
        MonthlyPassCLI monthlyPassCLI = new MonthlyPassCLI(monthlyPassService, reportService, adminService, reader);
        TollCrossingCLI tollCrossingCLI = new TollCrossingCLI(tollPlazaService, tollProcessingService, reader);
        PaymentCLI paymentCLI = new PaymentCLI(paymentService, reader);
        DisputeCLI disputeCLI = new DisputeCLI(disputeService, reportService, adminService, reader);
        ReportCLI reportCLI = new ReportCLI(reportService, reader);

        boolean running = true;
        while (running) {
            System.out.println("\n====================================================================");
            System.out.println("               TOLL MANAGEMENT SYSTEM (FASTag) ");
            System.out.println("====================================================================");
            if (adminService.isLoggedIn()) {
                System.out.printf(" Logged in as: %s [%s]\n",
                        adminService.getCurrentUser().getFullName(), adminService.getCurrentUser().getRole());
            } else {
                System.out.println(" Current Session: Guest / Operator");
            }
            System.out.println("--------------------------------------------------------------------");
            System.out.println("  1. Admin Login / Session Management");
            System.out.println("  2. Toll Plaza & Lane Management");
            System.out.println("  3. Vehicle Management");
            System.out.println("  4. FASTag Account Management");
            System.out.println("  5. Toll Rate Management");
            System.out.println("  6. Monthly Pass Management");
            System.out.println("  7. Process Vehicle Crossing (Toll Booth)");
            System.out.println("  8. Payments & Disputes");
            System.out.println("  9. Reports & Audits");
            System.out.println(" 10. Exit System");
            System.out.println("====================================================================");

            int choice = reader.readInt(" Enter choice (1-10): ", 1, 10);
            try {
                switch (choice) {
                    case 1:
                        adminCLI.displayMenu();
                        break;
                    case 2:
                        tollPlazaCLI.displayMenu();
                        break;
                    case 3:
                        vehicleCLI.displayMenu();
                        break;
                    case 4:
                        fastagCLI.displayMenu();
                        break;
                    case 5:
                        tollRateCLI.displayMenu();
                        break;
                    case 6:
                        monthlyPassCLI.displayMenu();
                        break;
                    case 7:
                        tollCrossingCLI.displayMenu();
                        break;
                    case 8:
                        System.out.println("\n--- Payments & Disputes ---");
                        System.out.println(" 1. View Payment Transactions");
                        System.out.println(" 2. Disputes Management");
                        int subChoice = reader.readInt(" Choice (1-2): ", 1, 2);
                        if (subChoice == 1) {
                            paymentCLI.displayMenu();
                        } else {
                            disputeCLI.displayMenu();
                        }
                        break;
                    case 9:
                        reportCLI.displayMenu();
                        break;
                    case 10:
                        System.out.println("\n [✓] Thank you for using FASTag Toll Management System. Goodbye!\n");
                        running = false;
                        break;
                }
            } catch (NoSuchElementException e) {
                System.out.println("\n [✓] Input stream closed. Exiting Toll Management System.");
                running = false;
            } catch (Exception e) {
                System.out.println("\n [!] ERROR: " + e.getMessage());
                reader.pressEnterToContinue();
            }
        }
    }
}
