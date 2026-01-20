package terratale.models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Bank extends Model {
    
    private Integer id;
    private String name;
    private UUID ownerUuid;
    private double balance;
    private double withdrawFee;
    private double depositFee;
    private double transactionsFee;
    
    public Bank(String name, UUID ownerUuid) {
        this.name = name;
        this.ownerUuid = ownerUuid;
        this.balance = 0.0;
        this.withdrawFee = 0.0;
        this.depositFee = 0.0;
        this.transactionsFee = 0.0;
    }
    
    private Bank(Integer id, String name, UUID ownerUuid, double balance, 
                 double withdrawFee, double depositFee, double transactionsFee) {
        this.id = id;
        this.name = name;
        this.ownerUuid = ownerUuid;
        this.balance = balance;
        this.withdrawFee = withdrawFee;
        this.depositFee = depositFee;
        this.transactionsFee = transactionsFee;
    }
    
    protected static void createTable() {
        if (connection == null) {
            logError("Cannot create banks table: connection is null");
            return;
        }
        
        String createBanksTable = """
            CREATE TABLE IF NOT EXISTS banks (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                owner_uuid TEXT NOT NULL,
                balance REAL DEFAULT 0.0 NOT NULL,
                withdraw_fee REAL DEFAULT 0.0 NOT NULL,
                deposit_fee REAL DEFAULT 0.0 NOT NULL,
                transactions_fee REAL DEFAULT 0.0 NOT NULL,
                FOREIGN KEY(owner_uuid) REFERENCES users(uuid)
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createBanksTable);
            logInfo("Banks table created/verified!");
        } catch (SQLException e) {
            logError("Failed to create banks table: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public List<BankAccount> getAccounts() {
        return BankAccount.findByBank(this.id);
    }
    
    public static Bank find(int id) {
        if (connection == null) {
            logError("Cannot find bank: connection is null");
            return null;
        }
        
        String sql = "SELECT * FROM banks WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Bank(
                    rs.getInt("id"),
                    rs.getString("name"),
                    UUID.fromString(rs.getString("owner_uuid")),
                    rs.getDouble("balance"),
                    rs.getDouble("withdraw_fee"),
                    rs.getDouble("deposit_fee"),
                    rs.getDouble("transactions_fee")
                );
            }
        } catch (SQLException e) {
            logError("Failed to find bank: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static List<Bank> findByOwner(UUID ownerUuid) {
        List<Bank> banks = new ArrayList<>();
        if (connection == null) {
            logError("Cannot find banks: connection is null");
            return banks;
        }
        
        String sql = "SELECT * FROM banks WHERE owner_uuid = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, ownerUuid.toString());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                banks.add(new Bank(
                    rs.getInt("id"),
                    rs.getString("name"),
                    UUID.fromString(rs.getString("owner_uuid")),
                    rs.getDouble("balance"),
                    rs.getDouble("withdraw_fee"),
                    rs.getDouble("deposit_fee"),
                    rs.getDouble("transactions_fee")
                ));
            }
        } catch (SQLException e) {
            logError("Failed to find banks by owner: " + e.getMessage());
            e.printStackTrace();
        }
        
        return banks;
    }

    public static Bank findByName(String name) {
        if (connection == null) {
            logError("Cannot find banks: connection is null");
            return null;
        }
        
        String sql = "SELECT * FROM banks WHERE name = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, name);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Bank(
                    rs.getInt("id"),
                    rs.getString("name"),
                    UUID.fromString(rs.getString("owner_uuid")),
                    rs.getDouble("balance"),
                    rs.getDouble("withdraw_fee"),
                    rs.getDouble("deposit_fee"),
                    rs.getDouble("transactions_fee")
                );


            }
        } catch (SQLException e) {
            logError("Failed to find banks by name: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    public static List<Bank> findAll() {
        List<Bank> banks = new ArrayList<>();
        if (connection == null) {
            logError("Cannot find banks: connection is null");
            return banks;
        }
        
        String sql = "SELECT * FROM banks";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                banks.add(new Bank(
                    rs.getInt("id"),
                    rs.getString("name"),
                    UUID.fromString(rs.getString("owner_uuid")),
                    rs.getDouble("balance"),
                    rs.getDouble("withdraw_fee"),
                    rs.getDouble("deposit_fee"),
                    rs.getDouble("transactions_fee")
                ));
            }
        } catch (SQLException e) {
            logError("Failed to find all banks: " + e.getMessage());
            e.printStackTrace();
        }
        
        return banks;
    }
    
    public void save() {
        if (connection == null) {
            logError("Cannot save bank: connection is null");
            return;
        }
        
        if (id == null) {
            // Insert
            String sql = """
                INSERT INTO banks (name, owner_uuid, balance, withdraw_fee, deposit_fee, transactions_fee) 
                VALUES (?, ?, ?, ?, ?, ?)
            """;
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, name);
                pstmt.setString(2, ownerUuid.toString());
                pstmt.setDouble(3, balance);
                pstmt.setDouble(4, withdrawFee);
                pstmt.setDouble(5, depositFee);
                pstmt.setDouble(6, transactionsFee);
                pstmt.executeUpdate();
                
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    this.id = rs.getInt(1);
                }
            } catch (SQLException e) {
                logError("Failed to insert bank: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Update
            String sql = """
                UPDATE banks SET name = ?, owner_uuid = ?, balance = ?, 
                withdraw_fee = ?, deposit_fee = ?, transactions_fee = ? 
                WHERE id = ?
            """;
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, name);
                pstmt.setString(2, ownerUuid.toString());
                pstmt.setDouble(3, balance);
                pstmt.setDouble(4, withdrawFee);
                pstmt.setDouble(5, depositFee);
                pstmt.setDouble(6, transactionsFee);
                pstmt.setInt(7, id);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                logError("Failed to update bank: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    // Getters y Setters
    public Integer getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public UUID getOwnerUuid() { return ownerUuid; }
    public void setOwnerUuid(UUID ownerUuid) { this.ownerUuid = ownerUuid; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public double getWithdrawFee() { return withdrawFee; }
    public void setWithdrawFee(double withdrawFee) { this.withdrawFee = withdrawFee; }
    public double getDepositFee() { return depositFee; }
    public void setDepositFee(double depositFee) { this.depositFee = depositFee; }
    public double getTransactionsFee() { return transactionsFee; }
    public void setTransactionsFee(double transactionsFee) { this.transactionsFee = transactionsFee; }
    
    public void addBalance(double amount) { this.balance += amount; }
    public void removeBalance(double amount) { this.balance = Math.max(0, this.balance - amount); }
}
