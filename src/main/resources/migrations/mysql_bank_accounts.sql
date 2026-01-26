-- MySQL migration for bank_accounts table
CREATE TABLE IF NOT EXISTS bank_accounts (
    id INT AUTO_INCREMENT PRIMARY KEY,
    bank_id INT NOT NULL,
    account_number VARCHAR(50),
    balance DECIMAL(15,2) DEFAULT 0.00 NOT NULL,
    withdraw_fee DECIMAL(15,2),
    deposit_fee DECIMAL(15,2),
    transactions_fee DECIMAL(15,2),
    FOREIGN KEY(bank_id) REFERENCES banks(id)
);