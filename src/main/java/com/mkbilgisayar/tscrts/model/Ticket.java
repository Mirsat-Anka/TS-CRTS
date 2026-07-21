package com.mkbilgisayar.tscrts.model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Ticket {
    private int id;
    private String ticketCode;
    private int deviceId;
    private Integer assignedUserId; // Can be null
    private String category; // 'sql_database', 'erp_installation', 'e_transformation', 'network', 'hardware'
    private String status; // 'pending', 'in_progress', 'completed', 'archived'
    private Timestamp createdAt;
    private Timestamp closedAt;
    private BigDecimal invoiceAmount;

    public Ticket() {}

    public Ticket(int id, String ticketCode, int deviceId, Integer assignedUserId, String category, String status, Timestamp createdAt, Timestamp closedAt) {
        this.id = id;
        this.ticketCode = ticketCode;
        this.deviceId = deviceId;
        this.assignedUserId = assignedUserId;
        this.category = category;
        this.status = status;
        this.createdAt = createdAt;
        this.closedAt = closedAt;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTicketCode() { return ticketCode; }
    public void setTicketCode(String ticketCode) { this.ticketCode = ticketCode; }

    public int getDeviceId() { return deviceId; }
    public void setDeviceId(int deviceId) { this.deviceId = deviceId; }

    public Integer getAssignedUserId() { return assignedUserId; }
    public void setAssignedUserId(Integer assignedUserId) { this.assignedUserId = assignedUserId; }

    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getClosedAt() { return closedAt; }
    public void setClosedAt(Timestamp closedAt) { this.closedAt = closedAt; }

    public BigDecimal getInvoiceAmount() { return invoiceAmount; }
    public void setInvoiceAmount(BigDecimal invoiceAmount) { this.invoiceAmount = invoiceAmount; }

    @Override
    public String toString() {
        return "Ticket #" + id + " - " + status.toUpperCase(java.util.Locale.ENGLISH).replace("_", " ");
    }
}
