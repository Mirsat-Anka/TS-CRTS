package com.mkbilgisayar.tscrts.dao;

import com.mkbilgisayar.tscrts.model.User;
import com.mkbilgisayar.tscrts.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User findByName(String name) {
        String sql = "SELECT * FROM users WHERE name = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToUser(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<User> findAll() {
        List<User> users = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                users.add(mapResultSetToUser(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return users;
    }

    public List<String[]> getITStaffStatus() {
        List<String[]> statusList = new ArrayList<>();
        String sql = "SELECT u.name AS staff_name, MAX(t.ticket_code) AS ticket_code, MAX(c.name) AS customer_name " +
                     "FROM users u " +
                     "LEFT JOIN tickets t ON t.id = ( " +
                     "    SELECT id FROM tickets WHERE assigned_user_id = u.id AND status = 'in_progress' ORDER BY id DESC LIMIT 1 " +
                     ") " +
                     "LEFT JOIN devices d ON t.device_id = d.id " +
                     "LEFT JOIN customers c ON d.customer_id = c.id " +
                     "WHERE u.role = 'it_staff' " +
                     "GROUP BY u.name " +
                     "ORDER BY u.name ASC";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String staffName = rs.getString("staff_name");
                String ticketCode = rs.getString("ticket_code");
                String customerName = rs.getString("customer_name");
                
                if (ticketCode == null) {
                    statusList.add(new String[]{staffName, "Available", "available"});
                } else {
                    String text = ticketCode + " (" + (customerName != null ? customerName : "Unknown") + ")";
                    statusList.add(new String[]{staffName, text, "in_progress"});
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return statusList;
    }

    public boolean create(User user) {
        String sql = "INSERT INTO users (name, role, password_hash) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getRole());
            stmt.setString(3, user.getPasswordHash());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        user.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(User user) {
        String sql = "UPDATE users SET name = ?, role = ?, password_hash = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, user.getName());
            stmt.setString(2, user.getRole());
            stmt.setString(3, user.getPasswordHash());
            stmt.setInt(4, user.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean delete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        return new User(
                rs.getInt("id"),
                rs.getString("name"),
                rs.getString("role"),
                rs.getString("password_hash")
        );
    }
}
