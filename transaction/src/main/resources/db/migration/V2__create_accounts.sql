-- V2__create_accounts.sql
-- Tabla de cuentas (incluye initial_balance y current_balance)
CREATE TABLE IF NOT EXISTS accounts (
    id               BIGSERIAL PRIMARY KEY,
    account_number   VARCHAR(32)  NOT NULL,
    account_type_id  BIGINT       NOT NULL REFERENCES account_types(id),
    client_id        VARCHAR(32)       NOT NULL,
    initial_balance  NUMERIC(19,2) NOT NULL,
    current_balance  NUMERIC(19,2) NOT NULL,
    active           BOOLEAN      NOT NULL,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_accounts_account_number UNIQUE (account_number),
    CONSTRAINT ck_accounts_initial_balance_non_negative CHECK (initial_balance >= 0),
    CONSTRAINT ck_accounts_current_balance_non_negative CHECK (current_balance >= 0)
);

-- Índices útiles
CREATE INDEX IF NOT EXISTS idx_accounts_client_id ON accounts (client_id);
CREATE INDEX IF NOT EXISTS idx_accounts_account_type_id ON accounts (account_type_id);
