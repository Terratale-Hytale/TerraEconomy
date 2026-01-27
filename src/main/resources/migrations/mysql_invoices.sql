CREATE TABLE IF NOT EXISTS invoices (
    id INT AUTO_INCREMENT PRIMARY KEY,
    receptor_account_number VARCHAR(255) NOT NULL,
    payer_account_number VARCHAR(255) NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    description TEXT,
    due_date DATE,
    status VARCHAR(20),
    events TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY(receptor_account_number) REFERENCES bank_accounts(account_number),
    FOREIGN KEY(payer_account_number) REFERENCES bank_accounts(account_number)
);