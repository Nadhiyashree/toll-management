package com.toll.models;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TollRate {
    public enum TripType {
        SINGLE,
        RETURN
    }

    private String id;
    private String plazaId;
    private Vehicle.VehicleType vehicleType;
    private TripType tripType;
    private BigDecimal rate;
    private LocalDate effectiveDate;

    public TollRate(String id, String plazaId, Vehicle.VehicleType vehicleType, TripType tripType, BigDecimal rate, LocalDate effectiveDate) {
        this.id = id;
        this.plazaId = plazaId;
        this.vehicleType = vehicleType;
        this.tripType = tripType;
        this.rate = rate;
        this.effectiveDate = effectiveDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public TripType getTripType() {
        return tripType;
    }

    public void setTripType(TripType tripType) {
        this.tripType = tripType;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public void setRate(BigDecimal rate) {
        this.rate = rate;
    }

    public LocalDate getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(LocalDate effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    @Override
    public String toString() {
        return String.format("TollRate[ID: %s, Plaza: %s, Vehicle: %s, Trip: %s, Rate: Rs.%.2f, Effective: %s]",
                id, plazaId, vehicleType, tripType, rate, effectiveDate);
    }
}
