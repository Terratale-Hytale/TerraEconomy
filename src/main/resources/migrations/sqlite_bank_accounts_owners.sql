-- SQLite migration for bank_accounts_owners table
CREATE TABLE IF NOT EXISTS bank_accounts_owners (
    account_id INTEGER NOT NULL,
    owner_uuid TEXT NOT NULL,
    permission TEXT DEFAULT 'owner' NOT NULL,
    FOREIGN KEY(account_id) REFERENCES bank_accounts(id),
    FOREIGN KEY(owner_uuid) REFERENCES users(uuid),
    PRIMARY KEY(account_id, owner_uuid)
);