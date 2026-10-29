package com.toll.services;

import com.toll.models.Lane;
import com.toll.models.TollPlaza;
import com.toll.repository.TollPlazaRepository;

public class LaneService {
    private final TollPlazaRepository tollPlazaRepository;
    private final AdminService adminService;

    public LaneService(TollPlazaRepository tollPlazaRepository, AdminService adminService) {
        this.tollPlazaRepository = tollPlazaRepository;
        this.adminService = adminService;
    }

    public Lane addLane(String plazaId, int laneNumber, Lane.LaneType laneType) {
        adminService.requireAdmin();

        if (laneNumber <= 0) {
            throw new InvalidOperationException("Lane number must be a positive integer.");
        }
        if (laneType == null) {
            throw new InvalidOperationException("Lane type cannot be null.");
        }

        TollPlaza plaza = tollPlazaRepository.findById(plazaId)
                .orElseThrow(() -> new EntityNotFoundException("Toll Plaza with ID " + plazaId + " not found."));

        if (plaza.getLaneByNumber(laneNumber) != null) {
            throw new InvalidOperationException("Lane number " + laneNumber + " already exists in plaza '" + plaza.getName() + "'.");
        }

        String laneId = IdGenerator.nextLaneId(plaza.getId(), laneNumber);
        Lane lane = new Lane(laneId, plaza.getId(), laneNumber, laneType, Lane.LaneStatus.OPEN);
        plaza.addLane(lane);
        tollPlazaRepository.save(plaza);
        return lane;
    }

    public void openLane(String plazaId, int laneNumber) {
        adminService.requireAdmin();

        TollPlaza plaza = tollPlazaRepository.findById(plazaId)
                .orElseThrow(() -> new EntityNotFoundException("Toll Plaza with ID " + plazaId + " not found."));

        Lane lane = plaza.getLaneByNumber(laneNumber);
        if (lane == null) {
            throw new EntityNotFoundException("Lane " + laneNumber + " not found in plaza '" + plaza.getName() + "'.");
        }

        lane.setLaneStatus(Lane.LaneStatus.OPEN);
        tollPlazaRepository.save(plaza);
    }

    public void closeLane(String plazaId, int laneNumber) {
        adminService.requireAdmin();

        TollPlaza plaza = tollPlazaRepository.findById(plazaId)
                .orElseThrow(() -> new EntityNotFoundException("Toll Plaza with ID " + plazaId + " not found."));

        Lane lane = plaza.getLaneByNumber(laneNumber);
        if (lane == null) {
            throw new EntityNotFoundException("Lane " + laneNumber + " not found in plaza '" + plaza.getName() + "'.");
        }

        if (lane.isProcessing()) {
            throw new InvalidOperationException("Cannot close Lane " + laneNumber + ": A vehicle is currently being processed in this lane.");
        }

        lane.setLaneStatus(Lane.LaneStatus.CLOSED);
        tollPlazaRepository.save(plaza);
    }

    public Lane getLane(String plazaId, int laneNumber) {
        TollPlaza plaza = tollPlazaRepository.findById(plazaId)
                .orElseThrow(() -> new EntityNotFoundException("Toll Plaza with ID " + plazaId + " not found."));

        Lane lane = plaza.getLaneByNumber(laneNumber);
        if (lane == null) {
            throw new EntityNotFoundException("Lane " + laneNumber + " not found in plaza '" + plaza.getName() + "'.");
        }
        return lane;
    }
}
