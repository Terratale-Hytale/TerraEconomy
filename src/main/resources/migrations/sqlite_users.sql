-- SQLite migration for users table
CREATE TABLE IF NOT EXISTS users (
    uuid TEXT PRIMARY KEY,
    username TEXT NOT NULL,
    money REAL DEFAULT 0.0 NOT NULL,
    last_login INTEGER
);