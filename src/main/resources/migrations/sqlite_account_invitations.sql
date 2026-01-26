-- SQLite migration for account_invitations table
CREATE TABLE IF NOT EXISTS account_invitations (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    account_id INTEGER NOT NULL,
    invited_uuid TEXT NOT NULL,
    inviter_uuid TEXT NOT NULL,
    timestamp INTEGER,
    FOREIGN KEY(account_id) REFERENCES bank_accounts(id),
    FOREIGN KEY(invited_uuid) REFERENCES users(uuid),
    FOREIGN KEY(inviter_uuid) REFERENCES users(uuid)
);