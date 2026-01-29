CREATE TABLE IF NOT EXISTS schedule_logs (
    id INT AUTO_INCREMENT PRIMARY KEY,
    schedule_payment_id INT NOT NULL,
    invoice_id INT NULL,
    status VARCHAR(50) NOT NULL,
    message TEXT,
    executed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_schedule_payment (schedule_payment_id),
    INDEX idx_invoice (invoice_id),
    INDEX idx_status (status),
    INDEX idx_executed_at (executed_at),
    FOREIGN KEY (schedule_payment_id) REFERENCES schedule_payments(id) ON DELETE CASCADE,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE SET NULL
);
