# Guía de despliegue con Docker Compose

Este `docker-compose.yml` levanta **dos bases PostgreSQL** (una por microservicio) y **dos APIs**:  
- `user` (gestión de clientes)  
- `transaction` (cuentas y movimientos; consulta clientes vía Feign al servicio `user`)

---

## 1) Requisitos

- Docker Desktop o Docker Engine con **Compose V2** (`docker compose`).
- Puertos libres en el host:
  - `5434` → PostgreSQL de **user** (opcional exponer)
  - `5435` → PostgreSQL de **transaction** (opcional exponer)
  - `8090` → API **user**
  - `8091` → API **transaction**
- Proyecto con esta estructura:
  ```text
  .
  ├─ docker-compose.yml
  ├─ user/
  │   └─ Dockerfile
  └─ transaction/
      └─ Dockerfile
  ```
- Cada microservicio con perfil `docker` y Flyway activado.

---

## 2) Qué levanta cada servicio

### 2.1 Bases de datos

- **postgres-clients**
  - Imagen: `postgres:16-alpine`
  - BD: `clients`
  - Credenciales: `postgres/postgres`
  - Volumen: `pg_clients` (persistencia)
  - Puerto host: `5434` → contenedor `5432` (exposición opcional)
  - Healthcheck con `pg_isready`

- **postgres-transactions**
  - Imagen: `postgres:16-alpine`
  - BD: `transactions`
  - Credenciales: `postgres/postgres`
  - Volumen: `pg_transactions` (persistencia)
  - Puerto host: `5435` → contenedor `5432` (exposición opcional)
  - Healthcheck con `pg_isready`

### 2.2 APIs

- **user** (API clientes)
  - Build desde `./user/Dockerfile`
  - Perfil activo: `docker`
  - Datasource: `jdbc:postgresql://postgres-clients:5432/clients`
  - Depende de `postgres-clients` (espera **healthy**)
  - Expone `8090:8090`

- **transaction** (API transacciones)
  - Build desde `./transaction/Dockerfile`
  - Perfil activo: `docker`
  - Datasource: `jdbc:postgresql://postgres-transactions:5432/transactions`
  - Feign hacia `user`: `CLIENTS_BASE_URL=http://user:8090/clientes`
    - Con esto, la interfaz Feign `@GetMapping("/clientId/{clientId}")` resuelve `http://user:8090/clientes/clientId/{clientId}`
  - Depende de `postgres-transactions` (healthy) y `user` (started)
  - Expone `8091:8091`

> Compose crea una red por defecto; los contenedores se resuelven por **nombre de servicio** (`user`, `postgres-clients`, etc.).

---

## 3) Uso básico

### 3.1 Arranque

- **Todo el stack** (recomendado):
  ```bash
  docker compose -p devsu up -d --build
  ```
- **Solo una API** (por ejemplo, recompilar `transaction`):
  ```bash
  docker compose -p devsu up -d --build transaction
  ```

### 3.2 Estado y logs

```bash
docker compose -p devsu ps
docker compose -p devsu logs -f user
docker compose -p devsu logs -f transaction
```

### 3.3 Parada y limpieza

- Parar y eliminar contenedores/red (conserva datos en volúmenes):
  ```bash
  docker compose -p devsu down
  ```
- Reset total (incluye borrar volúmenes → limpia BDs):
  ```bash
  docker compose -p devsu down -v
  ```

---

## 4) Verificación rápida (smoke test)

Una vez arriba:

- **Swagger**:
  - `user`: `http://localhost:8090/swagger-ui/index.html`
  - `transaction`: `http://localhost:8091/swagger-ui/index.html`
- **Ping interno transaction → user**:
  ```bash
  docker exec -it devsu-transaction sh -lc '
    (which curl >/dev/null 2>&1) || ( (which apk && apk add --no-cache curl) || (which apt-get && apt-get update && apt-get install -y curl -qq) || true );
    curl -i http://user:8090/actuator/health
  '
  ```

---

## 5) Flujo típico de prueba manual

