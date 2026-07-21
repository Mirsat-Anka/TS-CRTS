package com.mkbilgisayar.tscrts.model;

public class Device {
    private int id;
    private int customerId;
    private String deviceType;
    private String ram;
    private String cpu;
    private String notes;

    public Device() {}

    public Device(int id, int customerId, String deviceType, String ram, String cpu, String notes) {
        this.id = id;
        this.customerId = customerId;
        this.deviceType = deviceType;
        this.ram = ram;
        this.cpu = cpu;
        this.notes = notes;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getCustomerId() { return customerId; }
    public void setCustomerId(int customerId) { this.customerId = customerId; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getRam() { return ram; }
    public void setRam(String ram) { this.ram = ram; }

    public String getCpu() { return cpu; }
    public void setCpu(String cpu) { this.cpu = cpu; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    @Override
    public String toString() {
        return deviceType + " (" + (cpu != null ? cpu : "Unknown CPU") + ")";
    }
}
