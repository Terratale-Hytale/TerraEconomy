-- SQLite migration for bank_accounts table
CREATE TABLE IF NOT EXISTS bank_accounts (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    bank_id INTEGER NOT NULL,
    account_number TEXT,
    balance REAL DEFAULT 0.0 NOT NULL,
    withdraw_fee REAL,
    deposit_fee REAL,
    transactions_fee REAL,
    FOREIGN KEY(bank_id) REFERENCES banks(id)
);