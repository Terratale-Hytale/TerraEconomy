-- SQLite migration for bank_invitations table
CREATE TABLE IF NOT EXISTS bank_invitations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    bank_id INTEGER NOT NULL,
    invited_uuid TEXT NOT NULL,
    FOREIGN KEY(bank_id) REFERENCES banks(id),
    FOREIGN KEY(invited_uuid) REFERENCES users(uuid)
);