1) **Crear cliente** en `user`:
   ```bash
   curl -i -X POST http://localhost:8090/clientes      -H "Content-Type: application/json"      -d '{
       "firstName":"Ana","lastName":"Gómez","gender":"FEMALE","birthDate":"2000-01-01",
       "identificationType":"CC","identificationNumber":"1234567890",
       "address":"Calle 1","phone":"3001234567","password":"superSecret!"
     }'
   ```
   Consultar por `clientId` (el que devuelva/defina tu API):
   ```bash
   curl -i http://localhost:8090/clientes/clientId/CLI-0001
   ```

2) **Crear cuenta** en `transaction` (Feign consulta cliente en `user`):
   ```bash
   curl -i -X POST http://localhost:8091/cuentas      -H "Content-Type: application/json"      -d '{
       "accountType": "AHORROS",
       "clientId": "CLI-0001",
       "initialBalance": 500000.00
     }'
   ```

3) **Consultar movimientos**:
   ```bash
   curl -i "http://localhost:8091/movimientos?accountNumber=514228468671&from=2025-01-01&to=2025-12-31"
   ```

---

## 6) Variables de entorno relevantes

### 6.1 user
- `SPRING_PROFILES_ACTIVE=docker`
- `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-clients:5432/clients`
- `SPRING_DATASOURCE_USERNAME=postgres`
- `SPRING_DATASOURCE_PASSWORD=postgres`

### 6.2 transaction
- `SPRING_PROFILES_ACTIVE=docker`
- `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres-transactions:5432/transactions`
- `SPRING_DATASOURCE_USERNAME=postgres`
- `SPRING_DATASOURCE_PASSWORD=postgres`
- `CLIENTS_BASE_URL=http://user:8090/clientes`  
  (La base URL **incluye** `/clientes` porque el método Feign es `@GetMapping("/clientId/{clientId}")`)

---

## 7) Consejos y resolución de problemas

- **404 “Client not found in user-service”**:
  - Confirmar que el `clientId` existe en la BD de `user` (en Docker es una BD **separada** de local).
  - Verificar desde **transaction**:
    ```bash
    docker exec -it devsu-transaction sh -lc 'curl -i http://user:8090/clientes/clientId/EL_CLIENT_ID'
    ```
  - Asegurar que el body de `POST /cuentas` envía `clientId` como **string** y coincide exactamente.
  - Opcional: activar logs de Feign en `transaction` para ver la URL exacta:
    ```yaml
    logging.level.feign=DEBUG
    feign.client.config.default.loggerLevel=FULL
    ```

- **Flyway “checksum mismatch”**:
  - No modificar migraciones aplicadas (`Vx__...sql`); crear una nueva (`V(x+1)__...sql`).
  - En dev, si ya se cambió y no importa el histórico:  
    - Reparar con CLI de Flyway o actualizar `flyway_schema_history.checksum`.
  - Para reset completo: `docker compose -p devsu down -v` y volver a levantar.

- **“Algo quedó pegado”** (variables o imágenes):
  - Reconstruir:
    ```bash
    docker compose -p devsu up -d --build
    ```
  - Sin cache:
    ```bash
    docker compose -p devsu build --no-cache transaction
    docker compose -p devsu up -d transaction
    ```

- **Conexión desde el host a Postgres**:
  - `user`: `localhost:5434`  
    `psql -h localhost -p 5434 -U postgres -d clients`
  - `transaction`: `localhost:5435`  
    `psql -h localhost -p 5435 -U postgres -d transactions`

---

## 8) Matriz de puertos (host → contenedor)

| Servicio                | Host | Contenedor |
|-------------------------|------|------------|
| postgres-clients (user) | 5434 | 5432       |
| postgres-transactions   | 5435 | 5432       |
| user (API)              | 8090 | 8090       |
| transaction (API)       | 8091 | 8091       |

---

## 9) Comandos útiles

- Levantar todo:
  ```bash
  docker compose -p devsu up -d --build
  ```
- Solo `transaction`:
  ```bash
  docker compose -p devsu up -d --build transaction
  ```
- Reiniciar `transaction`:
  ```bash
  docker compose -p devsu restart transaction
  ```
- Reset de datos:
  ```bash
  docker compose -p devsu down -v
  docker compose -p devsu up -d --build
  ```

