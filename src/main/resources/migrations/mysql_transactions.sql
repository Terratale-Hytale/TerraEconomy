-- MySQL migration for transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id INT AUTO_INCREMENT PRIMARY KEY,
    user_uuid VARCHAR(36),
    account_id INT,
    type VARCHAR(20) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    timestamp BIGINT,
    FOREIGN KEY(account_id) REFERENCES bank_accounts(id),
    FOREIGN KEY(user_uuid) REFERENCES users(uuid)
);