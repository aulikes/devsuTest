-- Tabla principal de personas
CREATE TABLE IF NOT EXISTS persons (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  first_name VARCHAR(120) NOT NULL,
  last_Name VARCHAR(120) NOT NULL,
  birth_date DATE NOT NULL,
  address VARCHAR(200) NOT NULL,
  phone VARCHAR(40) NOT NULL,
  identification_number VARCHAR(40) NOT NULL,
  gender_id BIGINT NOT NULL,
  identification_type_id BIGINT NOT NULL
);

-- Unicidad por número de identificación
CREATE UNIQUE INDEX IF NOT EXISTS uk_persons_identification_number
  ON persons(identification_number);

-- Índices para FKs (performance de joins)
CREATE INDEX IF NOT EXISTS ix_persons_gender_id
  ON persons(gender_id);

CREATE INDEX IF NOT EXISTS ix_persons_identification_type_id
  ON persons(identification_type_id);

-- Llaves foráneas hacia catálogos
ALTER TABLE persons
  ADD CONSTRAINT fk_persons_gender
    FOREIGN KEY (gender_id)
    REFERENCES genders(id)
    ON UPDATE RESTRICT
    ON DELETE RESTRICT;

ALTER TABLE persons
  ADD CONSTRAINT fk_persons_ident_type
    FOREIGN KEY (identification_type_id)
    REFERENCES identification_types(id)
    ON UPDATE RESTRICT
    ON DELETE RESTRICT;
