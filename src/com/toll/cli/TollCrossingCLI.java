package com.toll.cli;

import com.toll.models.CrossingResult;
import com.toll.models.Lane;
import com.toll.models.TollPlaza;
import com.toll.models.Vehicle;
import com.toll.services.TollPlazaService;
import com.toll.services.TollProcessingService;

import java.util.List;

public class TollCrossingCLI {
    private final TollPlazaService tollPlazaService;
    private final TollProcessingService tollProcessingService;
    private final ConsoleReader reader;

    public TollCrossingCLI(TollPlazaService tollPlazaService, TollProcessingService tollProcessingService, ConsoleReader reader) {
        this.tollPlazaService = tollPlazaService;
        this.tollProcessingService = tollProcessingService;
        this.reader = reader;
    }

    public void displayMenu() {
        System.out.println("\n====================================================================");
        System.out.println("            [7] TOLL BOOTH — PROCESS VEHICLE CROSSING");
        System.out.println("====================================================================");

        List<TollPlaza> plazas = tollPlazaService.getOperationalPlazas();
        if (plazas.isEmpty()) {
            System.out.println(" [!] No operational toll plazas available. Plazas must have at least 2 lanes and rates configured to be activated.");
            reader.pressEnterToContinue();
            return;
        }

        System.out.println(" Operational Toll Plazas:");
        for (int i = 0; i < plazas.size(); i++) {
            System.out.println(" [" + (i + 1) + "] " + plazas.get(i).getName() + " (" + plazas.get(i).getId() + ")");
        }
        int pChoice = reader.readInt(" Select Plaza (1-" + plazas.size() + "): ", 1, plazas.size());
        TollPlaza selectedPlaza = plazas.get(pChoice - 1);

        System.out.println("\n Lanes available at " + selectedPlaza.getName() + ":");
        List<Lane> lanes = selectedPlaza.getLanes();
        for (Lane l : lanes) {
            System.out.println("  - Lane " + l.getLaneNumber() + " [" + l.getLaneType() + "] Status: " + l.getLaneStatus() + (l.isProcessing() ? " [BUSY]" : ""));
        }

        int laneNumber = reader.readInt(" Enter Lane Number: ", 1, 50);
        String vehicleIdentifier = reader.readNonEmptyString(" Enter Vehicle Registration Number or FASTag ID: ", "Identifier");

        System.out.println("\n [?] Does the physically observed vehicle match registered type? Or select observed type:");
        System.out.println("  [1] Observed matches registered vehicle profile");
        System.out.println("  [2] Discrepancy observed (Select observed vehicle type)");
        int mismatchChoice = reader.readInt(" Choice (1-2): ", 1, 2);

        Vehicle.VehicleType observedType = null;
        if (mismatchChoice == 2) {
            observedType = reader.readEnum(" Select Observed Vehicle Type: ", Vehicle.VehicleType.class);
        }

        System.out.println("\n >>> Processing vehicle crossing...");
        CrossingResult result = tollProcessingService.processCrossing(selectedPlaza.getId(), laneNumber, vehicleIdentifier, observedType);

        System.out.println("\n====================================================================");
        System.out.println("                    CROSSING TRANSACTION RECEIPT");
        System.out.println("====================================================================");
        System.out.printf(" Barrier Status     : %s\n", result.isBarrierOpened() ? ">>> [OPEN - PASSAGE GRANTED] <<<" : ">>> [CLOSED - BARRIER BLOCKED] <<<");
        System.out.printf(" Transaction Status : %s\n", result.getStatus());
        System.out.printf(" Transaction ID     : %s\n", result.getTransactionId() != null ? result.getTransactionId() : "FAILED-ATTEMPT");
        System.out.printf(" Vehicle Reg No     : %s\n", result.getRegistrationNumber());
        System.out.printf(" Plaza & Lane       : %s | Lane %d [%s]\n", result.getPlazaName(), result.getLaneNumber(), result.getLaneType());
        System.out.printf(" Toll Amount Charged: Rs. %.2f\n", result.getTollAmount());
        System.out.printf(" Payment Method     : %s\n", result.getPaymentMethod());
        System.out.printf(" System Note        : %s\n", result.getMessage());

        if (result.isFlaggedForReview()) {
            System.out.println(" [!] AUDIT NOTICE   : Transaction flagged for manual review due to vehicle type mismatch!");
            System.out.println("     Details        : " + result.getReviewReason());
        }

        if (result.isRedirectedToCash()) {
            System.out.println("\n [ACTION REQUIRED]  : FASTag cannot be processed. Direct vehicle to CASH Lane.");
        }
        System.out.println("====================================================================");

        reader.pressEnterToContinue();
    }
}
