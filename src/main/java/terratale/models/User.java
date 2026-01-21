package terratale.models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class User extends Model {
    
    private UUID uuid;
    private String username;
    private long lastLogin;
    private double money;
    
    public User(UUID uuid, String username) {
        this.uuid = uuid;
        this.username = username;
        this.money = 1000.0;
        this.lastLogin = System.currentTimeMillis();
    }
    
    private User(UUID uuid, String username, double money, long lastLogin) {
        this.uuid = uuid;
        this.username = username;
        this.money = money;
        this.lastLogin = lastLogin;
    }
    
    // Crear la tabla de usuarios
    protected static void createTable() {
        if (connection == null) {
            logError("Cannot create users table: connection is null");
            return;
        }
        
        String createUsersTable = """
            CREATE TABLE IF NOT EXISTS users (
                uuid TEXT PRIMARY KEY,
                username TEXT NOT NULL,
                money REAL DEFAULT 0.0 NOT NULL,
                last_login INTEGER
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createUsersTable);
            logInfo("Users table created/verified!");
        } catch (SQLException e) {
            logError("Failed to create users table: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Buscar o crear un usuario
    public static User findOrCreate(UUID uuid, String username) {
        User user = find(uuid);
        
        if (user == null) {
            user = new User(uuid, username);
            user.save();
        } else {
            // Actualizar username y lastLogin
            user.setUsername(username);
            user.setLastLogin(System.currentTimeMillis());
            user.save();
        }
        
        return user;
    }
    
    // Buscar un usuario por UUID
    public static User find(UUID uuid) {
        if (connection == null) {
            logError("Cannot find user: connection is null");
            return null;
        }
        
        String sql = "SELECT * FROM users WHERE uuid = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new User(
                    UUID.fromString(rs.getString("uuid")),
                    rs.getString("username"),
                    rs.getDouble("money"),
                    rs.getLong("last_login")
                );
            }
        } catch (SQLException e) {
            logError("Failed to find user: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Buscar un usuario por username
    public static User findByUsername(String username) {
        if (connection == null) {
            logError("Cannot find user: connection is null");
            return null;
        }
        
        String sql = "SELECT * FROM users WHERE username = ? COLLATE NOCASE";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, username);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new User(
                    UUID.fromString(rs.getString("uuid")),
                    rs.getString("username"),
                    rs.getDouble("money"),
                    rs.getLong("last_login")
                );
            }
        } catch (SQLException e) {
            logError("Failed to find user by username: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Guardar o actualizar el usuario
    public void save() {
        if (connection == null) {
            logError("Cannot save user: connection is null");
            return;
        }
        
        String sql = """
            INSERT INTO users (uuid, username, money, last_login) 
            VALUES (?, ?, ?, ?)
            ON CONFLICT(uuid) DO UPDATE SET 
                username = excluded.username,
                money = excluded.money,
                last_login = excluded.last_login
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, username);
            pstmt.setDouble(3, money);
            pstmt.setLong(4, lastLogin);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logError("Failed to save user: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Guardar cambios en el dinero del usuario
    public void saveMoney() {
        if (connection == null) {
            logError("Cannot save user money: connection is null");
            return;
        }
        
        String sql = "UPDATE users SET money = ? WHERE uuid = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setDouble(1, money);
            pstmt.setString(2, uuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logError("Failed to save user money: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void delete() {
        if (connection == null) {
            logError("Cannot delete user: connection is null");
            return;
        }
        
        String sql = "DELETE FROM users WHERE uuid = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logInfo("User deleted successfully: " + uuid);
            } else {
                logError("No user found with uuid: " + uuid);
            }
        } catch (SQLException e) {
            logError("Failed to delete user: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Obtener cuentas bancarias del usuario
    public List<BankAccount> getBankAccounts() {
        List<Integer> accountIds = BankAccountOwner.getAccountsByOwner(this.uuid);
        List<BankAccount> accounts = new ArrayList<>();
        
        for (Integer accountId : accountIds) {
            BankAccount account = BankAccount.find(accountId);
            if (account != null) {
                accounts.add(account);
            }
        }
        
        return accounts;
    }
    
    // Obtener bancos del usuario
    public List<Bank> getBanks() {
        return Bank.findByOwner(this.uuid);
    }
    
    // Calcular balance total del usuario (suma de todas sus cuentas)
    public double getTotalBalance() {
        double total = 0.0;
        List<BankAccount> accounts = getBankAccounts();
        
        for (BankAccount account : accounts) {
            total += account.getBalance();
        }
        
        return total;
    }
    
    // Getters y Setters
    public UUID getUuid() {
        return uuid;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }
    
    public long getLastLogin() {
        return lastLogin;
    }
    
    public void setLastLogin(long lastLogin) {
        this.lastLogin = lastLogin;
    }
}
