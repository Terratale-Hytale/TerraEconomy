package terratale.database;

import java.io.File;
import java.sql.*;
import java.util.UUID;
import java.util.logging.Level;

public class DatabaseManager {
    
    private Connection connection;
    private final File dataFolder;
    private final Object logger;
    
    public DatabaseManager(File dataFolder, Object logger) {
        this.dataFolder = dataFolder;
        this.logger = logger;
    }
    
    public void initialize() {
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
    
    private void createTables() {
        if (connection == null) {
            logError("Cannot create tables: connection is null");
            return;
        }

        java.util.ArrayList<String> migrations = new java.util.ArrayList<>();
        
        migrations.add("""
            CREATE TABLE IF NOT EXISTS users (
                uuid TEXT PRIMARY KEY,
                username TEXT NOT NULL,
                money REAL DEFAULT 1000.0 NOT NULL,
                last_login INTEGER
            )
        """);

        migrations.add("""
            CREATE TABLE IF NOT EXISTS banks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                owner_uuid TEXT NOT NULL,
                balance REAL DEFAULT 0.0 NOT NULL,
                withdraw_fee REAL DEFAULT 0.0 NOT NULL,
                deposit_fee REAL DEFAULT 0.0 NOT NULL,
                transfer_fee REAL DEFAULT 0.0 NOT NULL,
                FOREIGN KEY(owner_uuid) REFERENCES users(uuid),
                UNIQUE(name)
            )
        """);

            migrations.add("""
                CREATE TABLE IF NOT EXISTS bank_accounts (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    account_number TEXT NOT NULL,
                    bank_id INTEGER NOT NULL,
                    balance REAL DEFAULT 0.0 NOT NULL,
                    withdraw_fee REAL DEFAULT 0.0,
                    deposit_fee REAL DEFAULT 0.0,
                    transfer_fee REAL DEFAULT 0.0,
                    FOREIGN KEY(bank_id) REFERENCES banks(id),
                    FOREIGN KEY(owner_uuid) REFERENCES users(uuid)
                )
            """);

            migrations.add("""
                CREATE TABLE IF NOT EXISTS bank_accounts_owners (
                    account_id INTEGER NOT NULL,
                    owner_uuid TEXT NOT NULL,
                    FOREIGN KEY(account_id) REFERENCES bank_accounts(id),
                    FOREIGN KEY(owner_uuid) REFERENCES users(uuid),
                    PRIMARY KEY(account_id, owner_uuid)
                )
            """);

            migrations.add("""
                CREATE TABLE IF NOT EXISTS transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    user_uuid TEXT,
                    account_id INTEGER,
                    type TEXT NOT NULL,
                    amount REAL NOT NULL,
                    timestamp INTEGER,
                    FOREIGN KEY(account_id) REFERENCES bank_accounts(id),
                    FOREIGN KEY(user_uuid) REFERENCES users(uuid)
                )
            """);

            migrations.add("""
                CREATE TABLE IF NOT EXISTS bank_transactions (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    bank_id INTEGER,
                    type TEXT NOT NULL,
                    amount REAL NOT NULL,
                    user_uuid TEXT NOT NULL,
                    timestamp INTEGER,
                    FOREIGN KEY(bank_id) REFERENCES banks(id),
                    FOREIGN KEY(user_uuid) REFERENCES users(uuid)
                )
                """);

            migrations.add("""
                CREATE TABLE IF NOT EXISTS account_invitations(
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    account_id INTEGER NOT NULL,
                    invited_uuid TEXT NOT NULL,
                    inviter_uuid TEXT NOT NULL,
                    timestamp INTEGER,
                    FOREIGN KEY(account_id) REFERENCES bank_accounts(id),
                    FOREIGN KEY(invited_uuid) REFERENCES users(uuid),
                    FOREIGN KEY(inviter_uuid) REFERENCES users(uuid)
                )
                """);
        
        try (Statement stmt = connection.createStatement()) {
            for (String migration : migrations) {
                stmt.execute(migration);
            }
            logInfo("Users table created/verified!");
        } catch (SQLException e) {
            logError("Failed to create tables: " + e.getMessage());
        if (connection == null) {
            logError("Cannot create/update user: connection is null");
            return;
        }
        
            e.printStackTrace();
        }
    }
    
    public void createOrUpdateUser(UUID uuid, String username) {
        String sql = """
            INSERT INTO users (uuid, username, money, last_login) 
            VALUES (?, ?, 0.0, ?)
            ON CONFLICT(uuid) DO UPDATE SET 
                username = excluded.username,
                last_login = excluded.last_login
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, username);
            pstmt.setLong(3, System.currentTimeMillis());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logError("Failed to create/update user: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public double getMoney(UUID uuid) {
        if (connection == null) {
            logError("Cannot get money: connection is null");
            return 0.0;
        }
        
        String sql = "SELECT money FROM users WHERE uuid = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return rs.getDouble("money");
            }
        } catch (SQLException e) {
            logError("Failed to get money: " + e.getMessage());
            e.printStackTrace();
        }
        
        return 0.0;
    }
    
    public void setMoney(UUID uuid, double amount) {
        if (connection == null) {
            logError("Cannot set money: connection is null");
            return;
        }
        
        String sql = "UPDATE users SET money = ? WHERE uuid = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, amount);
            pstmt.setString(2, uuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logError("Failed to set money: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void addMoney(UUID uuid, double amount) {
        double currentMoney = getMoney(uuid);
        setMoney(uuid, currentMoney + amount);
    }
    
    public void removeMoney(UUID uuid, double amount) {
        double currentMoney = getMoney(uuid);
        setMoney(uuid, Math.max(0, currentMoney - amount));
    }
    
    public void close() {
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
    
    private void logInfo(String message) {
        try {
            logger.getClass().getMethod("at", Level.class).invoke(logger, Level.INFO);
            logger.getClass().getMethod("log", String.class).invoke(logger, message);
        } catch (Exception e) {
            System.out.println("[INFO] " + message);
        }
    }
    
    private void logError(String message) {
        try {
            logger.getClass().getMethod("at", Level.class).invoke(logger, Level.SEVERE);
            logger.getClass().getMethod("log", String.class).invoke(logger, message);
        } catch (Exception e) {
            System.err.println("[ERROR] " + message);
        }
    }
}
