package terratale.models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Transaction extends Model {
    
    private Integer id;
    private int accountId;
    private String type;
    private double amount;
    private long timestamp;
    private String userUuid;
    
    public Transaction(int accountId, String type, double amount, String userUuid) {
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.timestamp = System.currentTimeMillis();
        this.userUuid = userUuid;
    }
    
    private Transaction(Integer id, int accountId, String type, double amount, long timestamp, String userUuid) {
        this.id = id;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.timestamp = timestamp;
        this.userUuid = userUuid;
    }
    
    public static Transaction find(int id) {
        if (connection == null) {
            logError("Cannot find transaction: connection is null");
            return null;
        }
        
        String sql = "SELECT * FROM transactions WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Transaction(
                    rs.getInt("id"),
                    rs.getInt("account_id"),
                    rs.getString("type"),
                    rs.getDouble("amount"),
                    rs.getLong("timestamp"),
                    rs.getString("user_uuid")
                );
            }
        } catch (SQLException e) {
            logError("Failed to find transaction: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static List<Transaction> findByAccount(int accountId) {
        List<Transaction> transactions = new ArrayList<>();
        if (connection == null) {
            logError("Cannot find transactions: connection is null");
            return transactions;
        }
        
        String sql = "SELECT * FROM transactions WHERE account_id = ? ORDER BY timestamp DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(new Transaction(
                    rs.getInt("id"),
                    rs.getInt("account_id"),
                    rs.getString("type"),
                    rs.getDouble("amount"),
                    rs.getLong("timestamp"),
                    rs.getString("user_uuid")
                ));
            }
        } catch (SQLException e) {
            logError("Failed to find transactions by account: " + e.getMessage());
            e.printStackTrace();
        }
        
        return transactions;
    }
    
    public void save() {
        if (connection == null) {
            logError("Cannot save transaction: connection is null");
            return;
        }
        
        if (id == null) {
            // Insert
            String sql = """
                INSERT INTO transactions (account_id, type, amount, timestamp, user_uuid) 
                VALUES (?, ?, ?, ?, ?)
            """;
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, accountId);
                pstmt.setString(2, type);
                pstmt.setDouble(3, amount);
                pstmt.setLong(4, timestamp);
                pstmt.setString(5, userUuid);
                pstmt.executeUpdate();
                
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    this.id = rs.getInt(1);
                }
            } catch (SQLException e) {
                logError("Failed to insert transaction: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    public void delete() {
        if (connection == null) {
            logError("Cannot delete transaction: connection is null");
            return;
        }
        
        if (id == null) {
            logError("Cannot delete transaction: id is null");
            return;
        }
        
        String sql = "DELETE FROM transactions WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            int rowsAffected = pstmt.executeUpdate();
            if (rowsAffected > 0) {
                logInfo("Transaction deleted successfully: " + id);
            } else {
                logError("No transaction found with id: " + id);
            }
        } catch (SQLException e) {
            logError("Failed to delete transaction: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Getters
    public Integer getId() { return id; }
    public int getAccountId() { return accountId; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public String getUserUuid() { return userUuid; }
    public long getTimestamp() { return timestamp; }
}
