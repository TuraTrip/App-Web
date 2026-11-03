# TuraTrip — Backend

API REST construida con Spring Boot 4 y Java 21. La consumen la web (`../frontend`) y la
[app móvil Android](#-app-móvil).

## Requisitos para desarrollo local (sin Docker)

- Java 21+
- Maven 3.9+
- MySQL 9.7

> Con Docker no necesitas instalar nada. Usa `docker compose up` desde la raíz del proyecto.

## Variables de entorno

Copia `.env.example` en la raíz del proyecto y ajusta los valores:

| Variable                                                      | Descripción                                                                                                                                                                                                   | Default dev                                                        |
| :------------------------------------------------------------ | :------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ | :----------------------------------------------------------------- |
| `DB_URL`                                                      | URL JDBC de conexión a MySQL                                                                                                                                                                                  | `jdbc:mysql://127.0.0.1:3306/turatrip_db...`                       |
| `DB_USERNAME`                                                 | Usuario de la base de datos                                                                                                                                                                                   | `root`                                                             |
| `DB_PASSWORD`                                                 | Contraseña de la base de datos                                                                                                                                                                                | `root`                                                             |
| `DB_DRIVER`                                                   | Driver JDBC                                                                                                                                                                                                   | `com.mysql.cj.jdbc.Driver`                                         |
| `HIBERNATE_DIALECT`                                           | Dialecto de Hibernate (opcional, se autodetecta)                                                                                                                                                              | —                                                                  |
| `JPA_DDL_AUTO`                                                | Estrategia de `ddl-auto` (usar `validate` en prod; el esquema lo gestiona Flyway)                                                                                                                             | `update`                                                           |
| `JWT_SECRET`                                                  | Secreto para firmar tokens JWT (mín. 32 chars)                                                                                                                                                                | `9780fbdc24f409a10c25bd5120ed1d484b47d5a4c78c73a834d2fcb50d293362` |
| `JWT_EXPIRATION_MS`                                           | Duración del token en milisegundos                                                                                                                                                                            | `86400000` (24h)                                                   |
| `FRONTEND_URL`                                                | Origin permitido para CORS y base de los enlaces en los correos. En local: `http://localhost:3000`. En producción: la URL pública de tu dominio, **sin** `/api` ni slash final (ej. `https://tu-dominio.com`) | `http://localhost:3000`                                            |
| `API_URL`                                                     | Solo se usa en el build del frontend para construir `NEXT_PUBLIC_API_URL`. En local: `http://localhost:8080/api`. En producción: tu dominio **con** `/api` al final (ej. `https://tu-dominio.com/api`)        | `http://localhost:8080/api`                                        |
| `SMTP_HOST` / `SMTP_PORT` / `SMTP_USERNAME` / `SMTP_PASSWORD` | Servidor SMTP para correos de verificación/recuperación                                                                                                                                                       | —                                                                  |
| `UPLOADS_DIR`                                                 | Carpeta donde se guardan las imágenes subidas                                                                                                                                                                 | `/app/uploads`                                                     |
| `MAX_FILE_SIZE` / `MAX_REQUEST_SIZE`                          | Límites por archivo y por petición. Una publicación puede llevar **10 fotos**, así que `MAX_REQUEST_SIZE` debe ser al menos `60MB`                                                                             | `10MB` / `60MB`                                                   |

> ⚠️ `API_URL` y `FRONTEND_URL` no son intercambiables: `API_URL` lleva `/api` (se concatena con rutas como `/v1/auth/login`), `FRONTEND_URL` no lleva nada extra (Spring Security lo usa tal cual como `Origin` permitido en CORS).

### Correo (SMTP)

El envío de correos (`EmailServiceImpl`) es asíncrono (`@Async`) y solo registra el error en el log si falla — nunca hace fallar el registro/login. Si un correo no llega:

1. Revisa los logs: `docker logs turatrip-backend-prod | grep -i correo`.
2. Si dice "Correo enviado con éxito" pero no llega, revisa Spam primero.
3. La config actual (`spring.mail.properties.mail.smtp.starttls.enable=true`) solo funciona con **puerto 587 (STARTTLS)**. Si usas puerto 465 (SSL implícito), hay que ajustar la propiedad a `mail.smtp.ssl.enable=true`.
4. En producción, usa un dominio propio + proveedor transaccional (Resend, Brevo, SES) con SPF/DKIM/DMARC — una cuenta Gmail personal como remitente automático suele terminar en Spam sin importar la configuración del código.

## Ejecutar sin Docker

Abre una terminal en el proyecto y ejecuta:

```bash
cd backend
./mvnw spring-boot:run
```

o abrela terminal directamente en la carpeta backend del proyecto y ejecuta:

```bash
./mvnw spring-boot:run
```

## Inicialización de datos

Al arrancar, el sistema crea automáticamente los roles `USUARIO` y `ADMIN`
si no existen en la base de datos (via `DataInitializer`).

Para dar rol de administrador a una cuenta (por ejemplo, para moderar denuncias):

```sql
INSERT INTO usuario_roles (usuario_id, rol_id)
SELECT u.id, r.id FROM usuarios u, roles r WHERE u.correo = 'correo@ejemplo.com' AND r.nombre = 'ADMIN';
```

## Cambios en base de datos

Las migraciones están en `src/main/resources/db/migration` y **Flyway las aplica solas al arrancar**.

| Migración | Cambio |
| :--- | :--- |
| `V1` (SUG-1) | Tablas `usuarios`, `roles`, `usuario_roles`, `tokens` |
| `V2` | `foto_perfil` en usuarios |
| `V3` | Tabla `publicaciones` |
| `V4` | Ids a `BIGINT` |
| `V5` | Tabla `categorias` y relación con publicaciones |
| `V6` | Tabla `comentarios` |
| `V7` | Ubicación (`direccion`, `latitud`, `longitud`) en publicaciones |
| `V8` | `biografia` en usuarios, tablas `me_gusta` y `seguimientos`, índice de ubicación y borrado en cascada de los comentarios |
| `V9` | `premium_hasta` en usuarios (perfil verificado) |
| `V10` | Tabla `publicacion_imagenes` (hasta 10 fotos por publicación); las fotos existentes se migran |
| `V11` | Tabla `guardados` (privados) |
| `V12` | Tabla `denuncias`; columnas `oculta` (publicaciones) y `oculto` (comentarios) |

> ⚠️ Nunca cambies una migración que ya se aplicó en algún servidor: Flyway compara su checksum
> y el backend no arranca. Para cambiar el esquema, crea la siguiente (`V13__...`).

## Despliegue en producción

Ver la sección [🚢 Despliegue en Producción](../README.md#-despliegue-en-producción-cicd) en el README principal para el flujo de CI/CD, la configuración de nginx como reverse proxy y los secrets requeridos en GitHub Actions.

## Endpoints disponibles

La documentación completa e interactiva de la API se genera automáticamente con springdoc-openapi y vive en Swagger UI:

- Local: `http://localhost:8080/swagger-ui.html`
- Especificación OpenAPI (JSON): `http://localhost:8080/v3/api-docs`
- Documentación pública en línea (hosteada): _próximamente_

> Mantener aquí una tabla manual de endpoints se desactualiza rápido a medida que el proyecto crece; Swagger siempre refleja el estado real del código porque se genera desde las anotaciones de los controladores.

Resumen por recurso (todas las rutas empiezan por `/api/v1`). "Opcional" significa que funciona sin
sesión, pero con token la respuesta incluye datos del usuario (si le gusta, si la guardó, si sigue al autor…).

### Autenticación

| Método | Ruta | Auth | Descripción |
| :--- | :--- | :--- | :--- |
| `POST` | `/auth/registro` | ❌ Público | Registro de usuario con rol USUARIO |
| `POST` | `/auth/login` | ❌ Público | Devuelve el token JWT |
| `GET` | `/auth/verificar?token=` | ❌ Público | Verifica la cuenta con el enlace del correo |
| `POST` | `/auth/reenviar-verificacion` | ❌ Público | Reenvía el correo de verificación |
| `POST` | `/auth/restablecer-clave/solicitar`, `/confirmar` | ❌ Público | Cambio de contraseña por correo |

### Publicaciones

| Método | Ruta | Auth | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/publicaciones?page=&size=` | Opcional | Feed (sin las ocultas por moderación) |
| `GET` | `/publicaciones/{id}` | Opcional | Detalle. Una publicación oculta solo la ven su autor y los admins |
| `GET` | `/publicaciones/buscar?q=` | Opcional | Busca todas las palabras en título, descripción, lugar, autor y categoría (sin tildes) |
| `GET` | `/publicaciones/cercanas?lat=&lon=&radioKm=&limite=` | Opcional | Ordenadas por distancia |
| `GET` | `/publicaciones/mapa` | Opcional | Las que tienen ubicación (máx. 500) |
| `GET` | `/publicaciones/usuario/{id}` | Opcional | De un usuario (el propio autor ve también las ocultas) |
| `GET` | `/publicaciones/mias` | ✅ | Del usuario de la sesión |
| `GET` | `/publicaciones/siguiendo` | ✅ | De las cuentas que sigue |
| `GET` | `/publicaciones/me-gusta`, `/guardadas` | ✅ | Con "me gusta" / guardadas por el usuario |
| `POST` | `/publicaciones` (multipart) | ✅ | Parte `publicacion` (JSON) + 1 a 10 partes `archivos`. Acepta también una sola `archivo` (compatibilidad con la web) |
| `PUT` | `/publicaciones/{id}` (JSON) | ✅ Autor/admin | Edita título, descripción, categoría y ubicación |
| `PUT` | `/publicaciones/{id}/imagenes` (multipart) | ✅ Autor/admin | Parte `orden` (JSON: ids que se conservan y `"nueva:N"`) + `archivos` nuevos. Borra del disco las fotos quitadas |
| `DELETE` | `/publicaciones/{id}` | ✅ Autor/admin | Borra la publicación y sus fotos |
| `POST` / `DELETE` | `/publicaciones/{id}/me-gusta` | ✅ | Da o quita "me gusta"; devuelve el total |
| `POST` / `DELETE` | `/publicaciones/{id}/guardar` | ✅ | Guarda o quita de guardados (privado) |
| `GET` / `POST` | `/publicaciones/{id}/comentarios` | Público / ✅ | Lista o crea comentarios |
| `PUT` | `/publicaciones/{id}/comentarios/{cid}` | ✅ Autor | Edita el comentario |
| `DELETE` | `/publicaciones/{id}/comentarios/{cid}` | ✅ | Su autor, el autor de la publicación o un admin |

### Usuarios

| Método | Ruta | Auth | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` / `PUT` | `/usuarios/perfil` | ✅ | Perfil propio (nombre y biografía) |
| `GET` | `/usuarios/perfil/{id}` | Opcional | Perfil ajeno con contadores, verificado y si lo sigues. **Sin correo** |
| `POST` | `/usuarios/perfil/foto` | ✅ | Foto de perfil |
| `GET` | `/usuarios/buscar?q=` | ❌ Público | Cuentas por nombre (máx. 20, sin correo) |
| `POST` / `DELETE` | `/usuarios/{id}/seguir` | ✅ | Seguir / dejar de seguir |
| `GET` | `/usuarios/{id}/seguidores`, `/seguidos` | ❌ Público | Listas |
| `GET` | `/usuarios/{id}/comentarios?page=&size=` | ❌ Público | Comentarios que ha hecho el usuario |
| `POST` / `DELETE` | `/usuarios/suscripcion` | ✅ | Activa (30 días, **sin cobro**) o cancela Premium: el check de verificado lo ven todos |

### Denuncias y moderación

| Método | Ruta | Auth | Descripción |
| :--- | :--- | :--- | :--- |
| `POST` | `/publicaciones/{id}/denunciar` | ✅ | `{"motivo": "SPAM\|OFENSIVO\|FALSO\|ACOSO\|OTRO", "detalle": "..."}`. No se puede denunciar lo propio ni repetir (409) |
| `POST` | `/publicaciones/{pid}/comentarios/{cid}/denunciar` | ✅ | Igual, para comentarios |
| `GET` | `/admin/estadisticas`, `/admin/usuarios` | 🔒 ADMIN | Panel de administración (totales y lista de usuarios) |
| `GET` | `/admin/denuncias` | 🔒 ADMIN | Contenidos con denuncias pendientes, agrupados |
| `POST` | `/admin/publicaciones/{id}/restaurar`, `/ocultar` | 🔒 ADMIN | Restaurar resuelve sus denuncias |
| `POST` | `/admin/comentarios/{id}/restaurar`, `/ocultar` | 🔒 ADMIN | Ídem para comentarios |

Con **3 denuncias** de personas distintas el contenido se oculta solo
(`ModeracionServiceImpl.UMBRAL_OCULTAR`) hasta que un administrador lo revisa.

### Categorías

| Método | Ruta | Auth | Descripción |
| :--- | :--- | :--- | :--- |
| `GET` | `/categorias` | ❌ Público | Lista |
| `POST` / `PUT` / `DELETE` | `/categorias[/{id}]` | 🔒 ADMIN | Gestión. Al publicar, si la categoría no existe se crea sola (hashtag automático) |

## Seguridad

- `SecurityConfig` tiene `@EnableMethodSecurity`: sin él, los `@PreAuthorize("hasRole('ADMIN')")`
  de categorías no se aplicaban y cualquier usuario podía crearlas o borrarlas.
- `/api/v1/admin/**` exige rol ADMIN desde la propia configuración de rutas.
- Sin sesión válida (sin token, vencido o de una cuenta eliminada) las rutas protegidas responden **401**; las públicas siguen funcionando. Con sesión pero sin permisos, **403**. Un JSON inválido, **400**.
- El bean `passwordEncoder()` necesita `@Bean`: se perdió una vez en un merge y el backend no arrancaba.

## 📱 App móvil

La app Android (Kotlin + Jetpack Compose) vive en su propio proyecto (`App-Movil`). Su README
explica cómo ejecutarla contra este backend, crear cuentas de prueba y apuntarla al servidor
compartido.
