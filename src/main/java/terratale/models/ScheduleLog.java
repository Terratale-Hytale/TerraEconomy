package terratale.models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ScheduleLog extends Model {
    
    private Integer id;
    private int schedulePaymentId;
    private Integer invoiceId;
    private String status;
    private String message;
    private Timestamp executedAt;
    
    // Constructor para nuevo log
    public ScheduleLog(int schedulePaymentId, Integer invoiceId, String status, String message) {
        this.schedulePaymentId = schedulePaymentId;
        this.invoiceId = invoiceId;
        this.status = status;
        this.message = message;
        this.executedAt = new Timestamp(System.currentTimeMillis());
    }
    
    // Constructor privado para cargar desde base de datos
    private ScheduleLog(Integer id, int schedulePaymentId, Integer invoiceId, 
                       String status, String message, Timestamp executedAt) {
        this.id = id;
        this.schedulePaymentId = schedulePaymentId;
        this.invoiceId = invoiceId;
        this.status = status;
        this.message = message;
        this.executedAt = executedAt;
    }
    
    // Buscar log por ID
    public static ScheduleLog find(int id) {
        if (connection == null) {
            logError("Cannot find schedule log: connection is null");
            return null;
        }
        
        String sql = "SELECT * FROM schedule_logs WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                Integer invoiceId = rs.getObject("invoice_id") != null ? rs.getInt("invoice_id") : null;
                return new ScheduleLog(
                    rs.getInt("id"),
                    rs.getInt("schedule_payment_id"),
                    invoiceId,
                    rs.getString("status"),
                    rs.getString("message"),
                    rs.getTimestamp("executed_at")
                );
            }
        } catch (SQLException e) {
            logError("Failed to find schedule log: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Buscar logs por pago programado
    public static List<ScheduleLog> findBySchedulePayment(int schedulePaymentId) {
        List<ScheduleLog> logs = new ArrayList<>();
        if (connection == null) {
            logError("Cannot find schedule logs: connection is null");
            return logs;
        }
        
        String sql = "SELECT * FROM schedule_logs WHERE schedule_payment_id = ? ORDER BY executed_at DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, schedulePaymentId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Integer invoiceId = rs.getObject("invoice_id") != null ? rs.getInt("invoice_id") : null;
                logs.add(new ScheduleLog(
                    rs.getInt("id"),
                    rs.getInt("schedule_payment_id"),
                    invoiceId,
                    rs.getString("status"),
                    rs.getString("message"),
                    rs.getTimestamp("executed_at")
                ));
            }
        } catch (SQLException e) {
            logError("Failed to find schedule logs by payment: " + e.getMessage());
            e.printStackTrace();
        }
        
        return logs;
    }
    
    // Buscar logs por factura
    public static List<ScheduleLog> findByInvoice(int invoiceId) {
        List<ScheduleLog> logs = new ArrayList<>();
        if (connection == null) {
            logError("Cannot find schedule logs: connection is null");
            return logs;
        }
        
        String sql = "SELECT * FROM schedule_logs WHERE invoice_id = ? ORDER BY executed_at DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, invoiceId);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Integer invId = rs.getObject("invoice_id") != null ? rs.getInt("invoice_id") : null;
                logs.add(new ScheduleLog(
                    rs.getInt("id"),
                    rs.getInt("schedule_payment_id"),
                    invId,
                    rs.getString("status"),
                    rs.getString("message"),
                    rs.getTimestamp("executed_at")
                ));
            }
        } catch (SQLException e) {
            logError("Failed to find schedule logs by invoice: " + e.getMessage());
            e.printStackTrace();
        }
        
        return logs;
    }
    
    // Buscar logs por estado
    public static List<ScheduleLog> findByStatus(String status) {
        List<ScheduleLog> logs = new ArrayList<>();
        if (connection == null) {
            logError("Cannot find schedule logs: connection is null");
            return logs;
        }
        
        String sql = "SELECT * FROM schedule_logs WHERE status = ? ORDER BY executed_at DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Integer invoiceId = rs.getObject("invoice_id") != null ? rs.getInt("invoice_id") : null;
                logs.add(new ScheduleLog(
                    rs.getInt("id"),
                    rs.getInt("schedule_payment_id"),
                    invoiceId,
                    rs.getString("status"),
                    rs.getString("message"),
                    rs.getTimestamp("executed_at")
                ));
            }
        } catch (SQLException e) {
            logError("Failed to find schedule logs by status: " + e.getMessage());
            e.printStackTrace();
        }
        
        return logs;
    }
    
    // Obtener logs recientes (últimos N)
    public static List<ScheduleLog> getRecent(int limit) {
        List<ScheduleLog> logs = new ArrayList<>();
        if (connection == null) {
            logError("Cannot get schedule logs: connection is null");
            return logs;
        }
        
        String sql = "SELECT * FROM schedule_logs ORDER BY executed_at DESC LIMIT ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, limit);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Integer invoiceId = rs.getObject("invoice_id") != null ? rs.getInt("invoice_id") : null;
                logs.add(new ScheduleLog(
                    rs.getInt("id"),
                    rs.getInt("schedule_payment_id"),
                    invoiceId,
                    rs.getString("status"),
                    rs.getString("message"),
                    rs.getTimestamp("executed_at")
                ));
            }
        } catch (SQLException e) {
            logError("Failed to get recent schedule logs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return logs;
    }
    
    // Obtener todos los logs
    public static List<ScheduleLog> all() {
        List<ScheduleLog> logs = new ArrayList<>();
        if (connection == null) {
            logError("Cannot get schedule logs: connection is null");
            return logs;
        }
        
        String sql = "SELECT * FROM schedule_logs ORDER BY executed_at DESC";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                Integer invoiceId = rs.getObject("invoice_id") != null ? rs.getInt("invoice_id") : null;
                logs.add(new ScheduleLog(
                    rs.getInt("id"),
                    rs.getInt("schedule_payment_id"),
                    invoiceId,
                    rs.getString("status"),
                    rs.getString("message"),
                    rs.getTimestamp("executed_at")
                ));
            }
        } catch (SQLException e) {
            logError("Failed to get all schedule logs: " + e.getMessage());
            e.printStackTrace();
        }
        
        return logs;
    }
    
    // Guardar log
    public void save() {
        if (connection == null) {
            logError("Cannot save schedule log: connection is null");
            return;
        }
        
        if (id == null) {
            // Insertar nuevo log
            String sql = "INSERT INTO schedule_logs (schedule_payment_id, invoice_id, status, message, executed_at) " +
                        "VALUES (?, ?, ?, ?, ?)";
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, schedulePaymentId);
                if (invoiceId != null) {
                    pstmt.setInt(2, invoiceId);
                } else {
                    pstmt.setNull(2, Types.INTEGER);
                }
                pstmt.setString(3, status);
                pstmt.setString(4, message);
                pstmt.setTimestamp(5, executedAt);
                
                int affectedRows = pstmt.executeUpdate();
                
                if (affectedRows > 0) {
                    try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                        if (generatedKeys.next()) {
                            this.id = generatedKeys.getInt(1);
                            logInfo("Schedule log created with ID: " + id);
                        }
                    }
                }
            } catch (SQLException e) {
                logError("Failed to insert schedule log: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Actualizar log existente
            String sql = "UPDATE schedule_logs SET schedule_payment_id = ?, invoice_id = ?, " +
                        "status = ?, message = ? WHERE id = ?";
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setInt(1, schedulePaymentId);
                if (invoiceId != null) {
                    pstmt.setInt(2, invoiceId);
                } else {
                    pstmt.setNull(2, Types.INTEGER);
                }
                pstmt.setString(3, status);
                pstmt.setString(4, message);
                pstmt.setInt(5, id);
                
                pstmt.executeUpdate();
                logInfo("Schedule log updated: " + id);
            } catch (SQLException e) {
                logError("Failed to update schedule log: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    // Eliminar log
    public void delete() {
        if (connection == null) {
            logError("Cannot delete schedule log: connection is null");
            return;
        }
        
        if (id == null) {
            logError("Cannot delete schedule log: ID is null");
            return;
        }
        
        String sql = "DELETE FROM schedule_logs WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
            logInfo("Schedule log deleted: " + id);
        } catch (SQLException e) {
            logError("Failed to delete schedule log: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Getters
    public Integer getId() {
        return id;
    }
    
    public int getSchedulePaymentId() {
        return schedulePaymentId;
    }
    
    public Integer getInvoiceId() {
        return invoiceId;
    }
    
    public String getStatus() {
        return status;
    }
    
    public String getMessage() {
        return message;
    }
    
    public Timestamp getExecutedAt() {
        return executedAt;
    }
    
    // Setters
    public void setSchedulePaymentId(int schedulePaymentId) {
        this.schedulePaymentId = schedulePaymentId;
    }
    
    public void setInvoiceId(Integer invoiceId) {
        this.invoiceId = invoiceId;
    }
    
    public void setStatus(String status) {
        this.status = status;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
