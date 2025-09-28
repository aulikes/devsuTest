-- V1__create_account_types.sql
-- Catálogo de tipos de cuenta
CREATE TABLE IF NOT EXISTS account_types (
    id   BIGSERIAL PRIMARY KEY,
    code VARCHAR(30)  NOT NULL,
    description VARCHAR(200)  NOT NULL,
    CONSTRAINT uk_account_types_code UNIQUE (code)
);

-- Semilla inicial (los códigos deben coincidir con el enum del dominio)
INSERT INTO account_types (code, description) VALUES
    ('AHORROS',   'Cuenta de ahorros'),
    ('CORRIENTE', 'Cuenta corriente')
ON CONFLICT (code) DO NOTHING;
