package com.mkbilgisayar.tscrts.model;

import java.sql.Timestamp;

public class TicketHistory {
    private int id;
    private int ticketId;
    private Timestamp timestamp;
    private String note;

    public TicketHistory() {}

    public TicketHistory(int id, int ticketId, Timestamp timestamp, String note) {
        this.id = id;
        this.ticketId = ticketId;
        this.timestamp = timestamp;
        this.note = note;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getTicketId() { return ticketId; }
    public void setTicketId(int ticketId) { this.ticketId = ticketId; }

    public Timestamp getTimestamp() { return timestamp; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    @Override
    public String toString() {
        return "[" + timestamp + "] " + note;
    }
}
