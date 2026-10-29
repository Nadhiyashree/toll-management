package com.toll.services;

import com.toll.models.Dispute;
import com.toll.models.TollTransaction;
import com.toll.repository.DisputeRepository;
import com.toll.repository.TollTransactionRepository;

import java.time.LocalDateTime;
import java.util.List;

public class DisputeService {
    private final DisputeRepository disputeRepository;
    private final TollTransactionRepository tollTransactionRepository;
    private final AdminService adminService;

    public DisputeService(DisputeRepository disputeRepository,
                          TollTransactionRepository tollTransactionRepository,
                          AdminService adminService) {
        this.disputeRepository = disputeRepository;
        this.tollTransactionRepository = tollTransactionRepository;
        this.adminService = adminService;
    }

    public Dispute raiseDispute(String transactionId, String reason, String description) {
        if (transactionId == null || transactionId.trim().isEmpty()) {
            throw new InvalidOperationException("Transaction ID is required to raise a dispute.");
        }
        TollTransaction txn = tollTransactionRepository.findById(transactionId.trim())
                .orElseThrow(() -> new EntityNotFoundException("Transaction with ID '" + transactionId + "' not found."));

        if (reason == null || reason.trim().isEmpty()) {
            throw new InvalidOperationException("Dispute reason cannot be empty.");
        }

        String disputeId = IdGenerator.nextDisputeId();
        Dispute dispute = new Dispute(disputeId, txn.getId(), reason.trim(), description != null ? description.trim() : "");
        return disputeRepository.save(dispute);
    }

    public Dispute markUnderReview(String disputeId) {
        adminService.requireAdmin();

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new EntityNotFoundException("Dispute with ID '" + disputeId + "' not found."));

        dispute.setStatus(Dispute.DisputeStatus.UNDER_REVIEW);
        return disputeRepository.save(dispute);
    }

    public Dispute resolveDispute(String disputeId, String resolutionNotes) {
        adminService.requireAdmin();

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new EntityNotFoundException("Dispute with ID '" + disputeId + "' not found."));

        if (resolutionNotes == null || resolutionNotes.trim().isEmpty()) {
            throw new InvalidOperationException("Resolution notes must be provided when resolving a dispute.");
        }

        dispute.setStatus(Dispute.DisputeStatus.RESOLVED);
        dispute.setResolutionNotes(resolutionNotes.trim());
        dispute.setResolvedAt(LocalDateTime.now());
        dispute.setResolvedBy(adminService.getCurrentUser().getUsername());
        return disputeRepository.save(dispute);
    }

    public Dispute rejectDispute(String disputeId, String rejectionReason) {
        adminService.requireAdmin();

        Dispute dispute = disputeRepository.findById(disputeId)
                .orElseThrow(() -> new EntityNotFoundException("Dispute with ID '" + disputeId + "' not found."));

        if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
            throw new InvalidOperationException("Rejection reason must be provided when rejecting a dispute.");
        }

        dispute.setStatus(Dispute.DisputeStatus.REJECTED);
        dispute.setResolutionNotes(rejectionReason.trim());
        dispute.setResolvedAt(LocalDateTime.now());
        dispute.setResolvedBy(adminService.getCurrentUser().getUsername());
        return disputeRepository.save(dispute);
    }

    public List<Dispute> getAllDisputes() {
        return disputeRepository.findAll();
    }

    public Dispute getDispute(String disputeId) {
        return disputeRepository.findById(disputeId)
                .orElseThrow(() -> new EntityNotFoundException("Dispute with ID '" + disputeId + "' not found."));
    }
}
