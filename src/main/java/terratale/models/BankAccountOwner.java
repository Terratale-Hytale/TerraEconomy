package terratale.models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BankAccountOwner extends Model {
    
    private int accountId;
    private UUID ownerUuid;
    
    public BankAccountOwner(int accountId, UUID ownerUuid) {
        this.accountId = accountId;
        this.ownerUuid = ownerUuid;
    }
    
    protected static void createTable() {
        if (connection == null) {
            logError("Cannot create bank_accounts_owners table: connection is null");
            return;
        }
        
        String createOwnersTable = """
            CREATE TABLE IF NOT EXISTS bank_accounts_owners (
                account_id INTEGER NOT NULL,
                owner_uuid TEXT NOT NULL,
                FOREIGN KEY(account_id) REFERENCES bank_accounts(id),
                FOREIGN KEY(owner_uuid) REFERENCES users(uuid),
                PRIMARY KEY(account_id, owner_uuid)
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createOwnersTable);
            logInfo("Bank account owners table created/verified!");
        } catch (SQLException e) {
            logError("Failed to create bank_accounts_owners table: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static List<UUID> getOwnersByAccount(int accountId) {
        List<UUID> owners = new ArrayList<>();
        if (connection == null) {
            logError("Cannot find owners: connection is null");
            return owners;
        }
        
        String sql = "SELECT owner_uuid FROM bank_accounts_owners WHERE account_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                owners.add(UUID.fromString(rs.getString("owner_uuid")));
            }
        } catch (SQLException e) {
            logError("Failed to find owners by account: " + e.getMessage());
            e.printStackTrace();
        }
        
        return owners;
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
            logError("Cannot save bank account owner: connection is null");
            return;
        }
        
        String sql = """
            INSERT OR IGNORE INTO bank_accounts_owners (account_id, owner_uuid) 
            VALUES (?, ?)
        """;
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            pstmt.setString(2, ownerUuid.toString());
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
}
