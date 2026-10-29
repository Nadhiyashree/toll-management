package com.toll.cli;

import com.toll.services.ReportService;

import java.time.LocalDate;

public class ReportCLI {
    private final ReportService reportService;
    private final ConsoleReader reader;

    public ReportCLI(ReportService reportService, ConsoleReader reader) {
        this.reportService = reportService;
        this.reader = reader;
    }

    public void displayMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [9] Comprehensive Reports & Analytics ---");
            System.out.println(" 1. Crossings & Revenue Summary (with optional filter)");
            System.out.println(" 2. Audit & Exception Events (Failures / Blacklist / Mismatches / Duplicates)");
            System.out.println(" 3. Monthly Passes Status Report");
            System.out.println(" 4. Disputes Summary Report");
            System.out.println(" 5. Full End-to-End Audit & Revenue Report");
            System.out.println(" 6. Return to Main Menu");

            int c = reader.readInt(" Choice: ", 1, 6);
            try {
                switch (c) {
                    case 1: {
                        String pId = reader.readString(" Filter by Plaza ID (press ENTER for all): ");
                        LocalDate start = reader.readDate(" Start Date (YYYY-MM-DD, press ENTER for all): ", false);
                        LocalDate end = reader.readDate(" End Date (YYYY-MM-DD, press ENTER for all): ", false);
                        System.out.println(reportService.generateCrossingsAndRevenueSummary(pId.isEmpty() ? null : pId, start, end));
                        break;
                    }
                    case 2: {
                        String pId = reader.readString(" Filter by Plaza ID (press ENTER for all): ");
                        System.out.println(reportService.generateExceptionsAndAuditReport(pId.isEmpty() ? null : pId, null, null));
                        break;
                    }
                    case 3:
                        System.out.println(reportService.generateMonthlyPassesReport(null));
                        break;
                    case 4:
                        System.out.println(reportService.generateDisputesReport());
                        break;
                    case 5:
                        System.out.println(reportService.generateCrossingsAndRevenueSummary(null, null, null));
                        System.out.println(reportService.generateExceptionsAndAuditReport(null, null, null));
                        System.out.println(reportService.generateMonthlyPassesReport(null));
                        System.out.println(reportService.generateDisputesReport());
                        break;
                    case 6:
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
