package terratale.models;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import terratale.Helpers.InvoiceStatus;

public class Invoice extends Model {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private Integer id;
    private String receptorAccountNumber;
    private String payerAccountNumber;
    private double amount;
    private Date dueDate;
    private String description;
    private String status;
    private String events;
    private Timestamp createdAt;

    // Constructor para nueva factura
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
        this.status = InvoiceStatus.PENDING;

        ObjectNode root = MAPPER.createObjectNode();
        ArrayNode eventsArray = MAPPER.createArrayNode();

        ObjectNode created = MAPPER.createObjectNode();
        created.put("type", "created");
        created.put("timestamp", System.currentTimeMillis());
        created.put("by", "system");

        eventsArray.add(created);
        root.set("events", eventsArray);

        this.events = root.toString();
    }
    
    // Constructor privado para cargar desde base de datos
    private Invoice(Integer id, String receptorAccountNumber, String payerAccountNumber, 
                    double amount, Date dueDate, String description, String status, 
                    String events, Timestamp createdAt) {
        this.id = id;
        this.receptorAccountNumber = receptorAccountNumber;
        this.payerAccountNumber = payerAccountNumber;
        this.amount = amount;
        this.dueDate = dueDate;
        this.description = description;
        this.status = status;
        this.events = events;
        this.createdAt = createdAt;
    }
    
    // Buscar factura por ID
    public static Invoice find(int id) {
        if (connection == null) {
            logError("Cannot find invoice: connection is null");
            return null;
        }
        
        String sql = "SELECT * FROM invoices WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            
            if (rs.next()) {
                return new Invoice(
                    rs.getInt("id"),
                    rs.getString("receptor_account_number"),
                    rs.getString("payer_account_number"),
                    rs.getDouble("amount"),
                    rs.getDate("due_date"),
                    rs.getString("description"),
                    rs.getString("status"),
                    rs.getString("events"),
                    rs.getTimestamp("created_at")
                );
            }
        } catch (SQLException e) {
            logError("Failed to find invoice: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    // Buscar facturas por cuenta receptora
    public static List<Invoice> findByReceptorAccount(String accountNumber) {
        List<Invoice> invoices = new ArrayList<>();
        if (connection == null) {
            logError("Cannot find invoices: connection is null");
            return invoices;
        }
        
        String sql = "SELECT * FROM invoices WHERE receptor_account_number = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                invoices.add(new Invoice(
                    rs.getInt("id"),
                    rs.getString("receptor_account_number"),
                    rs.getString("payer_account_number"),
                    rs.getDouble("amount"),
                    rs.getDate("due_date"),
                    rs.getString("description"),
                    rs.getString("status"),
                    rs.getString("events"),
                    rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            logError("Failed to find invoices by receptor: " + e.getMessage());
            e.printStackTrace();
        }
        
        return invoices;
    }
    
    // Buscar facturas por cuenta pagadora
    public static List<Invoice> findByPayerAccount(String accountNumber) {
        List<Invoice> invoices = new ArrayList<>();
        if (connection == null) {
            logError("Cannot find invoices: connection is null");
            return invoices;
        }
        
        String sql = "SELECT * FROM invoices WHERE payer_account_number = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, accountNumber);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                invoices.add(new Invoice(
                    rs.getInt("id"),
                    rs.getString("receptor_account_number"),
                    rs.getString("payer_account_number"),
                    rs.getDouble("amount"),
                    rs.getDate("due_date"),
                    rs.getString("description"),
                    rs.getString("status"),
                    rs.getString("events"),
                    rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            logError("Failed to find invoices by payer: " + e.getMessage());
            e.printStackTrace();
        }
        
        return invoices;
    }
    
    // Buscar facturas por estado
    public static List<Invoice> findByStatus(String status) {
        List<Invoice> invoices = new ArrayList<>();
        if (connection == null) {
            logError("Cannot find invoices: connection is null");
            return invoices;
        }
        
        String sql = "SELECT * FROM invoices WHERE status = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, status);
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()) {
                invoices.add(new Invoice(
                    rs.getInt("id"),
                    rs.getString("receptor_account_number"),
                    rs.getString("payer_account_number"),
                    rs.getDouble("amount"),
                    rs.getDate("due_date"),
                    rs.getString("description"),
                    rs.getString("status"),
                    rs.getString("events"),
                    rs.getTimestamp("created_at")
                ));
            }
        } catch (SQLException e) {
            logError("Failed to find invoices by status: " + e.getMessage());
            e.printStackTrace();
        }
        
        return invoices;
    }
    
    // Guardar factura
    public void save() {
        if (connection == null) {
            logError("Cannot save invoice: connection is null");
            return;
        }
        
        if (id == null) {
            // Insertar nueva factura
            String sql = """
                INSERT INTO invoices (receptor_account_number, payer_account_number, amount, 
                                      due_date, description, status, events) 
                VALUES (?, ?, ?, ?, ?, ?, ?)
            """;
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setString(1, receptorAccountNumber);
                pstmt.setString(2, payerAccountNumber);
                pstmt.setDouble(3, amount);
                pstmt.setDate(4, dueDate);
                pstmt.setString(5, description);
                pstmt.setString(6, status);
                pstmt.setString(7, events);
                
                pstmt.executeUpdate();
                
                ResultSet rs = pstmt.getGeneratedKeys();
                if (rs.next()) {
                    this.id = rs.getInt(1);
                }
            } catch (SQLException e) {
                logError("Failed to insert invoice: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            // Actualizar factura existente
            String sql = """
                UPDATE invoices 
                SET receptor_account_number = ?, payer_account_number = ?, amount = ?,
                    due_date = ?, description = ?, status = ?, events = ?
                WHERE id = ?
            """;
            
            try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
                pstmt.setString(1, receptorAccountNumber);
                pstmt.setString(2, payerAccountNumber);
                pstmt.setDouble(3, amount);
                pstmt.setDate(4, dueDate);
                pstmt.setString(5, description);
                pstmt.setString(6, status);
                pstmt.setString(7, events);
                pstmt.setInt(8, id);
                pstmt.executeUpdate();
            } catch (SQLException e) {
                logError("Failed to update invoice: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    // Eliminar factura
    public void delete() {
        if (connection == null || id == null) {
            logError("Cannot delete invoice: connection is null or id is null");
            return;
        }
        
        String sql = "DELETE FROM invoices WHERE id = ?";
        
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            logError("Failed to delete invoice: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Añadir un evento a la factura
    public void addEvent(String eventType, String by) {
        try {
            JsonNode root = MAPPER.readTree(this.events);
            ArrayNode eventsArray = (ArrayNode) root.get("events");
            
            ObjectNode newEvent = MAPPER.createObjectNode();
            newEvent.put("type", eventType);
            newEvent.put("timestamp", System.currentTimeMillis());
            newEvent.put("by", by);
            
            eventsArray.add(newEvent);
            
            this.events = root.toString();
        } catch (Exception e) {
            logError("Failed to add event to invoice: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    // Marcar como pagada
    public void markAsPaid(String paidBy) {
        this.status = InvoiceStatus.PAID;
        addEvent("paid", paidBy);
        save();
    }
    
    // Marcar como cancelada
    public void markAsCancelled(String cancelledBy) {
        this.status = InvoiceStatus.CANCELLED;
        addEvent("cancelled", cancelledBy);
        save();
    }
    
    // Verificar si está vencida
    public boolean isOverdue() {
        if (dueDate == null || !InvoiceStatus.PENDING.equals(status)) {
            return false;
        }
        return new Date(System.currentTimeMillis()).after(dueDate);
    }
    
    // Getters
    public Integer getId() { return id; }
    public String getReceptorAccountNumber() { return receptorAccountNumber; }
    public String getPayerAccountNumber() { return payerAccountNumber; }
    public double getAmount() { return amount; }
    public Date getDueDate() { return dueDate; }
    public String getDescription() { return description; }
    public String getStatus() { return status; }
    public String getEvents() { return events; }
    public Timestamp getCreatedAt() { return createdAt; }
    
    // Setters
    public void setReceptorAccountNumber(String receptorAccountNumber) { 
        this.receptorAccountNumber = receptorAccountNumber; 
    }
    public void setPayerAccountNumber(String payerAccountNumber) { 
        this.payerAccountNumber = payerAccountNumber; 
    }
    public void setAmount(double amount) { 
        this.amount = amount; 
    }
    public void setDueDate(Date dueDate) { 
        this.dueDate = dueDate; 
    }
    public void setDescription(String description) { 
        this.description = description; 
    }
    public void setStatus(String status) { 
        this.status = status; 
    }
}
