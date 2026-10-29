package com.toll.cli;

import com.toll.models.TollRate;
import com.toll.models.Vehicle;
import com.toll.services.AdminService;
import com.toll.services.TollCalculator;
import com.toll.services.TollRateService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class TollRateCLI {
    private final TollRateService tollRateService;
    private final AdminService adminService;
    private final ConsoleReader reader;

    public TollRateCLI(TollRateService tollRateService, AdminService adminService, ConsoleReader reader) {
        this.tollRateService = tollRateService;
        this.adminService = adminService;
        this.reader = reader;
    }

    public void displayMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [5] Toll Rate Management ---");
            System.out.println(" 1. View All Configured Toll Rates");
            System.out.println(" 2. View Rates for a Specific Toll Plaza");
            System.out.println(" 3. Configure Toll Rate (Admin only)");
            System.out.println(" 4. Return to Main Menu");

            int c = reader.readInt(" Choice: ", 1, 4);
            try {
                switch (c) {
                    case 1:
                        List<TollRate> allRates = tollRateService.getAllRates();
                        System.out.println("\n--- Configured Toll Rates (" + allRates.size() + ") ---");
                        for (TollRate r : allRates) {
                            System.out.printf(" ID: %-8s | Plaza: %-10s | Vehicle: %-12s | Trip: %-6s | Rate: Rs. %6.2f | Effective: %s\n",
                                    r.getId(), r.getPlazaId(), r.getVehicleType(), r.getTripType(), r.getRate(), r.getEffectiveDate());
                        }
                        break;
                    case 2:
                        String pId = reader.readNonEmptyString(" Enter Plaza ID: ", "Plaza ID");
                        List<TollRate> pRates = tollRateService.getRatesByPlaza(pId);
                        System.out.println("\n--- Toll Rates for Plaza " + pId + " (" + pRates.size() + ") ---");
                        for (TollRate r : pRates) {
                            System.out.printf(" Vehicle: %-12s | Trip: %-6s | Rate: Rs. %6.2f | Effective Date: %s\n",
                                    r.getVehicleType(), r.getTripType(), r.getRate(), r.getEffectiveDate());
                        }
                        break;
                    case 3:
                        adminService.requireAdmin();
                        String confPlaza = reader.readNonEmptyString(" Enter Plaza ID: ", "Plaza ID");
                        Vehicle.VehicleType vt = reader.readEnum(" Select Vehicle Type: ", Vehicle.VehicleType.class);
                        BigDecimal singleAmt = reader.readBigDecimal(" Enter Standard SINGLE Trip Rate (Rs.): ",
                                new BigDecimal("10.00"), new BigDecimal("5000.00"));
                        LocalDate effDate = reader.readDate(" Effective Date (YYYY-MM-DD, blank for today): ", true);

                        TollRate rate = tollRateService.configureRate(confPlaza, vt, TollRate.TripType.SINGLE, singleAmt, effDate);
                        System.out.println(" [✓] Rates successfully configured!");
                        System.out.println("     - SINGLE Rate: Rs. " + rate.getRate());
                        System.out.println("     - RETURN Rate: Rs. " + TollCalculator.calculateReturnRate(rate.getRate()) + " (Auto 50% discount applied)");
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
