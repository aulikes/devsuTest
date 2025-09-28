-- ============================================================================
-- Bootstrap de base de datos para entorno local
-- Crea la base de datos 'clients' solo si no existe.
-- ============================================================================

DO $$
BEGIN
   IF NOT EXISTS (SELECT 1 FROM pg_database WHERE datname = 'clients') THEN
      EXECUTE 'CREATE DATABASE clients';
   END IF;
END$$;

-- ============================================================================
-- Cambiar el contexto a la base de datos 'clients'
-- Metacomando de psql; si se usa otra herramienta, se debe seleccionar la base
-- mediante la cadena de conexión o ajuste equivalente.
-- ============================================================================
\connect clients

-- ============================================================================
-- Catálogo: genders
-- Basado en el script de creación de géneros y su índice único por code.
-- ============================================================================

CREATE TABLE IF NOT EXISTS genders (
  id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code VARCHAR(16)  NOT NULL,
  name VARCHAR(64)  NOT NULL,
  CONSTRAINT uk_genders_code UNIQUE (code)
);

-- Semilla estándar
INSERT INTO genders(code, name) VALUES
  ('MALE', 'Male'),
  ('FEMALE', 'Female'),
  ('OTHER', 'Other')
ON CONFLICT (code) DO NOTHING;

-- ============================================================================
-- Catálogo: identification_types
-- Basado en el script original con índice único por code.
-- ============================================================================

CREATE TABLE IF NOT EXISTS identification_types (
  id   BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code VARCHAR(16)  NOT NULL,
  name VARCHAR(64)  NOT NULL,
  CONSTRAINT uk_ident_types_code UNIQUE (code)
);

-- Semilla estándar
INSERT INTO identification_types(code, name) VALUES
    ('CC',  'Cédula de ciudadanía'),
    ('CE',  'Cédula de extranjería'),
    ('PAS', 'Pasaporte'),
    ('NIT', 'NIT')
ON CONFLICT (code) DO NOTHING;

-- ============================================================================
-- Tabla principal: persons
-- Se normalizan nombres de columnas a snake_case (por ejemplo, last_name).
-- Incluye unicidad por identification_number y FKs a catálogos.
-- ============================================================================

CREATE TABLE IF NOT EXISTS persons (
  id                       BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  first_name               VARCHAR(120) NOT NULL,
  last_name                VARCHAR(120) NOT NULL,
  birth_date               DATE         NOT NULL,
  address                  VARCHAR(200) NOT NULL,
  phone                    VARCHAR(40)  NOT NULL,
  identification_number    VARCHAR(40)  NOT NULL,
  gender_id                BIGINT       NOT NULL,
  identification_type_id   BIGINT       NOT NULL,
  CONSTRAINT uk_persons_identification_number UNIQUE (identification_number),
  CONSTRAINT fk_persons_gender
     FOREIGN KEY (gender_id)
     REFERENCES genders(id)
     ON UPDATE RESTRICT
     ON DELETE RESTRICT,
  CONSTRAINT fk_persons_ident_type
     FOREIGN KEY (identification_type_id)
     REFERENCES identification_types(id)
     ON UPDATE RESTRICT
     ON DELETE RESTRICT
);

-- Índices de apoyo para joins por FKs
CREATE INDEX IF NOT EXISTS ix_persons_gender_id
  ON persons(gender_id);

CREATE INDEX IF NOT EXISTS ix_persons_identification_type_id
  ON persons(identification_type_id);

-- ============================================================================
-- Tabla: clients (relación 1:1 con persons)
-- Unicidad por person_id y por client_id.
-- ============================================================================

CREATE TABLE IF NOT EXISTS clients (
  id             BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  person_id      BIGINT       NOT NULL,
  client_id      VARCHAR(60)  NOT NULL,
  password_hash  VARCHAR(100) NOT NULL,
  active         BOOLEAN      NOT NULL,
  CONSTRAINT uk_clients_person_id UNIQUE (person_id),
  CONSTRAINT uk_clients_client_id UNIQUE (client_id),
  CONSTRAINT fk_clients_person
     FOREIGN KEY (person_id)
     REFERENCES persons(id)
     ON UPDATE RESTRICT
     ON DELETE RESTRICT
);

-- ============================================================================
-- Fin del script
-- ============================================================================
