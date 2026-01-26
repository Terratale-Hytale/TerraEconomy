-- SQLite migration for bank_transactions table
CREATE TABLE IF NOT EXISTS bank_transactions (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    bank_id INTEGER,
    type TEXT NOT NULL,
    amount REAL NOT NULL,
    user_uuid TEXT NOT NULL,
    timestamp INTEGER,
    FOREIGN KEY(bank_id) REFERENCES banks(id),
    FOREIGN KEY(user_uuid) REFERENCES users(uuid)
);