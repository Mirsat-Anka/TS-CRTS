package com.mkbilgisayar.tscrts.dao;

import com.mkbilgisayar.tscrts.model.Device;
import com.mkbilgisayar.tscrts.util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DeviceDAO {

    public Device findById(int id) {
        String sql = "SELECT * FROM devices WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return mapResultSetToDevice(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public List<Device> findByCustomerId(int customerId) {
        List<Device> devices = new ArrayList<>();
        String sql = "SELECT * FROM devices WHERE customer_id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, customerId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                devices.add(mapResultSetToDevice(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return devices;
    }

    public boolean create(Device device) {
        String sql = "INSERT INTO devices (customer_id, device_type, ram, cpu, notes) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, device.getCustomerId());
            stmt.setString(2, device.getDeviceType());
            stmt.setString(3, device.getRam());
            stmt.setString(4, device.getCpu());
            stmt.setString(5, device.getNotes());
            
            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        device.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean update(Device device) {
        String sql = "UPDATE devices SET customer_id = ?, device_type = ?, ram = ?, cpu = ?, notes = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, device.getCustomerId());
            stmt.setString(2, device.getDeviceType());
            stmt.setString(3, device.getRam());
            stmt.setString(4, device.getCpu());
            stmt.setString(5, device.getNotes());
            stmt.setInt(6, device.getId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private Device mapResultSetToDevice(ResultSet rs) throws SQLException {
        return new Device(
                rs.getInt("id"),
                rs.getInt("customer_id"),
                rs.getString("device_type"),
                rs.getString("ram"),
                rs.getString("cpu"),
                rs.getString("notes")
        );
    }
}
