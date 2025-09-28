# Transaction Service — README

## 1. Descripción general

El servicio **Transaction** gestiona **cuentas** y **movimientos** bancarios, y expone un endpoint de **reportes** de estados de cuenta. 

> El perfil activo se define vía variable `SPRING_PROFILES_ACTIVE` o con `-Dspring.profiles.active=<perfil>`.

---

## 2. Endpoints

### 2.1 Swagger / OpenAPI

- UI: `http://localhost:8090/swagger-ui/index.html`
- Docs: `http://localhost:8090/v3/api-docs`


### 2.2. Endpoints principales

#### 2.2.1 Cuentas

- **POST `/cuentas`** — Crea una cuenta.
  - Request (JSON): `CreateAccountRequest { accountType, clientId, initialBalance }`
  - Response: `201 Created` + `Location: /cuentas/{accountNumber}` y body `AccountResponse`
- **PATCH `/cuentas/{accountNumber}/estado`** — Cambia el estado activo/inactivo.
  - Request (JSON): `ChangeAccountStatusRequest { active }`
  - Response: `200 OK` con `AccountResponse`
- **GET `/cuentas/{accountNumber}`** — Obtiene detalle.
  - Response: `200 OK` con `AccountResponse`

#### 2.2.2 Movimientos

- **POST `/movimientos`** — Registra un movimiento (depósito/retiro).
  - Request (JSON): `CreateMovementRequest { accountNumber, amount, description }`
  - Response: `201 Created` + `Location: /movimientos/{id}` y body `MovementResponse`
  - Errores relevantes: `422 Unprocessable Entity` si no hay fondos.
- **GET `/movimientos?accountNumber=...&from=YYYY-MM-DD&to=YYYY-MM-DD`**
  - Response: `200 OK` con `List<MovementResponse>`

#### 2.2.3 Reportes

- **GET `/reportes?clientId=...&from=YYYY-MM-DD&to=YYYY-MM-DD`**
  - Response: `200 OK` con `AccountStatementReport`



## 3. Despliegue SIN Docker Compose (perfil **dev**)

Este camino usa una base de datos PostgreSQL existente (local o remota).

### 3.1 Requisitos
- JDK 21 y Gradle Wrapper.
- Una instancia **PostgreSQL** accesible (se recomienda 16).  
  Puede ser local, en contenedor (con `docker run`) o remota.
- Credenciales y base de datos creadas (por ejemplo: `user/pass` sobre `userdb`).

> Opción rápida con Docker **(no es Compose)** para levantar solo PostgreSQL:
> ```bash
> docker run --name user-postgres >   -e POSTGRES_USER=user >   -e POSTGRES_PASSWORD=pass >   -e POSTGRES_DB=clientes >   -p 5434:5432 -d postgres:16-alpine
> ```

### 3.2 Configurar `application-dev.yml`
Ubicación típica: `src/main/resources/application-dev.yml`.  
Valores de ejemplo (ajustar a su entorno):

```yaml
server:
  port: 8091

spring:
  datasource:
    url: jdbc:postgresql://localhost:5434/transactions
    username: user
    password: pass
```

### 3.3 Ejecutar la aplicación
```bash
./gradlew bootRun -Dspring.profiles.active=dev
```
La API quedará disponible en `http://localhost:8091` (ajustable en `server.port`).

---

## 4. Despliegue CON Docker Compose (perfil **docker**)

La aplicación puede desplegarse con Docker Compose para simplificar el arranque local.
Este camino levanta **API** + **PostgreSQL** con un solo comando.

### 4.1 Requisitos
- Docker (Desktop o Engine) con Compose V2 (`docker compose`).
- Puertos libres en el host:
  - **8090** para exponer la API (internamente la app escucha en 8090).
  - **5434** para exponer PostgreSQL.

### 4.2 Variables de entorno relevantes (resumen)
El archivo docker-compose.yml ya está configurado para levantar el ambiente correctamente.
Sin embargo, se dejan muestran las variables a configurar, dependiendo su entorno:

- `SPRING_PROFILES_ACTIVE=docker`
- `SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/clientes`
- `SPRING_DATASOURCE_USERNAME=user`
- `SPRING_DATASOURCE_PASSWORD=pass`
- `SPRING_FLYWAY_ENABLED=true`
- `SPRING_FLYWAY_LOCATIONS=classpath:db/migration`
- Variables de dependencias externas si aplican, por ejemplo:  
  `CLIENTS_BASE_URL=http://host.docker.internal:8090/clientes/`  
  En Linux, si `host.docker.internal` no resuelve, se puede usar la IP del host o `extra_hosts` en Compose.

### 4.3 Comandos útiles
```bash
# Levantar en segundo plano
docker compose -p devsu up -d

# Ver estado de servicios
docker compose -p devsu ps

# Detener y conservar datos
docker compose -p devsu down

# Detener y borrar volúmenes (datos)
docker compose -p devsu down -v
```

---

## 5. Pruebas

### 5.1 Pruebas unitarias y de controller (sin base de datos)
```bash
./gradlew test
```
Reportes:
- HTML: `build/reports/tests/test/index.html`
- XML: `build/test-results/test/`

> Los controllers se prueban con `@WebMvcTest` y `@MockitoBean`; no requieren base de datos.

### 5.2 Pruebas E2E con Testcontainers (PostgreSQL efímero)
- Requisitos: Docker activo (`docker ps`).
- Clases marcadas con `@Tag("e2e")` y `@EnabledIfEnvironmentVariable(named = "RUN_E2E", matches = "true")`.
- La tarea `e2eTest` exporta `RUN_E2E=true` y ejecuta solo E2E.
```bash
./gradlew clean e2eTest
```
Reportes:
- HTML: `build/reports/tests/e2eTest/index.html`
- XML: `build/test-results/e2eTest/`

> Las E2E usan `@ServiceConnection` para que la app apunte al contenedor de PostgreSQL. Flyway migra antes de JPA, por lo que no toca su BD local.

---

## 6. Migraciones (Flyway)
- Ruta: `src/main/resources/db/migration`

---

## 7. Ejemplos cURL

Crear cuenta:
```bash
curl -i -X POST http://localhost:8091/cuentas   -H "Content-Type: application/json"   -d '{"accountType":"SAVINGS","clientId":"c123","initialBalance":1000.0}'
```

Cambiar estado:
```bash
curl -i -X PATCH http://localhost:8091/cuentas/1234567890/estado   -H "Content-Type: application/json"   -d '{"active":false}'
```

Crear movimiento:
```bash
curl -i -X POST http://localhost:8091/movimientos   -H "Content-Type: application/json"   -d '{"accountNumber":"1234567890","amount":-50.0,"description":"Retiro ATM"}'
```

Listar movimientos:
```bash
curl -s "http://localhost:8091/movimientos?accountNumber=1234567890&from=2025-01-01&to=2025-12-31" | jq
```

Reporte:
```bash
curl -s "http://localhost:8091/reportes?clientId=c123&from=2025-01-01&to=2025-12-31" | jq
```
