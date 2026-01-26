-- MySQL migration for banks table
CREATE TABLE IF NOT EXISTS banks (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    owner_uuid VARCHAR(36) NOT NULL,
    balance DECIMAL(15,2) DEFAULT 0.00 NOT NULL,
    withdraw_fee DECIMAL(15,2) DEFAULT 0.00 NOT NULL,
    deposit_fee DECIMAL(15,2) DEFAULT 0.00 NOT NULL,
    transactions_fee DECIMAL(15,2) DEFAULT 0.00 NOT NULL,
    visibility VARCHAR(10) DEFAULT 'public' NOT NULL,
    FOREIGN KEY(owner_uuid) REFERENCES users(uuid)
);