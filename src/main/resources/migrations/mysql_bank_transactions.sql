-- MySQL migration for bank_transactions table
CREATE TABLE IF NOT EXISTS bank_transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    bank_id INT,
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    user_uuid VARCHAR(36) NOT NULL,
    timestamp BIGINT,
    FOREIGN KEY(bank_id) REFERENCES banks(id),
    FOREIGN KEY(user_uuid) REFERENCES users(uuid)
);