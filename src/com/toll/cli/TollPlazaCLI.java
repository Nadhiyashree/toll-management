package com.toll.cli;

import com.toll.models.Lane;
import com.toll.models.TollPlaza;
import com.toll.services.AdminService;
import com.toll.services.LaneService;
import com.toll.services.TollPlazaService;

import java.util.List;

public class TollPlazaCLI {
    private final TollPlazaService tollPlazaService;
    private final LaneService laneService;
    private final AdminService adminService;
    private final ConsoleReader reader;

    public TollPlazaCLI(TollPlazaService tollPlazaService, LaneService laneService, AdminService adminService, ConsoleReader reader) {
        this.tollPlazaService = tollPlazaService;
        this.laneService = laneService;
        this.adminService = adminService;
        this.reader = reader;
    }

    public void displayMenu() {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- [2] Toll Plaza and Lane Management ---");
            System.out.println(" 1. View All Toll Plazas & Lanes");
            System.out.println(" 2. Create New Toll Plaza [Admin]");
            System.out.println(" 3. Add Lane to Plaza [Admin]");
            System.out.println(" 4. Open Lane [Admin]");
            System.out.println(" 5. Close Lane [Admin]");
            System.out.println(" 6. Activate Toll Plaza [Admin]");
            System.out.println(" 7. Deactivate Toll Plaza [Admin]");
            System.out.println(" 8. Return to Main Menu");

            int c = reader.readInt(" Choice: ", 1, 8);
            try {
                switch (c) {
                    case 1:
                        viewAllPlazas();
                        break;
                    case 2:
                        adminService.requireAdmin();
                        String name = reader.readNonEmptyString(" Plaza Name: ", "Plaza Name");
                        String highway = reader.readNonEmptyString(" Highway Name: ", "Highway Name");
                        String loc = reader.readNonEmptyString(" Location: ", "Location");
                        TollPlaza p = tollPlazaService.createPlaza(name, highway, loc);
                        System.out.println(" [✓] Toll Plaza created successfully! ID: " + p.getId());
                        break;
                    case 3:
                        adminService.requireAdmin();
                        viewAllPlazas();
                        String pId = reader.readNonEmptyString(" Enter Plaza ID: ", "Plaza ID");
                        int laneNo = reader.readInt(" Enter Lane Number: ", 1, 50);
                        Lane.LaneType lType = reader.readEnum(" Lane Type: ", Lane.LaneType.class);
                        laneService.addLane(pId, laneNo, lType);
                        System.out.println(" [✓] Lane " + laneNo + " (" + lType + ") added successfully to plaza " + pId);
                        break;
                    case 4:
                        adminService.requireAdmin();
                        String pIdOpen = reader.readNonEmptyString(" Enter Plaza ID: ", "Plaza ID");
                        int laneOpen = reader.readInt(" Enter Lane Number to Open: ", 1, 50);
                        laneService.openLane(pIdOpen, laneOpen);
                        System.out.println(" [✓] Lane " + laneOpen + " is now OPEN.");
                        break;
                    case 5:
                        adminService.requireAdmin();
                        String pIdClose = reader.readNonEmptyString(" Enter Plaza ID: ", "Plaza ID");
                        int laneClose = reader.readInt(" Enter Lane Number to Close: ", 1, 50);
                        laneService.closeLane(pIdClose, laneClose);
                        System.out.println(" [✓] Lane " + laneClose + " is now CLOSED.");
                        break;
                    case 6:
                        adminService.requireAdmin();
                        String pIdAct = reader.readNonEmptyString(" Enter Plaza ID to Activate: ", "Plaza ID");
                        tollPlazaService.activatePlaza(pIdAct);
                        System.out.println(" [✓] Plaza " + pIdAct + " activated successfully and is now OPERATIONAL.");
                        break;
                    case 7:
                        adminService.requireAdmin();
                        String pIdDeact = reader.readNonEmptyString(" Enter Plaza ID to Deactivate: ", "Plaza ID");
                        tollPlazaService.deactivatePlaza(pIdDeact);
                        System.out.println(" [✓] Plaza " + pIdDeact + " is now DEACTIVATED.");
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

    public void viewAllPlazas() {
        List<TollPlaza> plazas = tollPlazaService.getAllPlazas();
        System.out.println("\n--- Toll Plazas Registered (" + plazas.size() + ") ---");
        if (plazas.isEmpty()) {
            System.out.println(" [!] No toll plazas found.");
            return;
        }
        for (TollPlaza p : plazas) {
            System.out.println(" Plaza ID: " + p.getId() + " | Name: " + p.getName() +
                    " | Highway: " + p.getHighwayName() + " | Operational: " + (p.isOperational() ? "YES [OPEN]" : "NO [INACTIVE]"));
            System.out.println("   Lanes (" + p.getLanes().size() + "):");
            for (Lane l : p.getLanes()) {
                System.out.println("     - Lane " + l.getLaneNumber() + " [" + l.getLaneType() +
                        "] Status: " + l.getLaneStatus() + (l.isProcessing() ? " (Active Vehicle in Lane)" : ""));
            }
        }
    }
}
