ALTER TABLE bank_accounts
  MODIFY account_number VARCHAR(50) NOT NULL,
  ADD UNIQUE KEY uq_bank_accounts_account_number (account_number);