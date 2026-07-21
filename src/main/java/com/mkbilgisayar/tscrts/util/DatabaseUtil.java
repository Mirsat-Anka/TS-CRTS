package com.mkbilgisayar.tscrts.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseUtil {
    
    // Default connection details for MySQL
    // In a production app, these should be loaded from a configuration file or environment variables
    private static final String URL = "jdbc:mysql://localhost:3306/tscrts_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true";
    private static final String USER = "root";
    private static final String PASSWORD = "Ezgi2004**";

    // Private constructor to prevent instantiation
    private DatabaseUtil() {}

    private static boolean configPrinted = false;

    /**
     * Establishes and returns a connection to the database.
     * @return Connection object
     * @throws SQLException if a database access error occurs
     */
    public static Connection getConnection() throws SQLException {
        if (!configPrinted) {
            System.out.println("--- DB CONFIGURATION ---");
            System.out.println("URL: " + URL);
            System.out.println("USER: " + USER);
            System.out.println("------------------------");
            configPrinted = true;
        }
        try {
            // Explicitly load the MySQL JDBC driver (optional in newer JDBC versions, but good practice)
            Class.forName("com.mysql.cj.jdbc.Driver");
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (ClassNotFoundException e) {
            System.err.println("--- MYSQL DRIVER NOT FOUND ---");
            e.printStackTrace();
            throw new SQLException("MySQL JDBC Driver not found.", e);
        } catch (SQLException e) {
            System.err.println("--- DATABASE CONNECTION FAILED ---");
            System.err.println("Could not connect to MySQL at " + URL);
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Closes the connection gracefully.
     * @param connection The connection to close
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                System.err.println("Error closing the database connection: " + e.getMessage());
            }
        }
    }
}
