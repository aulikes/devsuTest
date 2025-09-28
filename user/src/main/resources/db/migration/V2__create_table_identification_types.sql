CREATE TABLE IF NOT EXISTS identification_types (
  id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
  code VARCHAR(16) NOT NULL,
  name VARCHAR(64) NOT NULL
);
CREATE UNIQUE INDEX IF NOT EXISTS uk_ident_types_code ON identification_types(code);

INSERT INTO identification_types(code, name) VALUES
    ('CC', 'Cédula de ciudadanía'),
    ('CE', 'Cédula de extranjería'),
    ('PAS', 'Pasaporte'),
    ('NIT', 'NIT')
ON CONFLICT (code) DO NOTHING;
