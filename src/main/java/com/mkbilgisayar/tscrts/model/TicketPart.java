package com.mkbilgisayar.tscrts.model;

public class TicketPart {
    private int id;
    private int ticketId;
    private int partId;
    private int quantity;

    public TicketPart() {}

    public TicketPart(int id, int ticketId, int partId, int quantity) {
        this.id = id;
        this.ticketId = ticketId;
        this.partId = partId;
        this.quantity = quantity;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTicketId() { return ticketId; }
    public void setTicketId(int ticketId) { this.ticketId = ticketId; }

    public int getPartId() { return partId; }
    public void setPartId(int partId) { this.partId = partId; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    @Override
    public String toString() {
        return "TicketPart: Part ID " + partId + " x" + quantity + " for Ticket ID " + ticketId;
    }
}
