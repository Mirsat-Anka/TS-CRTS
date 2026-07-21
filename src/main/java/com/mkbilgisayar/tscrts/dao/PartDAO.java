package com.mkbilgisayar.tscrts.dao;

import com.mkbilgisayar.tscrts.model.Part;
import com.mkbilgisayar.tscrts.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class PartDAO {

    public Part findById(int id) {
        String sql = "SELECT * FROM parts WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToPart(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Part> findAll() {
        List<Part> parts = new ArrayList<>();
        String sql = "SELECT * FROM parts ORDER BY name ASC";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                parts.add(mapResultSetToPart(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return parts;
    }

    public boolean create(Part part) {
        String sql = "INSERT INTO parts (name, stock_count) VALUES (?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, part.getName());
            stmt.setInt(2, part.getStockCount());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        part.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Part part) {
        String sql = "UPDATE parts SET name = ?, stock_count = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, part.getName());
            stmt.setInt(2, part.getStockCount());
            stmt.setInt(3, part.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean decrementStock(int partId, int quantity) {
        String sql = "UPDATE parts SET stock_count = stock_count - ? WHERE id = ? AND stock_count >= ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, quantity);
            stmt.setInt(2, partId);
            stmt.setInt(3, quantity);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Part mapResultSetToPart(ResultSet rs) throws SQLException {
        return new Part(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getInt("stock_count")
        );
    }
}
