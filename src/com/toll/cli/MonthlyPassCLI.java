package com.toll.cli;

import com.toll.models.MonthlyPass;
import com.toll.models.Vehicle;
import com.toll.services.AdminService;
import com.toll.services.MonthlyPassService;
import com.toll.services.ReportService;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MonthlyPassCLI {
    private final MonthlyPassService monthlyPassService;
    private final ReportService reportService;
    private final AdminService adminService;
    private final ConsoleReader reader;

    public MonthlyPassCLI(MonthlyPassService monthlyPassService, ReportService reportService, AdminService adminService, ConsoleReader reader) {
        this.monthlyPassService = monthlyPassService;
        this.reportService = reportService;
        this.adminService = adminService;
        this.reader = reader;
    }

    public void displayMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [6] Monthly Pass Management ---");
            System.out.println(" 1. View All Monthly Passes");
            System.out.println(" 2. Issue Monthly Pass for Vehicle");
            System.out.println(" 3. View / Configure Monthly Pass Fees [Admin]");
            System.out.println(" 4. Return to Main Menu");

            int c = reader.readInt(" Choice: ", 1, 4);
            try {
                switch (c) {
                    case 1:
                        System.out.println(reportService.generateMonthlyPassesReport(null));
                        break;
                    case 2:
                        String reg = reader.readNonEmptyString(" Vehicle Registration Number: ", "Registration Number");
                        String pId = reader.readNonEmptyString(" Toll Plaza ID: ", "Plaza ID");
                        LocalDate start = reader.readDate(" Start Date (YYYY-MM-DD, blank for today): ", true);
                        int days = reader.readInt(" Duration in Days (default 30): ", 1, 365);
                        MonthlyPass pass = monthlyPassService.issuePass(reg, pId, start, days);
                        System.out.println(" [✓] Monthly Pass issued successfully!");
                        System.out.println("     Pass ID: " + pass.getId() + " | Vehicle: " + pass.getVehicleRegistrationNumber() +
                                " | Valid: " + pass.getStartDate() + " to " + pass.getEndDate() + " | Fee Paid: Rs. " + pass.getAmountPaid());
                        break;
                    case 3:
                        System.out.println("\n--- Current Pass Fee Configuration ---");
                        monthlyPassService.getPassFeeConfig().forEach((vType, fee) ->
                                System.out.printf("  * %-20s : Rs. %.2f\n", vType.getDisplayName(), fee));

                        if (adminService.isAdmin()) {
                            String resp = reader.readString("\n Would you like to update a pass fee? (y/n): ");
                            if (resp.equalsIgnoreCase("y")) {
                                Vehicle.VehicleType updateVt = reader.readEnum(" Select Vehicle Type: ", Vehicle.VehicleType.class);
                                BigDecimal newFee = reader.readBigDecimal(" Enter New Monthly Pass Fee (Rs.): ",
                                        new BigDecimal("100"), new BigDecimal("50000"));
                                monthlyPassService.configurePassFee(updateVt, newFee);
                                System.out.println(" [✓] Pass fee updated for " + updateVt + " to Rs. " + newFee);
                            }
                        }
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
