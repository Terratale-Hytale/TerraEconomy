-- MySQL migration for users table
CREATE TABLE IF NOT EXISTS users (
    uuid VARCHAR(36) PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    money DECIMAL(15,2) DEFAULT 0.00 NOT NULL,
    last_login BIGINT
);