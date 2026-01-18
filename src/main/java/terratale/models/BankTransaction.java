package terratale.models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BankTransaction extends Model {
    
    private Integer id;
    private int bankId;
    private String type;
    private double amount;
    private String userUuid;
    private long timestamp;
    
    public BankTransaction(int bankId, String type, double amount, String userUuid) {
        this.bankId = bankId;
        this.type = type;
        this.amount = amount;
        this.userUuid = userUuid;
        this.timestamp = System.currentTimeMillis();
    }
    
    private BankTransaction(Integer id, int bankId, String type, double amount, String userUuid, long timestamp) {
        this.id = id;
        this.bankId = bankId;
        this.type = type;
        this.amount = amount;
        this.userUuid = userUuid;
        this.timestamp = timestamp;
    }
    
    protected static void createTable() {
        if (connection == null) {
            logError("Cannot create bank_transactions table: connection is null");
            return;
        }
        
        String createBankTransactionsTable = """
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
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createBankTransactionsTable);
            logInfo("Bank transactions table created/verified!");
        } catch (SQLException e) {
            logError("Failed to create bank_transactions table: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static BankTransaction find(int id) {
        if (connection == null) {
            logError("Cannot find bank transaction: connection is null");
            return null;
        }
        
        String sql = "SELECT * FROM bank_transactions WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new BankTransaction(
                    rs.getInt("id"),
                    rs.getInt("bank_id"),
                    rs.getString("type"),
                    rs.getDouble("amount"),
                    rs.getString("user_uuid"),
                    rs.getLong("timestamp")
                );
            }
        } catch (SQLException e) {
            logError("Failed to find bank transaction: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static List<BankTransaction> findByBank(int bankId) {
        List<BankTransaction> transactions = new ArrayList<>();
        if (connection == null) {
            logError("Cannot find bank transactions: connection is null");
            return transactions;
        }
        
        String sql = "SELECT * FROM bank_transactions WHERE bank_id = ? ORDER BY timestamp DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, bankId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                transactions.add(new BankTransaction(
                    rs.getInt("id"),
                    rs.getInt("bank_id"),
                    rs.getString("type"),
                    rs.getDouble("amount"),
                    rs.getString("user_uuid"),
                    rs.getLong("timestamp")
                ));
            }
        } catch (SQLException e) {
            logError("Failed to find bank transactions: " + e.getMessage());
            e.printStackTrace();
        }
        
        return transactions;
    }
    
    public void save() {
        if (connection == null) {
            logError("Cannot save bank transaction: connection is null");
            return;
        }
        
        if (id == null) {
            // Insert
            String sql = """
                INSERT INTO bank_transactions (bank_id, type, amount, user_uuid, timestamp) 
                VALUES (?, ?, ?, ?, ?)
            """;
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, bankId);
                pstmt.setString(2, type);
                pstmt.setDouble(3, amount);
                pstmt.setString(4, userUuid);
                pstmt.setLong(5, timestamp);
                pstmt.executeUpdate();
                
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    this.id = rs.getInt(1);
                }
            } catch (SQLException e) {
                logError("Failed to insert bank transaction: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    // Getters
    public Integer getId() { return id; }
    public int getBankId() { return bankId; }
    public String getType() { return type; }
    public double getAmount() { return amount; }
    public long getTimestamp() { return timestamp; }
}
