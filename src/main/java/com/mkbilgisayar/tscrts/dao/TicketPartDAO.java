package com.mkbilgisayar.tscrts.dao;

import com.mkbilgisayar.tscrts.model.TicketPart;
import com.mkbilgisayar.tscrts.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TicketPartDAO {

    public List<TicketPart> findByTicketId(int ticketId) {
        List<TicketPart> ticketParts = new ArrayList<>();
        String sql = "SELECT * FROM ticket_parts WHERE ticket_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, ticketId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ticketParts.add(mapResultSetToTicketPart(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return ticketParts;
    }

    public boolean create(TicketPart ticketPart) {
        String sql = "INSERT INTO ticket_parts (ticket_id, part_id, quantity) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, ticketPart.getTicketId());
            stmt.setInt(2, ticketPart.getPartId());
            stmt.setInt(3, ticketPart.getQuantity());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        ticketPart.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private TicketPart mapResultSetToTicketPart(ResultSet rs) throws SQLException {
        return new TicketPart(
                rs.getInt("id"),
                rs.getInt("ticket_id"),
                rs.getInt("part_id"),
                rs.getInt("quantity")
        );
    }
}
