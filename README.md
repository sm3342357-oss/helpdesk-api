# API de Mesa de Ayuda (Helpdesk) con SLA

API REST construida con **Spring Boot 3.2** + **Spring Security JWT** para gestión de tickets de soporte técnico con cálculo automático de SLA.

## Stack Tecnológico

| Componente       | Tecnología                  |
|------------------|-----------------------------|
| Lenguaje         | Java 17+                    |
| Framework        | Spring Boot 3.2             |
| Seguridad        | Spring Security + JWT       |
| Persistencia     | Spring Data JPA             |
| Base de datos    | H2 (en memoria)             |
| Tokens           | jjwt 0.12.5                 |

## Estrategia de Refresh Token

Se implementa la **Opción A: Refresh Token persistido en base de datos**.

### Por qué esta opción

- **Revocación real**: el logout invalida el token en la DB, no solo del lado del cliente.
- **Control de sesiones**: permite saber qué tokens están activos y cuándo expiraron.
- **Didáctica**: obliga a razonar sobre el ciclo de vida completo del token.

### Modelo RefreshToken

| Campo    | Tipo      | Descripción                          |
|----------|-----------|--------------------------------------|
| id       | Long      | PK auto-generado                     |
| token    | String    | Valor del refresh token (único)      |
| usuario  | Usuario   | Dueño del token                      |
| expiraEn | DateTime  | Fecha de expiración (7 días)         |
| revocado | Boolean   | `true` cuando se hace logout o rotación |

### Flujo de tokens

1. **Login** → devuelve `accessToken` (15 min) + `refreshToken` (7 días).
2. **Uso normal** → el cliente envía `Authorization: Bearer <accessToken>`.
3. **Cuando expira (401)** → llama a `POST /api/auth/refresh` con el `refreshToken`.
4. **Renovación** → el servidor valida el refresh token, lo **revoca** (rotación), y devuelve nuevos tokens.
5. **Logout** → el refresh token se marca como revocado. Un intento posterior de refresh falla con 401.

### Ventajas sobre la Opción B (stateless)

| Opción A (DB)         | Opción B (JWT stateless)       |
|-----------------------|--------------------------------|
| Revocación inmediata  | No se puede revocar fácilmente |
| Control de sesiones   | Sin control en servidor         |
| Logout real           | Logout solo del lado cliente    |

## Endpoints

### Públicas (sin autenticación)

| Método | Ruta                | Descripción                    |
|--------|---------------------|--------------------------------|
| POST   | /api/auth/registro  | Registrar usuario (rol USUARIO)|
| POST   | /api/auth/login     | Login → accessToken+refreshToken|
| POST   | /api/auth/refresh   | Renovar accessToken            |
| GET    | /api/ping           | Verificar API → "pong"         |

### Protegidas (cualquier usuario logueado)

| Método | Ruta                  | Descripción                       |
|--------|-----------------------|-----------------------------------|
| POST   | /api/auth/logout      | Revoca el refresh token           |
| POST   | /api/tickets          | Crear ticket                      |
| GET    | /api/tickets/mios     | Listar tickets del usuario        |
| GET    | /api/tickets/{id}     | Ver ticket (dueño o SOPORTE/ADMIN)|
| GET    | /api/auth/me          | Ver usuario autenticado           |

### Protegidas por rol

| Método | Ruta                      | Rol            | Descripción               |
|--------|---------------------------|----------------|---------------------------|
| GET    | /api/tickets              | SOPORTE, ADMIN | Listar todos los tickets  |
| PATCH  | /api/tickets/{id}/estado  | SOPORTE, ADMIN | Cambiar estado del ticket |
| GET    | /api/tickets/vencidos     | SOPORTE, ADMIN | Tickets que pasaron SLA   |
| POST   | /api/admin/soporte        | ADMIN          | Ascender usuario a SOPORTE|

## Cálculo de SLA

El servidor calcula `slaVenceEn` automáticamente al crear el ticket:

| Prioridad | SLA         |
|-----------|-------------|
| ALTA      | 4 horas     |
| MEDIA     | 24 horas    |
| BAJA      | 72 horas    |

Un ticket se considera **vencido** cuando `LocalDateTime.now() > slaVenceEn` y su estado no es `RESUELTO`.

## Ejecución

```bash
# Requisitos
# - Java 17+
# - Maven 3.8+

# Compilar y ejecutar
mvn spring-boot:run

# La API arranca en http://localhost:8080
# Consola H2: http://localhost:8080/h2-console
```

## Pruebas

Importar la colección Postman desde `postman/HelpdeskAPI.postman_collection.json`.

### Flujo de prueba rápido

1. `POST /api/auth/registro` → crear usuario
2. `POST /api/auth/login` → obtener tokens
3. `POST /api/tickets` → crear ticket con Bearer token
4. `GET /api/tickets/mios` → listar mis tickets
5. `PATCH /api/tickets/{id}/estado` → cambiar estado (requiere SOPORTE/ADMIN)
6. `POST /api/auth/refresh` → renovar access token
7. `POST /api/auth/logout` → revocar refresh token
8. Intentar refresh con token revocado → debe devolver 401
