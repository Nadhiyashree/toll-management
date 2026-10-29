package com.toll.cli;

import com.toll.models.Dispute;
import com.toll.services.AdminService;
import com.toll.services.DisputeService;
import com.toll.services.ReportService;

public class DisputeCLI {
    private final DisputeService disputeService;
    private final ReportService reportService;
    private final AdminService adminService;
    private final ConsoleReader reader;

    public DisputeCLI(DisputeService disputeService, ReportService reportService, AdminService adminService, ConsoleReader reader) {
        this.disputeService = disputeService;
        this.reportService = reportService;
        this.adminService = adminService;
        this.reader = reader;
    }

    public void displayMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [8] Disputes Management ---");
            System.out.println(" 1. View All Disputes");
            System.out.println(" 2. Raise Dispute against Transaction");
            System.out.println(" 3. Mark Dispute Under Review [Admin]");
            System.out.println(" 4. Resolve Dispute [Admin]");
            System.out.println(" 5. Reject Dispute [Admin]");
            System.out.println(" 6. Return to Previous Menu");

            int c = reader.readInt(" Choice: ", 1, 6);
            try {
                switch (c) {
                    case 1:
                        System.out.println(reportService.generateDisputesReport());
                        break;
                    case 2:
                        String txnId = reader.readNonEmptyString(" Transaction ID to Dispute: ", "Transaction ID");
                        String reason = reader.readNonEmptyString(" Dispute Reason (e.g. Overcharged, Tag mismatch): ", "Reason");
                        String desc = reader.readString(" Additional Description: ");
                        Dispute d = disputeService.raiseDispute(txnId, reason, desc);
                        System.out.println(" [✓] Dispute raised successfully! Dispute ID: " + d.getId() + " | Status: " + d.getStatus());
                        break;
                    case 3:
                        adminService.requireAdmin();
                        String dIdReview = reader.readNonEmptyString(" Dispute ID: ", "Dispute ID");
                        disputeService.markUnderReview(dIdReview);
                        System.out.println(" [✓] Dispute " + dIdReview + " marked as UNDER_REVIEW.");
                        break;
                    case 4:
                        adminService.requireAdmin();
                        String dIdRes = reader.readNonEmptyString(" Dispute ID to Resolve: ", "Dispute ID");
                        String resNotes = reader.readNonEmptyString(" Resolution Notes: ", "Resolution Notes");
                        disputeService.resolveDispute(dIdRes, resNotes);
                        System.out.println(" [✓] Dispute " + dIdRes + " marked as RESOLVED.");
                        break;
                    case 5:
                        adminService.requireAdmin();
                        String dIdRej = reader.readNonEmptyString(" Dispute ID to Reject: ", "Dispute ID");
                        String rejReason = reader.readNonEmptyString(" Rejection Reason: ", "Rejection Reason");
                        disputeService.rejectDispute(dIdRej, rejReason);
                        System.out.println(" [✓] Dispute " + dIdRej + " marked as REJECTED.");
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
