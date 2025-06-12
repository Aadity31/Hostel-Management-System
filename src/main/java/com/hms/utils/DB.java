package com.hms.utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.Level;
import java.util.logging.Logger;



public class DB {
    private static final Logger LOGGER = Logger.getLogger(DB.class.getName());
    public static Connection connect() {
        Connection conn = null;
        try {
            Class.forName("com.mysql.cj.jdbc.Driver"); // Important line
            String url = "jdbc:mysql://localhost:3306/hms";
            String user = "root";
            String password = ""; // Agar password hai to woh daalo
            conn = DriverManager.getConnection(url, user, password);
            System.out.println("Connected to database successfully.");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Database connection failed", e);
        }
        return conn;
    }
}