# User Service (Spring Boot)

Servicio para la gestión de **clientes**. Expone endpoints para **crear**, **consultar** (por `id` y por `clientId`), **actualizar** y **eliminar**. Incluye validaciones, migraciones con **Flyway**, mapeos con **MapStruct**, pruebas de **controllers** (sin base de datos) y **E2E** aisladas con **Testcontainers**.

---

## 1. Perfiles de Spring (proyecto)

- **dev**: desarrollo local con base de datos PostgreSQL real. Hibernate se configura en modo `validate` y **Flyway** migra el esquema al inicio.
- **test**: destinado a pruebas unitarias y de controller. No requiere base de datos (los controllers se prueban con `@WebMvcTest` y dependencias mockeadas).
- **docker**: ejecución dentro de contenedores (por ejemplo, con Docker Compose). La conexión a base de datos se hace al servicio `postgres` de la red de Compose.
- **it** (si existe en el repo): perfil opcional para pruebas de integración. En este proyecto, las E2E se ejecutan con Testcontainers y overrides en las clases de test, sin requerir un perfil adicional.

> El perfil activo se define vía variable `SPRING_PROFILES_ACTIVE` o con `-Dspring.profiles.active=<perfil>`.

---

## 2. Endpoints

### 2.1 Swagger / OpenAPI

- UI: `http://localhost:8090/swagger-ui/index.html`
- Docs: `http://localhost:8090/v3/api-docs`

### 2.2 Endpoints principales

- **POST `/clientes`** — Crea un cliente.  
  Respuestas: `201 Created`, `400 Bad Request` (validación/JSON), `404 Not Found` (catálogos inválidos), `409 Conflict` (identificación duplicada), `500 Internal Server Error`.

- **GET `/clientes/{id}`** — Obtiene por `id`.  
  Respuestas: `200 OK`, `404 Not Found`, `400 Bad Request` (id no numérico).

- **GET `/clientes/clientId/{clientId}`** — Obtiene por `clientId`.  
  Respuestas: `200 OK`, `404 Not Found`.

- **PUT `/clientes/{id}`** — Actualiza cliente.  
  Respuestas: `200 OK`, `400 Bad Request`, `404 Not Found` (no existe o catálogos inválidos), `409 Conflict`, `500`.

- **DELETE `/clientes/{id}`** — Elimina cliente.  
  Respuestas: `204 No Content`, `404 Not Found`, `400 Bad Request` (id no numérico).

---

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
  port: 8090

spring:
  datasource:
    url: jdbc:postgresql://localhost:5434/clientes
    username: user
    password: pass
```

### 3.3 Ejecutar la aplicación
```bash
./gradlew bootRun -Dspring.profiles.active=dev
```
La API quedará disponible en `http://localhost:8090` (ajustable en `server.port`).

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
  `CLIENTS_BASE_URL=http://host.docker.internal:8090/clientes/clientId/`  
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

Crear:
```bash
curl -i -X POST http://localhost:8090/clientes   -H "Content-Type: application/json"   -d '{
    "firstName":"Ana","lastName":"Gómez","gender":"FEMALE","birthDate":"2000-01-01",
    "identificationType":"CC","identificationNumber":"1234567890",
    "address":"Calle 1 # 2-3","phone":"3001234567","password":"superSecret!"
  }'
```

Obtener por id:
```bash
curl -i http://localhost:8090/clientes/1
```

Obtener por clientId:
```bash
curl -i http://localhost:8090/clientes/clientId/CLI-0001
```

Actualizar:
```bash
curl -i -X PUT http://localhost:8090/clientes/1   -H "Content-Type: application/json"   -d '{
    "firstName":"Ana María","lastName":"Gómez R.","gender":"FEMALE","birthDate":"2000-02-02",
    "identificationType":"CC","identificationNumber":"1234567890",
    "address":"Calle 10 # 20-30","phone":"3015550000","password":"newPass123"
  }'
```

Eliminar:
```bash
curl -i -X DELETE http://localhost:8090/clientes/1
```

---
