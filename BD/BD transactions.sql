-- ============================================================================
-- Bootstrap de base de datos para entorno local
-- Este bloque crea la base de datos 'transactions' solo si no existe.
-- ============================================================================

DO $$
BEGIN
   IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'transactions') THEN
      PERFORM dblink_connect('dbname=' || current_database()); -- no-op si dblink no está
      EXECUTE 'CREATE DATABASE transactions';
   END IF;
END$$;

-- ============================================================================
-- Cambiar el contexto a la base de datos 'transactions'
-- Este metacomando es propio de psql. Si se usa otra herramienta,
-- se debe seleccionar la base por fuera (DSN).
-- ============================================================================
\connect transactions

-- ============================================================================
-- V1__create_account_types.sql
-- Catálogo de tipos de cuenta + datos semilla.
-- ============================================================================

CREATE TABLE IF NOT EXISTS account_types (
    id           BIGSERIAL PRIMARY KEY,
    code         VARCHAR(30)   NOT NULL,
    description  VARCHAR(200)  NOT NULL,
    CONSTRAINT uk_account_types_code UNIQUE (code)
);

-- Semilla (los códigos deben coincidir con el enum del dominio).
INSERT INTO account_types (code, description) VALUES
    ('AHORROS',   'Cuenta de ahorros'),
    ('CORRIENTE', 'Cuenta corriente')
ON CONFLICT (code) DO NOTHING;

-- ============================================================================
-- V2__create_accounts.sql
-- Tabla de cuentas con restricciones e índices útiles.
-- ============================================================================

CREATE TABLE IF NOT EXISTS accounts (
    id               BIGSERIAL PRIMARY KEY,
    account_number   VARCHAR(32)   NOT NULL,
    account_type_id  BIGINT        NOT NULL,
    client_id        VARCHAR(64)   NOT NULL,
    initial_balance  NUMERIC(19,2) NOT NULL,
    current_balance  NUMERIC(19,2) NOT NULL,
    active           BOOLEAN       NOT NULL,
    created_at       TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT uk_accounts_account_number UNIQUE (account_number),
    CONSTRAINT ck_accounts_initial_balance_non_negative CHECK (initial_balance >= 0),
    CONSTRAINT ck_accounts_current_balance_non_negative CHECK (current_balance >= 0),
    CONSTRAINT fk_accounts_account_type
        FOREIGN KEY (account_type_id) REFERENCES account_types (id)
);

CREATE INDEX IF NOT EXISTS idx_accounts_client_id
    ON accounts (client_id);

CREATE INDEX IF NOT EXISTS idx_accounts_account_type_id
    ON accounts (account_type_id);

-- ============================================================================
-- V3__create_movements.sql
-- Tabla de movimientos contables (inmutables) con restricciones e índices.
-- ============================================================================

CREATE TABLE IF NOT EXISTS movements (
    id             BIGSERIAL PRIMARY KEY,
    account_id     BIGINT        NOT NULL,
    type           VARCHAR(20)   NOT NULL,
    amount         NUMERIC(19,2) NOT NULL,
    balance_after  NUMERIC(19,2) NOT NULL,
    uuid           VARCHAR(36)   NOT NULL,
    happened_at    TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_movements_account
        FOREIGN KEY (account_id) REFERENCES accounts (id) ON DELETE RESTRICT,
    CONSTRAINT uk_movements_uuid UNIQUE (uuid),
    CONSTRAINT ck_movements_type_valid CHECK (type IN ('DEPOSIT','WITHDRAWAL'))
);

CREATE INDEX IF NOT EXISTS idx_movements_account
    ON movements (account_id);

CREATE INDEX IF NOT EXISTS idx_movements_account_happened_at
    ON movements (account_id, happened_at DESC);

CREATE INDEX IF NOT EXISTS idx_movements_account_uuid
    ON movements (account_id, uuid);
