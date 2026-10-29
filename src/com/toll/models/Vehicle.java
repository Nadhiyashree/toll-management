package com.toll.models;

public class Vehicle {
    public enum VehicleType {
        CAR("Car / Jeep / Van", false),
        VAN("Light Commercial Vehicle / Van", false),
        BUS("Bus", false),
        TRUCK("Truck (2-Axle)", false),
        MULTI_AXLE("Multi-Axle Vehicle (3+ Axle)", false),
        AMBULANCE("Ambulance (Emergency)", true),
        FIRE_ENGINE("Fire Engine (Emergency)", true);

        private final String displayName;
        private final boolean emergency;

        VehicleType(String displayName, boolean emergency) {
            this.displayName = displayName;
            this.emergency = emergency;
        }

        public String getDisplayName() {
            return displayName;
        }

        public boolean isEmergency() {
            return emergency;
        }
    }

    private String registrationNumber;
    private VehicleType vehicleType;
    private String ownerName;
    private String phoneNumber;
    private String fastagId;

    public Vehicle(String registrationNumber, VehicleType vehicleType, String ownerName, String phoneNumber) {
        this.registrationNumber = registrationNumber.toUpperCase().trim();
        this.vehicleType = vehicleType;
        this.ownerName = ownerName;
        this.phoneNumber = phoneNumber;
        this.fastagId = null;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public void setRegistrationNumber(String registrationNumber) {
        this.registrationNumber = registrationNumber.toUpperCase().trim();
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getFastagId() {
        return fastagId;
    }

    public void setFastagId(String fastagId) {
        this.fastagId = fastagId;
    }

    public boolean isEmergencyVehicle() {
        return vehicleType != null && vehicleType.isEmergency();
    }

    @Override
    public String toString() {
        return String.format("Vehicle[Reg: %s, Type: %s, Owner: %s, Phone: %s, FASTag: %s]",
                registrationNumber, vehicleType, ownerName, phoneNumber, fastagId != null ? fastagId : "None");
    }
}
