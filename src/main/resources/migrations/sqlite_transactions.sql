-- SQLite migration for transactions table
CREATE TABLE IF NOT EXISTS transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    user_uuid TEXT,
    account_id INTEGER,
    type TEXT NOT NULL,
    amount REAL NOT NULL,
    timestamp INTEGER,
    FOREIGN KEY(account_id) REFERENCES bank_accounts(id),
    FOREIGN KEY(user_uuid) REFERENCES users(uuid)
);