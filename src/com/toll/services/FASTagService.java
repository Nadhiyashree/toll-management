package com.toll.services;

import com.toll.models.FASTagAccount;
import com.toll.models.Vehicle;
import com.toll.repository.FASTagAccountRepository;
import com.toll.repository.VehicleRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public class FASTagService {
    public static final BigDecimal MIN_RECHARGE = new BigDecimal("100.00");
    public static final BigDecimal MAX_RECHARGE = new BigDecimal("10000.00");
    public static final BigDecimal MAX_BALANCE = new BigDecimal("50000.00");
    public static final BigDecimal MIN_ACTIVE_BALANCE = new BigDecimal("100.00");

    private final FASTagAccountRepository fastagAccountRepository;
    private final VehicleRepository vehicleRepository;
    private final AdminService adminService;

    public FASTagService(FASTagAccountRepository fastagAccountRepository,
                         VehicleRepository vehicleRepository,
                         AdminService adminService) {
        this.fastagAccountRepository = fastagAccountRepository;
        this.vehicleRepository = vehicleRepository;
        this.adminService = adminService;
    }

    public FASTagAccount issueFASTag(String registrationNumber, BigDecimal initialDeposit) {
        if (registrationNumber == null || registrationNumber.trim().isEmpty()) {
            throw new InvalidOperationException("Registration number cannot be empty.");
        }
        String cleanRegNo = registrationNumber.toUpperCase().trim();
        Vehicle vehicle = vehicleRepository.findByRegistrationNumber(cleanRegNo)
                .orElseThrow(() -> new EntityNotFoundException("Vehicle with registration number '" + cleanRegNo + "' is not registered."));

        Optional<FASTagAccount> existingTag = fastagAccountRepository.findByVehicleRegistrationNumber(cleanRegNo);
        if (existingTag.isPresent() && existingTag.get().getStatus() != FASTagAccount.AccountStatus.CLOSED) {
            throw new InvalidOperationException("Vehicle " + cleanRegNo + " already has an active or blocked FASTag account (" + existingTag.get().getTagId() + "). A vehicle can have at most one active tag.");
        }

        if (initialDeposit == null || initialDeposit.compareTo(MIN_RECHARGE) < 0) {
            throw new InvalidOperationException("Initial deposit must be at least Rs. " + MIN_RECHARGE + ".");
        }
        if (initialDeposit.compareTo(MAX_BALANCE) > 0) {
            throw new InvalidOperationException("Initial deposit cannot exceed Rs. " + MAX_BALANCE + ".");
        }

        String tagId = IdGenerator.nextTagId();
        FASTagAccount account = new FASTagAccount(tagId, cleanRegNo, initialDeposit);
        fastagAccountRepository.save(account);

        vehicle.setFastagId(tagId);
        vehicleRepository.save(vehicle);

        return account;
    }

    public FASTagAccount recharge(String tagId, BigDecimal amount) {
        if (tagId == null) {
            throw new InvalidOperationException("Tag ID is required.");
        }
        FASTagAccount account = fastagAccountRepository.findByTagId(tagId.trim())
                .orElseThrow(() -> new EntityNotFoundException("FASTag account with ID '" + tagId.trim() + "' not found."));

        if (account.getStatus() == FASTagAccount.AccountStatus.CLOSED) {
            throw new InvalidOperationException("Cannot recharge: FASTag account " + tagId + " is CLOSED.");
        }

        if (amount == null) {
            throw new InvalidOperationException("Recharge amount cannot be null.");
        }
        if (amount.compareTo(MIN_RECHARGE) < 0) {
            throw new InvalidOperationException("Recharge amount must be at least Rs. " + MIN_RECHARGE + ".");
        }
        if (amount.compareTo(MAX_RECHARGE) > 0) {
            throw new InvalidOperationException("Recharge amount cannot exceed Rs. " + MAX_RECHARGE + " per transaction.");
        }

        BigDecimal newBalance = account.getBalance().add(amount);
        if (newBalance.compareTo(MAX_BALANCE) > 0) {
            throw new InvalidOperationException("Recharge rejected: Resulting balance of Rs. " + newBalance + " exceeds the maximum allowed balance limit of Rs. " + MAX_BALANCE + ".");
        }

        account.setBalance(newBalance);
        account.setLastRechargedAt(LocalDateTime.now());

        if (account.getStatus() == FASTagAccount.AccountStatus.BLOCKED && newBalance.compareTo(MIN_ACTIVE_BALANCE) >= 0 && !account.isBlacklisted()) {
            account.setStatus(FASTagAccount.AccountStatus.ACTIVE);
        }

        return fastagAccountRepository.save(account);
    }

    public void blacklistTag(String tagId, String reason) {
        adminService.requireAdmin();

        FASTagAccount account = fastagAccountRepository.findByTagId(tagId.trim())
                .orElseThrow(() -> new EntityNotFoundException("FASTag account with ID '" + tagId.trim() + "' not found."));

        account.setBlacklisted(true);
        fastagAccountRepository.save(account);
    }

    public void unblacklistTag(String tagId) {
        adminService.requireAdmin();

        FASTagAccount account = fastagAccountRepository.findByTagId(tagId.trim())
                .orElseThrow(() -> new EntityNotFoundException("FASTag account with ID '" + tagId.trim() + "' not found."));

        account.setBlacklisted(false);
        if (account.getStatus() == FASTagAccount.AccountStatus.BLOCKED && account.getBalance().compareTo(MIN_ACTIVE_BALANCE) >= 0) {
            account.setStatus(FASTagAccount.AccountStatus.ACTIVE);
        }
        fastagAccountRepository.save(account);
    }

    public void closeAccount(String tagId) {
        adminService.requireAdmin();

        FASTagAccount account = fastagAccountRepository.findByTagId(tagId.trim())
                .orElseThrow(() -> new EntityNotFoundException("FASTag account with ID '" + tagId.trim() + "' not found."));

        account.setStatus(FASTagAccount.AccountStatus.CLOSED);
        fastagAccountRepository.save(account);

        vehicleRepository.findByRegistrationNumber(account.getVehicleRegistrationNumber()).ifPresent(v -> {
            if (tagId.equalsIgnoreCase(v.getFastagId())) {
                v.setFastagId(null);
                vehicleRepository.save(v);
            }
        });
    }

    public FASTagAccount getAccount(String tagId) {
        return fastagAccountRepository.findByTagId(tagId.trim())
                .orElseThrow(() -> new EntityNotFoundException("FASTag account with ID '" + tagId.trim() + "' not found."));
    }

    public Optional<FASTagAccount> findByRegistrationNumber(String regNo) {
        return fastagAccountRepository.findByVehicleRegistrationNumber(regNo.trim());
    }

    public List<FASTagAccount> getAllAccounts() {
        return fastagAccountRepository.findAll();
    }
}
