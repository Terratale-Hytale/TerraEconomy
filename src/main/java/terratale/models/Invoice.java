package terratale.models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class Invoice extends Model {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Integer id;
    private String receptorAccountNumber;
    private String payerAccountNumber;
    private double amount;
    private Date dueDate;
    private String description;
    private String status;
    private JsonNode metadata;

    public Invoice(String receptorAccountNumber,
                String payerAccountNumber,
                double amount,
                Date dueDate,
                String description) {

        this.receptorAccountNumber = receptorAccountNumber;
        this.payerAccountNumber = payerAccountNumber;
        this.amount = amount;
        this.dueDate = dueDate;
        this.description = description;
        this.status = "pending";

        ObjectNode root = MAPPER.createObjectNode();
        ArrayNode events = MAPPER.createArrayNode();

        ObjectNode created = MAPPER.createObjectNode();
        created.put("type", "pending");
        created.put("timestamp", System.currentTimeMillis());
        created.put("by", "system");

        events.add(created);
        root.set("events", events);

        this.metadata = root;
    }
    
    public Invoice(String receptorAccountNumber, String payerAccountNumber, double amount, Date dueDate, String description, String status, JsonNode metadata) {
        this.receptorAccountNumber = receptorAccountNumber;
        this.payerAccountNumber = payerAccountNumber;
        this.amount = amount;
        this.dueDate = dueDate;
        this.description = description;
        this.status = status;
        this.metadata = metadata;
    }
    
    
    public static List<Integer> getAccountsByOwner(UUID ownerUuid) {
        List<Integer> accounts = new ArrayList<>();
        if (connection == null) {
            logError("Cannot find accounts: connection is null");
            return accounts;
        }
        
        String sql = "SELECT account_id FROM bank_accounts_owners WHERE owner_uuid = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, ownerUuid.toString());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                accounts.add(rs.getInt("account_id"));
            }
        } catch (SQLException e) {
            logError("Failed to find accounts by owner: " + e.getMessage());
            e.printStackTrace();
        }
        
        return accounts;
    }
    
    public void save() {
        if (connection == null) {
            logError("Cannot save Invoice: connection is null");
            return;
        }
        
        String sql = """
            INSERT INTO invoices () 
            VALUES (?, ?, ?)
            ON DUPLICATE KEY UPDATE permission = VALUES(permission)
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            pstmt.setString(2, ownerUuid.toString());
            pstmt.setString(3, permission);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logError("Failed to save bank account owner: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public void delete() {
        if (connection == null) {
            logError("Cannot delete bank account owner: connection is null");
            return;
        }
        
        String sql = "DELETE FROM bank_accounts_owners WHERE account_id = ? AND owner_uuid = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            pstmt.setString(2, ownerUuid.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logError("Failed to delete bank account owner: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static void deleteByAccount(int accountId) {
        if (connection == null) {
            logError("Cannot delete bank account owners: connection is null");
            return;
        }
        
        String sql = "DELETE FROM bank_accounts_owners WHERE account_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logError("Failed to delete bank account owners: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Getters
    public int getAccountId() { return accountId; }
    public UUID getOwnerUuid() { return ownerUuid; }
    public String getPermission() { return permission; }
}
