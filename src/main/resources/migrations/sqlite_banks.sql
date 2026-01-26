-- SQLite migration for banks table
CREATE TABLE IF NOT EXISTS banks (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    name TEXT NOT NULL,
    owner_uuid TEXT NOT NULL,
    balance REAL DEFAULT 0.0 NOT NULL,
    withdraw_fee REAL DEFAULT 0.0 NOT NULL,
    deposit_fee REAL DEFAULT 0.0 NOT NULL,
    transactions_fee REAL DEFAULT 0.0 NOT NULL,
    visibility TEXT DEFAULT 'public' NOT NULL,
    FOREIGN KEY(owner_uuid) REFERENCES users(uuid)
);