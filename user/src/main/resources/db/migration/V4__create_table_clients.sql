-- Tabla de clientes (1:1 con persons)
CREATE TABLE IF NOT EXISTS clients (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  person_id BIGINT NOT NULL,
  client_id VARCHAR(60) NOT NULL,
  password_hash VARCHAR(100) NOT NULL,
  active BOOLEAN NOT NULL
);

-- Unicidad por person_id (relación 1:1) y por client_id (business key)
CREATE UNIQUE INDEX IF NOT EXISTS uk_clients_person_id
  ON clients(person_id);

CREATE UNIQUE INDEX IF NOT EXISTS uk_clients_client_id
  ON clients(client_id);

-- FK hacia persons
ALTER TABLE clients
  ADD CONSTRAINT fk_clients_person
    FOREIGN KEY (person_id)
    REFERENCES persons(id)
    ON UPDATE RESTRICT
    ON DELETE RESTRICT;
