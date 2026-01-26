-- MySQL migration for bank_invitations table
CREATE TABLE IF NOT EXISTS bank_invitations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    bank_id INT NOT NULL,
    invited_uuid VARCHAR(36) NOT NULL,
    FOREIGN KEY(bank_id) REFERENCES banks(id),
    FOREIGN KEY(invited_uuid) REFERENCES users(uuid)
);