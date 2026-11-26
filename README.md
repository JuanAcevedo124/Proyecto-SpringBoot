# CoWorkHub — Aplicación web con Spring Boot

API REST para una red de coworking: evita reservas duplicadas, controla las horas que consume cada miembro,
cobra las cancelaciones siempre con la misma regla y limita a cada usuario a lo que le corresponde.

**Stack:** Java 21 · Spring Boot 4.1.2 · Maven (wrapper incluido) · MySQL 8 · Spring Data JPA · Spring Security + JWT (JJWT) · springdoc-openapi (Swagger UI).

## 1. Crear la base de datos

La aplicación se conecta por defecto a `jdbc:mysql://localhost:3306/coworkhub` con usuario y contraseña `coworkhub`.

### Opción A — MySQL instalado

```sql
CREATE DATABASE coworkhub;
CREATE USER 'coworkhub'@'localhost' IDENTIFIED BY 'coworkhub';
GRANT ALL PRIVILEGES ON coworkhub.* TO 'coworkhub'@'localhost';
FLUSH PRIVILEGES;
```

### Opción B — Docker

```bash
docker run -d --name coworkhub-mysql -e MYSQL_DATABASE=coworkhub -e MYSQL_USER=coworkhub -e MYSQL_PASSWORD=coworkhub -e MYSQL_ROOT_PASSWORD=root -p 3306:3306 -v coworkhub-datos:/var/lib/mysql mysql:8.4
```

Espere a que `docker logs coworkhub-mysql` muestre `ready for connections`.

Si el puerto 3306 ya está ocupado, publique otro (por ejemplo `-p 3307:3306`) e indique la URL con la variable
`DB_URL` al arrancar (ver abajo).

## 2. Ejecutar el proyecto

```bash
mvn spring-boot:run          # o, sin Maven instalado:  ./mvnw spring-boot:run   (Windows: mvnw.cmd spring-boot:run)
```

Cada arranque recrea las tablas (`ddl-auto=create`) y carga los datos de prueba de `src/main/resources/import.sql`.

Variables de entorno opcionales:

| Variable | Por defecto | Para qué |
|---|---|---|
| `DB_URL` | `jdbc:mysql://localhost:3306/coworkhub` | Otra URL/puerto de MySQL, p. ej. `jdbc:mysql://localhost:3307/coworkhub` |
| `DB_USER` / `DB_PASSWORD` | `coworkhub` / `coworkhub` | Credenciales de la base |
| `PORT` | `8080` | Puerto HTTP |
| `JWT_SECRETO` | clave de desarrollo | Clave de firma del JWT (≥ 32 caracteres) |
| `SHOW_SQL` | `false` | `true` para ver el SQL y comprobar que no hay N+1 |
| `APP_ZONA` | zona del sistema | Zona horaria del reloj de negocio, p. ej. `America/Bogota` |

Ejemplo en PowerShell: `$env:DB_URL="jdbc:mysql://localhost:3307/coworkhub"; ./mvnw spring-boot:run`

## 3. Usuarios de prueba

> ⚠️ **Advertencia educativa:** estas credenciales y la clave JWT de `application.properties` son solo para
> desarrollo y están en el repositorio a propósito. En producción la clave se define con `JWT_SECRETO` y nunca se sube.
> Las contraseñas se guardan con BCrypt; las personas y correos son ficticios.

| Usuario | Contraseña | Rol | Miembro |
|---|---|---|---|
| `admin` | `admin123` | ADMIN | — |
| `recepcion` | `recep123` | RECEPTIONIST | — |
| `ana` | `ana123` | MEMBER | 1 — Ana Gomez (Premium) |
| `luis` | `luis123` | MEMBER | 2 — Luis Perez (Profesional) |

Otros miembros sin cuenta: 3 Carla Ruiz (Básico, ya en su máximo de reservas activas), 4 Diego Torres, 5 Elena Martinez (`SUSPENDED`).
Sedes: 1 Sede Centro (07:00–21:00) y 2 Sede Norte 24h (00:00–23:59). La sala 4 está inactiva.

## 4. Interfaz web (frontend)

Con el proyecto corriendo, abra <http://localhost:8080/>. Es un frontend en HTML, CSS y JavaScript puro
(`src/main/resources/static/`) que consume la misma API REST con el JWT de `/auth/login`.

| Rol | Qué ve |
|---|---|
| `ADMIN` | Inicio con resumen, disponibilidad, todas las reservas (confirmar, completar, cancelar, servicios), miembros y CRUD de catálogos |
| `RECEPTIONIST` | Lo mismo que ADMIN, pero los catálogos son de solo lectura |
| `MEMBER` | Sus horas del mes, disponibilidad, **solo sus** reservas (crear, ver, cambiar servicios, cancelar) y catálogos de solo lectura |

En la pantalla de inicio de sesión hay botones para completar los usuarios de prueba. Los errores del backend se
muestran con su código (por ejemplo `RN-01`). Las reglas y los permisos se validan siempre en el backend.

## 5. Swagger UI

- Swagger UI: <http://localhost:8080/doc/swagger-ui.html>
- OpenAPI JSON: <http://localhost:8080/v3/api-docs> (copia exportada en [`docs/openapi.json`](docs/openapi.json))

Haga `POST /auth/login`, copie el `token`, pulse **Authorize** y péguelo. El token vence a los 60 minutos.

```bash
curl -s -X POST http://localhost:8080/auth/login -H "Content-Type: application/json" -d '{"usuario":"admin","contrasena":"admin123"}'
curl -s http://localhost:8080/api/salas -H "Authorization: Bearer <token>"
```

## 6. Pruebas

| Qué | Cómo |
|---|---|
| Pruebas unitarias de reglas (RN-02 a RN-09, costos, límites exactos 24 h / 2 h) | `./mvnw test` |
| 14 escenarios de aceptación + 14 pruebas extra (97 aserciones) | Importar `postman/CoWorkHub.postman_collection.json` en Postman/Insomnia y ejecutarla en orden, o `npx newman run postman/CoWorkHub.postman_collection.json` (variable `baseUrl`, por defecto `http://localhost:8080`) |
| Concurrencia real RN-01 (N solicitudes simultáneas, solo una gana) | `bash scripts/prueba-concurrencia.sh http://localhost:8080 6` |

Reinicie la aplicación antes de ejecutar la colección para partir de los datos de `import.sql`.

## 7. Arquitectura

```
com.coworkhub
├── config        AppConfig (Clock, PasswordEncoder), SecurityConfig, OpenApiConfig, AutorizacionRecursos, ValidadorConfiguracion
├── jwt           JwtService, JwtAuthenticationFilter, UsuarioAutenticado
├── presentacion
│   ├── controlador   controladores REST (sin acceso a repositorios)
│   ├── dto           DTO record de entrada (@Valid) y salida
│   └── excepcion     @RestControllerAdvice, cuerpo de error único, manejadores 401/403, /error
├── negocio
│   ├── model         entidades JPA y enums
│   ├── service       interfaces + impl, CalculadoraCostos, ContadorHoras, ReglasReserva
│   └── excepcion     excepciones propias (sin HTTP)
└── persistence.repository   repositorios Spring Data
```

El análisis (actores, glosario, ER justificado, trazabilidad, supuestos y fuera de alcance) está en
[`docs/analisis.md`](docs/analisis.md).

Formato de error, igual en todos los casos (incluidos JSON mal formado, ruta inexistente, método no permitido, 401 y 403):

```json
{"error": "La sala ya esta reservada en ese horario", "codigo": "RN-01", "timestamp": "2030-06-03T13:00:00Z"}
```
