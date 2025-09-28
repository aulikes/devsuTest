-- V3__create_movements.sql
-- Movimientos contables (inmutables)
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

-- Índices para consultas frecuentes
CREATE INDEX idx_movements_account ON movements (account_id);
CREATE INDEX idx_movements_account_happened_at ON movements (account_id, happened_at DESC);
-- CREATE INDEX idx_movements_account_uuid ON movements (account_id, uuid);
