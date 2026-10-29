package com.toll.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class MonthlyPass {
    private String id;
    private String vehicleRegistrationNumber;
    private String plazaId;
    private Vehicle.VehicleType vehicleType;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal amountPaid;
    private boolean active;

    public MonthlyPass(String id, String vehicleRegistrationNumber, String plazaId, Vehicle.VehicleType vehicleType,
                       LocalDate startDate, LocalDate endDate, BigDecimal amountPaid) {
        this.id = id;
        this.vehicleRegistrationNumber = vehicleRegistrationNumber.toUpperCase().trim();
        this.plazaId = plazaId;
        this.vehicleType = vehicleType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.amountPaid = amountPaid;
        this.active = true;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getVehicleRegistrationNumber() {
        return vehicleRegistrationNumber;
    }

    public void setVehicleRegistrationNumber(String vehicleRegistrationNumber) {
        this.vehicleRegistrationNumber = vehicleRegistrationNumber.toUpperCase().trim();
    }

    public String getPlazaId() {
        return plazaId;
    }

    public void setPlazaId(String plazaId) {
        this.plazaId = plazaId;
    }

    public Vehicle.VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(Vehicle.VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public BigDecimal getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(BigDecimal amountPaid) {
        this.amountPaid = amountPaid;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isValidOn(LocalDate date) {
        if (!active) {
            return false;
        }
        return (date.isEqual(startDate) || date.isAfter(startDate)) &&
               (date.isEqual(endDate) || date.isBefore(endDate));
    }

    @Override
    public String toString() {
        return String.format("MonthlyPass[ID: %s, Reg: %s, Plaza: %s, Valid: %s to %s, Amount: Rs.%.2f, Active: %s]",
                id, vehicleRegistrationNumber, plazaId, startDate, endDate, amountPaid, active ? "YES" : "NO");
    }
}
