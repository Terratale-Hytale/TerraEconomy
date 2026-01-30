package terratale.models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import terratale.models.Bank;

public class BankAccount extends Model {
    
    private Integer id;
    private int bankId;
    private double balance;
    private Double withdrawFee;
    private Double depositFee;
    private String accountNumber;
    private Double transactionsFee;
    
    public BankAccount(int bankId) {
        this.bankId = bankId;
        this.balance = 0.0;
        this.accountNumber = null;
        this.withdrawFee = null;
        this.depositFee = null;
        this.transactionsFee = null;
    }
    
    private BankAccount(Integer id, int bankId, double balance, 
                        Double withdrawFee, Double depositFee, Double transactionsFee, String accountNumber) {
        this.id = id;
        this.bankId = bankId;
        this.balance = balance;
        this.accountNumber = accountNumber;
        this.withdrawFee = withdrawFee;
        this.depositFee = depositFee;
        this.transactionsFee = transactionsFee;
    }

    public Bank getBank() {
        return Bank.find(this.bankId);
    }
    
    public static BankAccount find(int id) {
        if (connection == null) {
            logError("Cannot find bank account: connection is null");
            return null;
        }
        
        String sql = "SELECT * FROM bank_accounts WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Double withdrawFee = rs.getDouble("withdraw_fee");
                if (rs.wasNull()) withdrawFee = null;
                
                Double depositFee = rs.getDouble("deposit_fee");
                if (rs.wasNull()) depositFee = null;
                
                Double transactionsFee = rs.getDouble("transactions_fee");
                if (rs.wasNull()) transactionsFee = null;
                
                return new BankAccount(
                    rs.getInt("id"),
                    rs.getInt("bank_id"),
                    rs.getDouble("balance"),
                    withdrawFee,
                    depositFee,
                    transactionsFee,
                    rs.getString("account_number")
                );
            }
        } catch (SQLException e) {
            logError("Failed to find bank account: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }

    public static BankAccount findByAccountNumber(String accountNumber) {
        if (connection == null) {
            logError("Cannot find bank account: connection is null");
            return null;
        }
        
        String sql = "SELECT * FROM bank_accounts WHERE account_number = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Double withdrawFee = rs.getDouble("withdraw_fee");
                if (rs.wasNull()) withdrawFee = null;
                
                Double depositFee = rs.getDouble("deposit_fee");
                if (rs.wasNull()) depositFee = null;
                
                Double transactionsFee = rs.getDouble("transactions_fee");
                if (rs.wasNull()) transactionsFee = null;
                
                return new BankAccount(
                    rs.getInt("id"),
                    rs.getInt("bank_id"),
                    rs.getDouble("balance"),
                    withdrawFee,
                    depositFee,
                    transactionsFee,
                    rs.getString("account_number")
                );
            }
        } catch (SQLException e) {
            logError("Failed to find bank account: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    public static List<BankAccount> findByBank(int bankId) {
        List<BankAccount> accounts = new ArrayList<>();
        if (connection == null) {
            logError("Cannot find bank accounts: connection is null");
            return accounts;
        }
        
        String sql = "SELECT * FROM bank_accounts WHERE bank_id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, bankId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Double withdrawFee = rs.getDouble("withdraw_fee");
                if (rs.wasNull()) withdrawFee = null;
                
                Double depositFee = rs.getDouble("deposit_fee");
                if (rs.wasNull()) depositFee = null;
                
                Double transactionsFee = rs.getDouble("transactions_fee");
                if (rs.wasNull()) transactionsFee = null;
                
                accounts.add(new BankAccount(
                    rs.getInt("id"),
                    rs.getInt("bank_id"),
                    rs.getDouble("balance"),
                    withdrawFee,
                    depositFee,
                    transactionsFee,
                    rs.getString("account_number")
                ));
            }
        } catch (SQLException e) {
            logError("Failed to find bank accounts: " + e.getMessage());
            e.printStackTrace();
        }
        
        return accounts;
    }

    public static List<BankAccount> getAllByOwner( UUID ownerUUID ) {
        List<BankAccount> accounts = new ArrayList<>();
        if (connection == null) {
            logError("Cannot find bank accounts: connection is null");
            return accounts;
        }
        
        String sql = "SELECT * FROM bank_accounts JOIN bank_account_owners ON bank_accounts.id = bank_account_owners.account_id WHERE bank_account_owners.owner_uuid = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setObject(1, ownerUUID);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Double withdrawFee = rs.getDouble("withdraw_fee");
                if (rs.wasNull()) withdrawFee = null;
                
                Double depositFee = rs.getDouble("deposit_fee");
                if (rs.wasNull()) depositFee = null;
                
                Double transactionsFee = rs.getDouble("transactions_fee");
                if (rs.wasNull()) transactionsFee = null;
                
                accounts.add(new BankAccount(
                    rs.getInt("id"),
                    rs.getInt("bank_id"),
                    rs.getDouble("balance"),
                    withdrawFee,
                    depositFee,
                    transactionsFee,
                    rs.getString("account_number")
                ));
            }
        } catch (SQLException e) {
            logError("Failed to find bank accounts for owner: " + e.getMessage());
            e.printStackTrace();
        }
        
        return accounts;
    }
    
    public void save() {
        if (connection == null) {
            logError("Cannot save bank account: connection is null");
            return;
        }
        
        if (id == null) {
            // Insert
            String sql = """
                INSERT INTO bank_accounts (bank_id, balance, withdraw_fee, deposit_fee, transactions_fee, account_number) 
                VALUES (?, ?, ?, ?, ?, ?)
            """;

            String accountNumber = generateAccountNumber(bankId);
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, bankId);
                pstmt.setDouble(2, balance);
                pstmt.setObject(3, withdrawFee);
                pstmt.setObject(4, depositFee);
                pstmt.setObject(5, transactionsFee);
                pstmt.setString(6, accountNumber);
                pstmt.executeUpdate();
                
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    this.id = rs.getInt(1);
                }
            } catch (SQLException e) {
                logError("Failed to insert bank account: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Update
            String sql = """
                UPDATE bank_accounts SET bank_id = ?, balance = ?, 
                withdraw_fee = ?, deposit_fee = ?, transactions_fee = ?, account_number = ?
                WHERE id = ?
            """;
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, bankId);
                pstmt.setDouble(2, balance);
                pstmt.setObject(3, withdrawFee);
                pstmt.setObject(4, depositFee);
                pstmt.setObject(5, transactionsFee);
                pstmt.setString(6, accountNumber);
                pstmt.setInt(7, id);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                logError("Failed to update bank account: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    public static double getAllAccountsMoney() {
        double total = 0.0;
        if (connection == null) {
            logError("Cannot calculate total money: connection is null");
            return total;
        }
        
        String sql = "SELECT SUM(balance) AS total_balance FROM bank_accounts";
        
        try (Statement stmt = connection.createStatement()) {
            ResultSet rs = stmt.executeQuery(sql);
            if (rs.next()) {
                total = rs.getDouble("total_balance");
            }
        } catch (SQLException e) {
            logError("Failed to calculate total money: " + e.getMessage());
            e.printStackTrace();
        }
        
        return total;
    }

    private String generateAccountNumber(int bankId) {
        // Simple account number generation logic
        Bank bank = Bank.find(bankId);
        String bankPrefix = (bank != null) ? bank.getName().substring(0, 2).toUpperCase() : "UN";
        
        // Format bankId as 2 digits (01, 02, etc.)
        String formattedBankId = String.format("%02d", bankId);
        
        // Generate 8 random digits
        String randomDigits = String.format("%08d", (int)(Math.random() * 100000000));
        
        return bankPrefix + formattedBankId + randomDigits;
    }
    
    public void delete() {
        if (connection == null || id == null) {
            logError("Cannot delete bank account: connection is null or id is null");
            return;
        }
        
        String sql = "DELETE FROM bank_accounts WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logError("Failed to delete bank account: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Getters y Setters
    public Integer getId() { return id; }
    public int getBankId() { return bankId; }
    public void setBankId(int bankId) { this.bankId = bankId; }
    public double getBalance() { return balance; }
    public void setBalance(double balance) { this.balance = balance; }
    public Double getWithdrawFee() { return withdrawFee; }
    public void setWithdrawFee(Double withdrawFee) { this.withdrawFee = withdrawFee; }
    public Double getDepositFee() { return depositFee; }
    public void setDepositFee(Double depositFee) { this.depositFee = depositFee; }
    public Double getTransactionsFee() { return transactionsFee; }
    public void setTransactionsFee(Double transactionsFee) { this.transactionsFee = transactionsFee; }
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    
    public void addBalance(double amount) { this.balance += amount; }
    public void removeBalance(double amount) { this.balance = Math.max(0, this.balance - amount); }
}
