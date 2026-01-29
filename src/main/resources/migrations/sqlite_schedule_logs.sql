CREATE TABLE IF NOT EXISTS schedule_logs (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    schedule_payment_id INTEGER NOT NULL,
    invoice_id INTEGER NULL,
    status TEXT NOT NULL,
    message TEXT,
    executed_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (schedule_payment_id) REFERENCES schedule_payments(id) ON DELETE CASCADE,
    FOREIGN KEY (invoice_id) REFERENCES invoices(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_schedule_payment ON schedule_logs(schedule_payment_id);
CREATE INDEX IF NOT EXISTS idx_invoice ON schedule_logs(invoice_id);
CREATE INDEX IF NOT EXISTS idx_status ON schedule_logs(status);
CREATE INDEX IF NOT EXISTS idx_executed_at ON schedule_logs(executed_at);
