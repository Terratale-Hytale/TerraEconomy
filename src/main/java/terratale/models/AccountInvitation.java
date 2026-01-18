package terratale.models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AccountInvitation extends Model {
    
    private Integer id;
    private int accountId;
    private UUID invitedUuid;
    private UUID inviterUuid;
    private long timestamp;
    
    public AccountInvitation(int accountId, UUID invitedUuid, UUID inviterUuid) {
        this.accountId = accountId;
        this.invitedUuid = invitedUuid;
        this.inviterUuid = inviterUuid;
        this.timestamp = System.currentTimeMillis();
    }
    
    public AccountInvitation(Integer id, int accountId, UUID invitedUuid, UUID inviterUuid, long timestamp) {
        this.id = id;
        this.accountId = accountId;
        this.invitedUuid = invitedUuid;
        this.inviterUuid = inviterUuid;
        this.timestamp = timestamp;
    }
    
    protected static void createTable() {
        if (connection == null) {
            logError("Cannot create account_invitations table: connection is null");
            return;
        }
        
        String createInvitationsTable = """
            CREATE TABLE IF NOT EXISTS account_invitations (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                account_id INTEGER NOT NULL,
                invited_uuid TEXT NOT NULL,
                inviter_uuid TEXT NOT NULL,
                timestamp INTEGER,
                FOREIGN KEY(account_id) REFERENCES bank_accounts(id),
                FOREIGN KEY(invited_uuid) REFERENCES users(uuid),
                FOREIGN KEY(inviter_uuid) REFERENCES users(uuid)
            )
        """;
        
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(createInvitationsTable);
            logInfo("Account invitations table created/verified!");
        } catch (SQLException e) {
            logError("Failed to create account_invitations table: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public static AccountInvitation find(int id) {
        if (connection == null) {
            logError("Cannot find account invitation: connection is null");
            return null;
        }
        
        String sql = "SELECT * FROM account_invitations WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new AccountInvitation(
                    rs.getInt("id"),
                    rs.getInt("account_id"),
                    UUID.fromString(rs.getString("invited_uuid")),
                    UUID.fromString(rs.getString("inviter_uuid")),
                    rs.getLong("timestamp")
                );
            }
        } catch (SQLException e) {
            logError("Failed to find account invitation: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static List<AccountInvitation> findByAccount(int accountId) {
        List<AccountInvitation> invitations = new ArrayList<>();
        
        if (connection == null) {
            logError("Cannot find invitations by account: connection is null");
            return invitations;
        }
        
        String sql = "SELECT * FROM account_invitations WHERE account_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                invitations.add(new AccountInvitation(
                    rs.getInt("id"),
                    rs.getInt("account_id"),
                    UUID.fromString(rs.getString("invited_uuid")),
                    UUID.fromString(rs.getString("inviter_uuid")),
                    rs.getLong("timestamp")
                ));
            }
        } catch (SQLException e) {
            logError("Failed to find invitations by account: " + e.getMessage());
            e.printStackTrace();
        }
        
        return invitations;
    }
    
    public static List<AccountInvitation> findByInvitedUser(UUID invitedUuid) {
        List<AccountInvitation> invitations = new ArrayList<>();
        
        if (connection == null) {
            logError("Cannot find invitations by invited user: connection is null");
            return invitations;
        }
        
        String sql = "SELECT * FROM account_invitations WHERE invited_uuid = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, invitedUuid.toString());
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                invitations.add(new AccountInvitation(
                    rs.getInt("id"),
                    rs.getInt("account_id"),
                    UUID.fromString(rs.getString("invited_uuid")),
                    UUID.fromString(rs.getString("inviter_uuid")),
                    rs.getLong("timestamp")
                ));
            }
        } catch (SQLException e) {
            logError("Failed to find invitations by invited user: " + e.getMessage());
            e.printStackTrace();
        }
        
        return invitations;
    }
    
    public static AccountInvitation findPending(int accountId, UUID invitedUuid) {
        if (connection == null) {
            logError("Cannot find pending invitation: connection is null");
            return null;
        }
        
        String sql = "SELECT * FROM account_invitations WHERE account_id = ? AND invited_uuid = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, accountId);
            pstmt.setString(2, invitedUuid.toString());
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new AccountInvitation(
                    rs.getInt("id"),
                    rs.getInt("account_id"),
                    UUID.fromString(rs.getString("invited_uuid")),
                    UUID.fromString(rs.getString("inviter_uuid")),
                    rs.getLong("timestamp")
                );
            }
        } catch (SQLException e) {
            logError("Failed to find pending invitation: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    public void save() {
        if (connection == null) {
            logError("Cannot save account invitation: connection is null");
            return;
        }
        
        if (id == null) {
            // INSERT
            String sql = """
                INSERT INTO account_invitations (account_id, invited_uuid, inviter_uuid, timestamp) 
                VALUES (?, ?, ?, ?)
            """;
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, accountId);
                pstmt.setString(2, invitedUuid.toString());
                pstmt.setString(3, inviterUuid.toString());
                pstmt.setLong(4, timestamp);
                pstmt.executeUpdate();
                
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    this.id = rs.getInt(1);
                }
            } catch (SQLException e) {
                logError("Failed to insert account invitation: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // UPDATE
            String sql = """
                UPDATE account_invitations 
                SET account_id = ?, invited_uuid = ?, inviter_uuid = ?, timestamp = ?
                WHERE id = ?
            """;
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, accountId);
                pstmt.setString(2, invitedUuid.toString());
                pstmt.setString(3, inviterUuid.toString());
                pstmt.setLong(4, timestamp);
                pstmt.setInt(5, id);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                logError("Failed to update account invitation: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    public void delete() {
        if (connection == null || id == null) {
            logError("Cannot delete account invitation: connection is null or id is null");
            return;
        }
        
        String sql = "DELETE FROM account_invitations WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logError("Failed to delete account invitation: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    public boolean accept() {
        // Crear el vínculo en bank_accounts_owners
        BankAccountOwner owner = new BankAccountOwner(accountId, invitedUuid);
        owner.save();
        
        // Eliminar la invitación
        delete();
        
        return true;
    }
    
    public void reject() {
        delete();
    }
    
    // Getters
    public Integer getId() {
        return id;
    }
    
    public int getAccountId() {
        return accountId;
    }
    
    public UUID getInvitedUuid() {
        return invitedUuid;
    }
    
    public UUID getInviterUuid() {
        return inviterUuid;
    }
    
    public long getTimestamp() {
        return timestamp;
    }
    
    // Setters
    public void setAccountId(int accountId) {
        this.accountId = accountId;
    }
    
    public void setInvitedUuid(UUID invitedUuid) {
        this.invitedUuid = invitedUuid;
    }
    
    public void setInviterUuid(UUID inviterUuid) {
        this.inviterUuid = inviterUuid;
    }
    
    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
