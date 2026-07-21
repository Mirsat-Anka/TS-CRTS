package com.mkbilgisayar.tscrts.dao;

import com.mkbilgisayar.tscrts.model.Ticket;
import com.mkbilgisayar.tscrts.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketDAO {

    public Ticket findById(int id) {
        String sql = "SELECT * FROM tickets WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToTicket(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Ticket findByTicketCode(String code) {
        String sql = "SELECT * FROM tickets WHERE ticket_code = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, code);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToTicket(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Ticket> findAll() {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT * FROM tickets ORDER BY created_at DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                tickets.add(mapResultSetToTicket(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tickets;
    }

    public List<Ticket> findByStatus(String status) {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT * FROM tickets WHERE status = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tickets.add(mapResultSetToTicket(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tickets;
    }

    public List<Ticket> findByAssignedUser(int userId) {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT * FROM tickets WHERE assigned_user_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tickets.add(mapResultSetToTicket(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tickets;
    }

    public List<Ticket> findByDevice(int deviceId) {
        List<Ticket> tickets = new ArrayList<>();
        String sql = "SELECT * FROM tickets WHERE device_id = ? ORDER BY created_at DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, deviceId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tickets.add(mapResultSetToTicket(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tickets;
    }

    public List<Ticket> findByDeviceIds(List<Integer> deviceIds) {
        List<Ticket> tickets = new ArrayList<>();
        if (deviceIds == null || deviceIds.isEmpty()) return tickets;
        
        StringBuilder sb = new StringBuilder("SELECT * FROM tickets WHERE device_id IN (");
        for (int i = 0; i < deviceIds.size(); i++) {
            sb.append(i > 0 ? ",?" : "?");
        }
        sb.append(") ORDER BY created_at DESC");
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sb.toString())) {
            for (int i = 0; i < deviceIds.size(); i++) {
                stmt.setInt(i + 1, deviceIds.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                tickets.add(mapResultSetToTicket(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tickets;
    }

    public int countAssignedInProgress(int userId) {
        String sql = "SELECT COUNT(*) FROM tickets WHERE assigned_user_id = ? AND status = 'in_progress'";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countAvailableInCategories(List<String> categories) {
        if (categories == null || categories.isEmpty()) return 0;
        StringBuilder sb = new StringBuilder("SELECT COUNT(*) FROM tickets WHERE status = 'pending' AND category IN (");
        for (int i = 0; i < categories.size(); i++) {
            sb.append(i > 0 ? ",?" : "?");
        }
        sb.append(")");
        
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sb.toString())) {
            for (int i = 0; i < categories.size(); i++) {
                stmt.setString(i + 1, categories.get(i));
            }
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public int countCompletedByUser(int userId) {
        String sql = "SELECT COUNT(*) FROM tickets WHERE assigned_user_id = ? AND status = 'completed'";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean create(Ticket ticket) {
        String sql = "INSERT INTO tickets (device_id, assigned_user_id, category, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, ticket.getDeviceId());
            if (ticket.getAssignedUserId() != null) {
                stmt.setInt(2, ticket.getAssignedUserId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setString(3, ticket.getCategory());
            stmt.setString(4, ticket.getStatus());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                int newId = -1;
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        newId = generatedKeys.getInt(1);
                        ticket.setId(newId);
                    }
                }
                
                // Generate and update ticket code
                if (newId != -1) {
                    String prefix;
                    switch (ticket.getCategory()) {
                        case "sql_database": prefix = "SQL"; break;
                        case "erp_installation": prefix = "ERP"; break;
                        case "e_transformation": prefix = "ETR"; break;
                        case "network": prefix = "NET"; break;
                        case "hardware": prefix = "HW"; break;
                        default: prefix = "TKT"; break;
                    }
                    String ticketCode = prefix + "-" + (newId + 1000);
                    ticket.setTicketCode(ticketCode);
                    
                    String updateSql = "UPDATE tickets SET ticket_code = ? WHERE id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                        updateStmt.setString(1, ticketCode);
                        updateStmt.setInt(2, newId);
                        updateStmt.executeUpdate();
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Ticket ticket) {
        String sql = "UPDATE tickets SET device_id = ?, assigned_user_id = ?, category = ?, status = ?, closed_at = ?, invoice_amount = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ticket.getDeviceId());
            if (ticket.getAssignedUserId() != null) {
                stmt.setInt(2, ticket.getAssignedUserId());
            } else {
                stmt.setNull(2, Types.INTEGER);
            }
            stmt.setString(3, ticket.getCategory());
            stmt.setString(4, ticket.getStatus());
            stmt.setTimestamp(5, ticket.getClosedAt());
            if (ticket.getInvoiceAmount() != null) {
                stmt.setBigDecimal(6, ticket.getInvoiceAmount());
            } else {
                stmt.setNull(6, Types.DECIMAL);
            }
            stmt.setInt(7, ticket.getId());
            
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Ticket mapResultSetToTicket(ResultSet rs) throws SQLException {
        Integer assignedUserId = rs.getInt("assigned_user_id");
        if (rs.wasNull()) {
            assignedUserId = null;
        }
        
        Ticket ticket = new Ticket(
                rs.getInt("id"),
                rs.getString("ticket_code"),
                rs.getInt("device_id"),
                assignedUserId,
                rs.getString("category"),
                rs.getString("status"),
                rs.getTimestamp("created_at"),
                rs.getTimestamp("closed_at")
        );
        ticket.setInvoiceAmount(rs.getBigDecimal("invoice_amount"));
        return ticket;
    }

    // ===== Reporting Queries =====

    /** Returns a Map of status -> count for tickets created in the given year/month */
    public java.util.Map<String, Integer> countByStatusThisMonth(int year, int month) {
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        map.put("pending", 0);
        map.put("in_progress", 0);
        map.put("completed", 0);
        map.put("archived", 0);
        String sql = "SELECT status, COUNT(*) AS cnt FROM tickets WHERE YEAR(created_at) = ? AND MONTH(created_at) = ? GROUP BY status";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, year);
            stmt.setInt(2, month);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                map.put(rs.getString("status"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    /** Returns a Map of category -> count for all tickets, sorted by count desc */
    public java.util.Map<String, Integer> countByCategory() {
        java.util.Map<String, Integer> map = new java.util.LinkedHashMap<>();
        String sql = "SELECT category, COUNT(*) AS cnt FROM tickets GROUP BY category ORDER BY cnt DESC";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                map.put(rs.getString("category"), rs.getInt("cnt"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return map;
    }

    /** Returns average resolution time in hours for completed/archived tickets that have closed_at set */
    public double averageResolutionHours() {
        String sql = "SELECT AVG(ABS(TIMESTAMPDIFF(HOUR, created_at, closed_at))) AS avg_hours FROM tickets WHERE status IN ('completed', 'archived') AND closed_at IS NOT NULL";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("avg_hours");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /** Returns the sum of invoice_amount for tickets archived in the given year/month */
    public java.math.BigDecimal totalInvoicedThisMonth(int year, int month) {
        String sql = "SELECT COALESCE(SUM(invoice_amount), 0) AS total FROM tickets WHERE status = 'archived' AND YEAR(closed_at) = ? AND MONTH(closed_at) = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, year);
            stmt.setInt(2, month);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getBigDecimal("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return java.math.BigDecimal.ZERO;
    }
}
