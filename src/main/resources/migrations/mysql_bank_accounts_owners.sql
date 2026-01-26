-- MySQL migration for bank_accounts_owners table
CREATE TABLE IF NOT EXISTS bank_accounts_owners (
    account_id INT NOT NULL,
    owner_uuid VARCHAR(36) NOT NULL,
    permission VARCHAR(20) DEFAULT 'owner' NOT NULL,
    FOREIGN KEY(account_id) REFERENCES bank_accounts(id),
    FOREIGN KEY(owner_uuid) REFERENCES users(uuid),
    PRIMARY KEY(account_id, owner_uuid)
);

-- Add permission column if it doesn't exist
ALTER TABLE bank_accounts_owners ADD COLUMN IF NOT EXISTS permission VARCHAR(20) DEFAULT 'owner';