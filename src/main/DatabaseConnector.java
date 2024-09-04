package main;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {

    // JDBC URL, username and password of MySQL server
    private static final String JDBC_URL = "jdbc:mysql://localhost:3306/payrollms";
    private static final String JDBC_USER = "root";
    private static final String JDBC_PASSWORD = ""; // Updated to empty string if no password

    // Connection object
    private static Connection connection = null;

    // Static initializer to load MySQL JDBC driver
    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("Error loading JDBC driver: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Method to get the database connection
    public static Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            try {
                connection = DriverManager.getConnection(JDBC_URL, JDBC_USER, JDBC_PASSWORD);
                System.out.println("Connected to MySQL database.");
            } catch (SQLException e) {
                System.err.println("Error connecting to MySQL database: " + e.getMessage());
                e.printStackTrace();
                throw e; // rethrowing the exception after logging it
            }
        }
        return connection;
    }

    // Method to close the database connection
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Disconnected from MySQL database.");
            } catch (SQLException ex) {
                System.err.println("Error closing MySQL connection: " + ex.getMessage());
                ex.printStackTrace();
            }
        }
    }
}
