package com.example.models;

import java.io.File;
import java.sql.*;
import java.util.logging.Level;

public abstract class Model {
    
    protected static Connection connection;
    protected static Object logger;
    
    public static void initialize(File dataFolder, Object loggerInstance) {
        logger = loggerInstance;
        
        try {
            // Cargar el driver de SQLite
            Class.forName("org.sqlite.JDBC");
            
            if (!dataFolder.exists()) {
                boolean created = dataFolder.mkdirs();
                logInfo("Data folder created: " + created);
            }
            
            File dbFile = new File(dataFolder, "terratale.db");
            String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
            
            logInfo("Connecting to database: " + dbFile.getAbsolutePath());
            connection = DriverManager.getConnection(url);
            
            if (connection != null) {
                logInfo("Database connection established!");
                createTables();
            } else {
                logError("Failed to establish database connection!");
            }
        } catch (ClassNotFoundException e) {
            logError("SQLite JDBC driver not found: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            logError("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    protected static void createTables() {
        if (connection == null) {
            logError("Cannot create tables: connection is null");
            return;
        }
        
        // Crear tablas en el orden correcto por dependencias
        User.createTable();
        Bank.createTable();
        BankAccount.createTable();
        BankAccountOwner.createTable();
        Transaction.createTable();
        BankTransaction.createTable();
        AccountInvitation.createTable();
    }
    
    public static void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                logInfo("Database connection closed!");
            }
        } catch (SQLException e) {
            logError("Failed to close database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    protected static void logInfo(String message) {
        if (logger == null) {
            System.out.println("[INFO] " + message);
            return;
        }
        
        try {
            Object at = logger.getClass().getMethod("at", Level.class).invoke(logger, Level.INFO);
            at.getClass().getMethod("log", String.class).invoke(at, message);
        } catch (Exception e) {
            System.out.println("[INFO] " + message);
        }
    }
    
    protected static void logError(String message) {
        if (logger == null) {
            System.err.println("[ERROR] " + message);
            return;
        }
        
        try {
            Object at = logger.getClass().getMethod("at", Level.class).invoke(logger, Level.SEVERE);
            at.getClass().getMethod("log", String.class).invoke(at, message);
        } catch (Exception e) {
            System.err.println("[ERROR] " + message);
        }
    }
    
    protected static Connection getConnection() {
        return connection;
    }
}
