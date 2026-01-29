CREATE TABLE IF NOT EXISTS schedule_payments (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    receptor_account_number TEXT NOT NULL,
    payer_account_number TEXT NOT NULL,
    description TEXT,
    due_days INTEGER NOT NULL,
    amount REAL NOT NULL,
    day_of_month INTEGER NOT NULL,
    status TEXT DEFAULT 'active',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_receptor_account ON schedule_payments(receptor_account_number);
CREATE INDEX IF NOT EXISTS idx_payer_account ON schedule_payments(payer_account_number);
CREATE INDEX IF NOT EXISTS idx_day_of_month ON schedule_payments(day_of_month);
CREATE INDEX IF NOT EXISTS idx_status ON schedule_payments(status);
