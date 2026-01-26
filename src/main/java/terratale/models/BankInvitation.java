package terratale.models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BankInvitation extends Model {

    private Integer id;
    private int bankId;
    private UUID invitedUuid;

    public BankInvitation(int bankId, UUID invitedUuid) {
        this.bankId = bankId;
        this.invitedUuid = invitedUuid;
    }

    public BankInvitation(Integer id, int bankId, UUID invitedUuid) {
        this.id = id;
        this.bankId = bankId;
        this.invitedUuid = invitedUuid;
    }

    public static List<BankInvitation> findByBankId(int bankId) {
        List<BankInvitation> invitations = new ArrayList<>();
        if (connection == null) {
            logError("Cannot find invitations: connection is null");
            return invitations;
        }

        String sql = "SELECT id, bank_id, invited_uuid FROM bank_invitations WHERE bank_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, bankId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                invitations.add(new BankInvitation(
                    rs.getInt("id"),
                    rs.getInt("bank_id"),
                    UUID.fromString(rs.getString("invited_uuid"))
                ));
            }
        } catch (SQLException e) {
            logError("Failed to find invitations by bank: " + e.getMessage());
            e.printStackTrace();
        }

        return invitations;
    }

    public static List<BankInvitation> findByInvitedUuid(UUID invitedUuid) {
        List<BankInvitation> invitations = new ArrayList<>();
        if (connection == null) {
            logError("Cannot find invitations: connection is null");
            return invitations;
        }

        String sql = "SELECT id, bank_id, invited_uuid FROM bank_invitations WHERE invited_uuid = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, invitedUuid.toString());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                invitations.add(new BankInvitation(
                    rs.getInt("id"),
                    rs.getInt("bank_id"),
                    UUID.fromString(rs.getString("invited_uuid"))
                ));
            }
        } catch (SQLException e) {
            logError("Failed to find invitations by invited user: " + e.getMessage());
            e.printStackTrace();
        }

        return invitations;
    }

    public static BankInvitation findByBankAndUser(int bankId, UUID invitedUuid) {
        if (connection == null) {
            logError("Cannot find invitation: connection is null");
            return null;
        }

        String sql = "SELECT id, bank_id, invited_uuid FROM bank_invitations WHERE bank_id = ? AND invited_uuid = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, bankId);
            pstmt.setString(2, invitedUuid.toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new BankInvitation(
                    rs.getInt("id"),
                    rs.getInt("bank_id"),
                    UUID.fromString(rs.getString("invited_uuid"))
                );
            }
        } catch (SQLException e) {
            logError("Failed to find invitation by bank and user: " + e.getMessage());
            e.printStackTrace();
        }

        return null;
    }

    public void save() {
        if (connection == null) {
            logError("Cannot save bank invitation: connection is null");
            return;
        }

        String sql;
        boolean isUpdate = (id != null);

        if (isUpdate) {
            sql = "UPDATE bank_invitations SET bank_id = ?, invited_uuid = ? WHERE id = ?";
        } else {
            sql = "INSERT INTO bank_invitations (bank_id, invited_uuid) VALUES (?, ?)";
        }

        try (PreparedStatement pstmt = connection.prepareStatement(sql, isUpdate ? Statement.NO_GENERATED_KEYS : Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, bankId);
            pstmt.setString(2, invitedUuid.toString());

            if (isUpdate) {
                pstmt.setInt(3, id);
                pstmt.executeUpdate();
            } else {
                pstmt.executeUpdate();
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        this.id = generatedKeys.getInt(1);
                    }
                }
            }
        } catch (SQLException e) {
            logError("Failed to save bank invitation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void delete() {
        if (connection == null) {
            logError("Cannot delete bank invitation: connection is null");
            return;
        }

        String sql = "DELETE FROM bank_invitations WHERE id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logError("Failed to delete bank invitation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void deleteByBankId(int bankId) {
        if (connection == null) {
            logError("Cannot delete bank invitations: connection is null");
            return;
        }

        String sql = "DELETE FROM bank_invitations WHERE bank_id = ?";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, bankId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logError("Failed to delete bank invitations: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Getters
    public Integer getId() { return id; }
    public int getBankId() { return bankId; }
    public UUID getInvitedUuid() { return invitedUuid; }
}