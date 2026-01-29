CREATE TABLE IF NOT EXISTS schedule_payments (
    id INT AUTO_INCREMENT PRIMARY KEY,
    receptor_account_number VARCHAR(255) NOT NULL,
    payer_account_number VARCHAR(255) NOT NULL,
    description TEXT,
    due_days INT NOT NULL,
    amount DOUBLE NOT NULL,
    day_of_month INT NOT NULL,
    status VARCHAR(50) DEFAULT 'active',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_receptor_account (receptor_account_number),
    INDEX idx_payer_account (payer_account_number),
    INDEX idx_day_of_month (day_of_month),
    INDEX idx_status (status)
);
