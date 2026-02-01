package terratale.models;

import terratale.plugin.TerratalePlugin;
import java.io.File;
import java.io.InputStream;
import java.sql.*;
import java.util.logging.Level;

public abstract class Model {
    
    protected static Connection connection;
    protected static Object logger;
    public static boolean isMySQL = false;
    
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
                isMySQL = useMySQL;
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
                   password != null && !password.trim().isEmpty(); // La contraseña puede estar vacía
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
        
        // Ejecutar migraciones desde archivos
        String dbType = isMySQL ? "mysql" : "sqlite";
        String[] migrations = {
            "users",
            "banks", 
            "bank_accounts",
            "add_key_to_bank_accounts",
            "bank_accounts_owners",
            "transactions",
            "bank_transactions",
            "account_invitations",
            "bank_invitations",
            "invoices",
            "schedule_payments",
            "schedule_logs"
        };
        
        for (String migration : migrations) {
            executeMigration(dbType + "_" + migration + ".sql");
        }
    }
    
    private static void executeMigration(String fileName) {
        if (connection == null) {
            logError("No hay conexión; no se puede ejecutar migración: " + fileName);
            return;
        }

        String path = "/migrations/" + fileName;

        try (InputStream is = Model.class.getResourceAsStream(path)) {
            if (is == null) {
                logError("Archivo de migración no encontrado: " + path);
                return; // seguir con la siguiente
            }

            String content = new String(is.readAllBytes());

            // Quitar comentarios de línea y líneas vacías
            String[] lines = content.split("\n");
            StringBuilder sqlBuilder = new StringBuilder();
            for (String line : lines) {
                line = line.trim();
                if (!line.isEmpty() && !line.startsWith("--")) {
                    sqlBuilder.append(line).append("\n");
                }
            }

            String sql = sqlBuilder.toString().trim();
            if (sql.isEmpty()) {
                logInfo("Migración vacía (skip): " + fileName);
                return;
            }

            boolean originalAutoCommit = connection.getAutoCommit();
            connection.setAutoCommit(false);

            try (Statement stmt = connection.createStatement()) {
                logInfo("Ejecutando migración: " + fileName);
                stmt.execute(sql);

                connection.commit();
                logInfo("Migración OK: " + fileName);

            } catch (SQLException e) {
                // rollback SOLO de esta migración
                try { connection.rollback(); } catch (SQLException ignored) {}

                // IMPORTANTE: no relanzar => no corta el resto
                logError("Migración FALLÓ (se continúa): " + fileName + " -> " + e.getMessage());

            } finally {
                try { connection.setAutoCommit(originalAutoCommit); } catch (SQLException ignored) {}
            }

        } catch (Exception e) {
            // También sin relanzar
            logError("Error leyendo/ejecutando migración (se continúa): " + fileName + " -> " + e.getMessage());
            e.printStackTrace();
        }
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

    public static String getDatabaseTranslation (String key) {
        if (isMySQL) {
            switch (key) {
                case "AUTOINCREMENT":
                    return "AUTO_INCREMENT";
                case "TEXT_TYPE":
                    return "TEXT";
                case "DATETIME_TYPE":
                    return "DATETIME";
                case "utf8mb4_general_ci":
                    return "utf8mb4_general_ci";
                default:
                    return key;
            }
        } else {
            switch (key) {
                case "AUTOINCREMENT":
                    return "AUTOINCREMENT";
                case "TEXT_TYPE":
                    return "TEXT";
                case "DATETIME_TYPE":
                    return "TEXT"; // SQLite stores datetime as TEXT
                case "utf8mb4_general_ci":
                    return "NOCASE";
                default:
                    return key;
            }
        }
    }
}
