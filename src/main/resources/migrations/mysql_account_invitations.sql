-- MySQL migration for account_invitations table
CREATE TABLE IF NOT EXISTS account_invitations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    account_id INT NOT NULL,
    invited_uuid VARCHAR(36) NOT NULL,
    inviter_uuid VARCHAR(36) NOT NULL,
    timestamp BIGINT,
    FOREIGN KEY(account_id) REFERENCES bank_accounts(id),
    FOREIGN KEY(invited_uuid) REFERENCES users(uuid),
    FOREIGN KEY(inviter_uuid) REFERENCES users(uuid)
);