-- TS-CRTS MySQL Database Schema

CREATE DATABASE IF NOT EXISTS tscrts_db;
USE tscrts_db;

-- Users table
CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    role ENUM('manager', 'engineer', 'technician', 'it_staff', 'customer_support') NOT NULL,
    password_hash VARCHAR(255) NOT NULL
);

-- Customers table
CREATE TABLE IF NOT EXISTS customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(20) NOT NULL,
    company_name VARCHAR(100)
);

-- Devices table
CREATE TABLE IF NOT EXISTS devices (
    id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT NOT NULL,
    device_type VARCHAR(100) NOT NULL,
    ram VARCHAR(50),
    cpu VARCHAR(100),
    notes TEXT,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

-- Tickets table
CREATE TABLE IF NOT EXISTS tickets (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ticket_code VARCHAR(20) UNIQUE,
    device_id INT NOT NULL,
    assigned_user_id INT,
    category ENUM('sql_database', 'erp_installation', 'e_transformation', 'network', 'hardware') NOT NULL,
    status ENUM('pending', 'in_progress', 'completed', 'archived') NOT NULL DEFAULT 'pending',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    closed_at TIMESTAMP NULL,
    invoice_amount DECIMAL(10, 2),
    FOREIGN KEY (device_id) REFERENCES devices(id) ON DELETE CASCADE,
    FOREIGN KEY (assigned_user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- Ticket History table
CREATE TABLE IF NOT EXISTS ticket_history (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ticket_id INT NOT NULL,
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    note TEXT NOT NULL,
    FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE
);

-- Parts inventory table
CREATE TABLE IF NOT EXISTS parts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    stock_count INT NOT NULL DEFAULT 0
);

-- Ticket Parts (Join table)
CREATE TABLE IF NOT EXISTS ticket_parts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    ticket_id INT NOT NULL,
    part_id INT NOT NULL,
    quantity INT NOT NULL DEFAULT 1,
    FOREIGN KEY (ticket_id) REFERENCES tickets(id) ON DELETE CASCADE,
    FOREIGN KEY (part_id) REFERENCES parts(id) ON DELETE CASCADE
);

-- Insert dummy test users for all 5 roles (passwords are set to 'test1234' for all users except manager which is 'admin')
INSERT IGNORE INTO users (name, role, password_hash) VALUES 
('Admin Manager', 'manager', 'admin'),
('Ali Engineer', 'engineer', 'test1234'),
('Veli Technician', 'technician', 'test1234'),
('Hasan IT Support 1', 'it_staff', 'test1234'),
('Huseyin IT Support 2', 'it_staff', 'test1234'),
('Ayse Customer Support', 'customer_support', 'test1234');

-- Insert sample parts for testing inventory logic
INSERT IGNORE INTO parts (name, stock_count) VALUES
('8GB DDR4 RAM', 15),
('16GB DDR4 RAM', 10),
('500GB SATA SSD', 20),
('1TB NVMe SSD', 12),
('Cat6 Network Cable (1m)', 50),
('Cat6 Network Cable (5m)', 30),
('500W Power Supply', 8),
('Thermal Paste', 25);
