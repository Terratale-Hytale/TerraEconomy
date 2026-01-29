package terratale.models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SchedulePayment extends Model {
    
    private Integer id;
    private String receptorAccountNumber;
    private String payerAccountNumber;
    private String description;
    private int dueDays;
    private double amount;
    private int dayOfMonth;
    private String status;
    private Timestamp createdAt;
    
    // Constructor para nuevo pago programado
    public SchedulePayment(String receptorAccountNumber, 
                          String payerAccountNumber, 
                          String description,
                          int dueDays,
                          double amount,
                          int dayOfMonth) {
        this.receptorAccountNumber = receptorAccountNumber;
        this.payerAccountNumber = payerAccountNumber;
        this.description = description;
        this.dueDays = dueDays;
        this.amount = amount;
        this.dayOfMonth = dayOfMonth;
        this.status = "active";
    }
    
    // Constructor privado para cargar desde base de datos
    private SchedulePayment(Integer id, String receptorAccountNumber, String payerAccountNumber,
                           String description, int dueDays, double amount, int dayOfMonth,
                           String status, Timestamp createdAt) {
        this.id = id;
        this.receptorAccountNumber = receptorAccountNumber;
        this.payerAccountNumber = payerAccountNumber;
        this.description = description;
        this.dueDays = dueDays;
        this.amount = amount;
        this.dayOfMonth = dayOfMonth;
        this.status = status;
        this.createdAt = createdAt;
    }
    
    // Buscar pago programado por ID
    public static SchedulePayment find(int id) {
        if (connection == null) {
            logError("Cannot find schedule payment: connection is null");
            return null;
        }
        
        String sql = "SELECT * FROM schedule_payments WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new SchedulePayment(
                    rs.getInt("id"),
                    rs.getString("receptor_account_number"),
                    rs.getString("payer_account_number"),
                    rs.getString("description"),
                    rs.getInt("due_days"),
                    rs.getDouble("amount"),
                    rs.getInt("day_of_month"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at")
                );
            }
        } catch (SQLException e) {
            logError("Failed to find schedule payment: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Buscar pagos programados por cuenta pagadora
    public static List<SchedulePayment> findByPayerAccount(String payerAccountNumber) {
        List<SchedulePayment> schedulePayments = new ArrayList<>();
        if (connection == null) {
            logError("Cannot find schedule payments: connection is null");
            return schedulePayments;
        }
        
        String sql = "SELECT * FROM schedule_payments WHERE payer_account_number = ? ORDER BY day_of_month ASC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, payerAccountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                schedulePayments.add(new SchedulePayment(
                    rs.getInt("id"),
                    rs.getString("receptor_account_number"),
                    rs.getString("payer_account_number"),
                    rs.getString("description"),
                    rs.getInt("due_days"),
                    rs.getDouble("amount"),
                    rs.getInt("day_of_month"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            logError("Failed to find schedule payments by payer: " + e.getMessage());
            e.printStackTrace();
        }
        
        return schedulePayments;
    }
    
    // Buscar pagos programados por cuenta receptora
    public static List<SchedulePayment> findByReceptorAccount(String receptorAccountNumber) {
        List<SchedulePayment> schedulePayments = new ArrayList<>();
        if (connection == null) {
            logError("Cannot find schedule payments: connection is null");
            return schedulePayments;
        }
        
        String sql = "SELECT * FROM schedule_payments WHERE receptor_account_number = ? ORDER BY day_of_month ASC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, receptorAccountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                schedulePayments.add(new SchedulePayment(
                    rs.getInt("id"),
                    rs.getString("receptor_account_number"),
                    rs.getString("payer_account_number"),
                    rs.getString("description"),
                    rs.getInt("due_days"),
                    rs.getDouble("amount"),
                    rs.getInt("day_of_month"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            logError("Failed to find schedule payments by receptor: " + e.getMessage());
            e.printStackTrace();
        }
        
        return schedulePayments;
    }
    
    // Buscar pagos programados activos por día del mes
    public static List<SchedulePayment> findByDayOfMonth(int dayOfMonth) {
        List<SchedulePayment> schedulePayments = new ArrayList<>();
        if (connection == null) {
            logError("Cannot find schedule payments: connection is null");
            return schedulePayments;
        }
        
        String sql = "SELECT * FROM schedule_payments WHERE day_of_month = ? AND status = 'active' ORDER BY created_at DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, dayOfMonth);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                schedulePayments.add(new SchedulePayment(
                    rs.getInt("id"),
                    rs.getString("receptor_account_number"),
                    rs.getString("payer_account_number"),
                    rs.getString("description"),
                    rs.getInt("due_days"),
                    rs.getDouble("amount"),
                    rs.getInt("day_of_month"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            logError("Failed to find schedule payments by day of month: " + e.getMessage());
            e.printStackTrace();
        }
        
        return schedulePayments;
    }
    
    // Obtener todos los pagos programados
    public static List<SchedulePayment> all() {
        List<SchedulePayment> schedulePayments = new ArrayList<>();
        if (connection == null) {
            logError("Cannot get schedule payments: connection is null");
            return schedulePayments;
        }
        
        String sql = "SELECT * FROM schedule_payments ORDER BY day_of_month ASC, created_at DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                schedulePayments.add(new SchedulePayment(
                    rs.getInt("id"),
                    rs.getString("receptor_account_number"),
                    rs.getString("payer_account_number"),
                    rs.getString("description"),
                    rs.getInt("due_days"),
                    rs.getDouble("amount"),
                    rs.getInt("day_of_month"),
                    rs.getString("status"),
                    rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            logError("Failed to get all schedule payments: " + e.getMessage());
            e.printStackTrace();
        }
        
        return schedulePayments;
    }
    
    // Guardar pago programado
    public void save() {
        if (connection == null) {
            logError("Cannot save schedule payment: connection is null");
            return;
        }
        
        if (id == null) {
            // Insertar nuevo pago programado
            String sql = "INSERT INTO schedule_payments (receptor_account_number, payer_account_number, " +
                        "description, due_days, amount, day_of_month, status, created_at) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, receptorAccountNumber);
                pstmt.setString(2, payerAccountNumber);
                pstmt.setString(3, description);
                pstmt.setInt(4, dueDays);
                pstmt.setDouble(5, amount);
                pstmt.setInt(6, dayOfMonth);
                pstmt.setString(7, status);
                pstmt.setTimestamp(8, new Timestamp(System.currentTimeMillis()));
                
                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            this.id = generatedKeys.getInt(1);
                            logInfo("Schedule payment created with ID: " + id);
                        }
                    }
                }
            } catch (SQLException e) {
                logError("Failed to insert schedule payment: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Actualizar pago programado existente
            String sql = "UPDATE schedule_payments SET receptor_account_number = ?, payer_account_number = ?, " +
                        "description = ?, due_days = ?, amount = ?, day_of_month = ?, status = ? WHERE id = ?";
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, receptorAccountNumber);
                pstmt.setString(2, payerAccountNumber);
                pstmt.setString(3, description);
                pstmt.setInt(4, dueDays);
                pstmt.setDouble(5, amount);
                pstmt.setInt(6, dayOfMonth);
                pstmt.setString(7, status);
                pstmt.setInt(8, id);
                
                pstmt.executeUpdate();
                logInfo("Schedule payment updated: " + id);
            } catch (SQLException e) {
                logError("Failed to update schedule payment: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    // Eliminar pago programado
    public void delete() {
        if (connection == null) {
            logError("Cannot delete schedule payment: connection is null");
            return;
        }
        
        if (id == null) {
            logError("Cannot delete schedule payment: ID is null");
            return;
        }
        
        String sql = "DELETE FROM schedule_payments WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            logInfo("Schedule payment deleted: " + id);
        } catch (SQLException e) {
            logError("Failed to delete schedule payment: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Getters
    public Integer getId() {
        return id;
    }
    
    public String getReceptorAccountNumber() {
        return receptorAccountNumber;
    }
    
    public String getPayerAccountNumber() {
        return payerAccountNumber;
    }
    
    public String getDescription() {
        return description;
    }
    
    public int getDueDays() {
        return dueDays;
    }
    
    public double getAmount() {
        return amount;
    }
    
    public int getDayOfMonth() {
        return dayOfMonth;
    }
    
    public String getStatus() {
        return status;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    // Setters
    public void setReceptorAccountNumber(String receptorAccountNumber) {
        this.receptorAccountNumber = receptorAccountNumber;
    }
    
    public void setPayerAccountNumber(String payerAccountNumber) {
        this.payerAccountNumber = payerAccountNumber;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setDueDays(int dueDays) {
        this.dueDays = dueDays;
    }
    
    public void setAmount(double amount) {
        this.amount = amount;
    }
    
    public void setDayOfMonth(int dayOfMonth) {
        this.dayOfMonth = dayOfMonth;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
}
