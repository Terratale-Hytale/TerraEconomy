package terratale.models;

import terratale.plugin.TerratalePlugin;
import java.io.File;
import java.sql.*;
import java.util.logging.Level;

public abstract class Model {
    
    protected static Connection connection;
    protected static Object logger;
    
    public static void initialize(File dataFolder, Object loggerInstance) {
        logger = loggerInstance;
        
        try {
            // Verificar si la configuración de MySQL está completa
            boolean useMySQL = isMySQLConfigured();
            
            if (useMySQL) {
                // Usar MySQL
                Class.forName("com.mysql.cj.jdbc.Driver");
                
                String url = "jdbc:mysql://" + TerratalePlugin.get().config().sqlConnectionIp + 
                           "/" + TerratalePlugin.get().config().sqlDatabaseName + 
                           "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
                
                logInfo("Connecting to MySQL database: " + TerratalePlugin.get().config().sqlConnectionIp + 
                       "/" + TerratalePlugin.get().config().sqlDatabaseName);
                connection = DriverManager.getConnection(url, 
                    TerratalePlugin.get().config().sqlUsername, 
                    TerratalePlugin.get().config().sqlPassword);
            } else {
                // Usar SQLite como fallback
                Class.forName("org.sqlite.JDBC");
                
                if (!dataFolder.exists()) {
                    boolean created = dataFolder.mkdirs();
                    logInfo("Data folder created: " + created);
                }
                
                File dbFile = new File(dataFolder, "terratale.db");
                String url = "jdbc:sqlite:" + dbFile.getAbsolutePath();
                
                logInfo("Connecting to SQLite database: " + dbFile.getAbsolutePath());
                connection = DriverManager.getConnection(url);
            }
            
            if (connection != null) {
                logInfo("Database connection established!");
                createTables();
            } else {
                logError("Failed to establish database connection!");
            }
        } catch (ClassNotFoundException e) {
            logError("Database JDBC driver not found: " + e.getMessage());
            e.printStackTrace();
        } catch (SQLException e) {
            logError("Failed to initialize database: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static boolean isMySQLConfigured() {
        try {
            String ip = TerratalePlugin.get().config().sqlConnectionIp;
            String dbName = TerratalePlugin.get().config().sqlDatabaseName;
            String username = TerratalePlugin.get().config().sqlUsername;
            String password = TerratalePlugin.get().config().sqlPassword;
            
            // Verificar que todos los campos estén configurados (no null y no vacíos)
            return ip != null && !ip.trim().isEmpty() &&
                   dbName != null && !dbName.trim().isEmpty() &&
                   username != null && !username.trim().isEmpty() &&
                   password != null; // La contraseña puede estar vacía
        } catch (Exception e) {
            logError("Error checking MySQL configuration: " + e.getMessage());
            return false;
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
