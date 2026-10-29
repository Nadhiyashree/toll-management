package com.toll.cli;

import com.toll.models.FASTagAccount;
import com.toll.services.AdminService;
import com.toll.services.EntityNotFoundException;
import com.toll.services.FASTagService;

import java.math.BigDecimal;
import java.util.List;

public class FASTagCLI {
    private final FASTagService fastagService;
    private final AdminService adminService;
    private final ConsoleReader reader;

    public FASTagCLI(FASTagService fastagService, AdminService adminService, ConsoleReader reader) {
        this.fastagService = fastagService;
        this.adminService = adminService;
        this.reader = reader;
    }

    public void displayMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [4] FASTag Account Management ---");
            System.out.println(" 1. View All FASTag Accounts");
            System.out.println(" 2. Issue New FASTag for Vehicle");
            System.out.println(" 3. Recharge FASTag Account (Rs. 100 - Rs. 10,000)");
            System.out.println(" 4. Check FASTag Balance & Status");
            System.out.println(" 5. Blacklist FASTag Account [Admin]");
            System.out.println(" 6. Un-blacklist FASTag Account [Admin]");
            System.out.println(" 7. Close FASTag Account [Admin]");
            System.out.println(" 8. Return to Main Menu");

            int c = reader.readInt(" Choice: ", 1, 8);
            try {
                switch (c) {
                    case 1:
                        List<FASTagAccount> tags = fastagService.getAllAccounts();
                        System.out.println("\n--- FASTag Accounts (" + tags.size() + ") ---");
                        for (FASTagAccount a : tags) {
                            System.out.printf(" Tag ID: %-10s | Reg: %-12s | Balance: Rs. %8.2f | Status: %-8s | Blacklisted: %s\n",
                                    a.getTagId(), a.getVehicleRegistrationNumber(), a.getBalance(), a.getStatus(),
                                    a.isBlacklisted() ? "YES [ALERT]" : "NO");
                        }
                        break;
                    case 2:
                        String reg = reader.readNonEmptyString(" Vehicle Registration Number: ", "Registration Number");
                        BigDecimal dep = reader.readBigDecimal(" Initial Deposit (Min Rs. 100): ", new BigDecimal("100"), new BigDecimal("50000"));
                        FASTagAccount acc = fastagService.issueFASTag(reg, dep);
                        System.out.println(" [✓] FASTag issued successfully! Tag ID: " + acc.getTagId() + " | Initial Balance: Rs. " + acc.getBalance() + " | Status: " + acc.getStatus());
                        break;
                    case 3:
                        String tagId = reader.readNonEmptyString(" Enter FASTag ID: ", "Tag ID");
                        BigDecimal amt = reader.readBigDecimal(" Enter Recharge Amount (Rs. 100 - Rs. 10,000): ",
                                FASTagService.MIN_RECHARGE, FASTagService.MAX_RECHARGE);
                        FASTagAccount recharged = fastagService.recharge(tagId, amt);
                        System.out.println(" [✓] Recharge successful!");
                        System.out.println("     Tag ID: " + recharged.getTagId() +
                                " | Updated Balance: Rs. " + recharged.getBalance() +
                                " | Status: " + recharged.getStatus());
                        break;
                    case 4:
                        String searchTag = reader.readNonEmptyString(" Enter FASTag ID or Reg Number: ", "Identifier");
                        FASTagAccount foundTag;
                        if (searchTag.toUpperCase().startsWith("FT-")) {
                            foundTag = fastagService.getAccount(searchTag);
                        } else {
                            foundTag = fastagService.findByRegistrationNumber(searchTag)
                                    .orElseThrow(() -> new EntityNotFoundException("No FASTag found for " + searchTag));
                        }
                        System.out.println(" [✓] " + foundTag);
                        break;
                    case 5:
                        adminService.requireAdmin();
                        String blkTag = reader.readNonEmptyString(" Enter FASTag ID to Blacklist: ", "Tag ID");
                        String reason = reader.readNonEmptyString(" Reason for Blacklisting: ", "Reason");
                        fastagService.blacklistTag(blkTag, reason);
                        System.out.println(" [✓] FASTag " + blkTag + " has been BLACKLISTED. It will be rejected at all plazas.");
                        break;
                    case 6:
                        adminService.requireAdmin();
                        String unblkTag = reader.readNonEmptyString(" Enter FASTag ID to Un-blacklist: ", "Tag ID");
                        fastagService.unblacklistTag(unblkTag);
                        System.out.println(" [✓] FASTag " + unblkTag + " has been un-blacklisted.");
                        break;
                    case 7:
                        adminService.requireAdmin();
                        String clsTag = reader.readNonEmptyString(" Enter FASTag ID to Close: ", "Tag ID");
                        fastagService.closeAccount(clsTag);
                        System.out.println(" [✓] FASTag " + clsTag + " has been CLOSED.");
                        break;
                    case 8:
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
