# TuraTrip

Plataforma tecnológica diseñada para registrar y compartir información turística de distintos destinos. Permite a los usuarios publicar recomendaciones, subir fotografías y visualizar la ubicación exacta de cada lugar mediante GPS, facilitando la exploración, planificación y descubrimiento de sitios de interés de manera interactiva y accesible

## 👥 Equipo de Trabajo (Scrum Team)

| Rol               | Nombre                        |
| :---------------- | :---------------------------- |
| **Product Owner** | Cristian Alberto Díaz García  |
| **Scrum Master**  | Sebastian Hurtado             |
| **Developer**     | Javier Andrés Salazar Escobar |

## 🚀 Gestión del Proyecto

Para dar seguimiento a nuestras tareas, historias de usuario y sprints, utilizamos **Jira**:

[Espacio de trabajo en Jira](https://sugerenciasgps.atlassian.net/jira/software/projects/SUG/boards/1/backlog?atlOrigin=eyJpIjoiN2QxNGY0NzU1M2I5NGU1Yzk3ZDVlMTllZjI3ODRiYWEiLCJwIjoiaiJ9&cloudId=7d5b4f9c-d5a7-47d8-8507-f923ba17f958)

## 🛠️ Stack Tecnológico

| Capa | Tecnología |
| :--- | :--- |
| Frontend | Next.js 16 + TypeScript + Tailwind CSS |
| App móvil | Android nativo: Kotlin + Jetpack Compose (proyecto `App-Movil`) |
| Backend | Spring Boot 4 + Java 21 |
| Base de datos | MySQL 9.7 (migraciones con Flyway) |
| Contenedores | Docker + Docker Compose |

## ⚡ Inicio Rápido (desarrollo local)

> Requisito único: tener **Docker Desktop** instalado.

```bash
git clone https://github.com/SHF0987654321/TuraTrip.git
cd TuraTrip
cp .env.example .env
docker compose up -d --build
```

| Servicio    | URL                                   |
| :---------- | :------------------------------------ |
| Frontend    | http://localhost:3000                 |
| Backend API | http://localhost:8080                 |
| Swagger UI  | http://localhost:8080/swagger-ui.html |

Para más detalles de cada servicio:
- [Documentación del Backend](./backend/README.md) — endpoints, migraciones, variables y seguridad
- [Documentación del Frontend](./frontend/README.md)
- App móvil: README del proyecto `App-Movil`

## 🚢 Despliegue en Producción (CI/CD)

El proyecto se despliega automáticamente a un VPS con cada `push` a `main`, mediante GitHub Actions (`.github/workflows/deploy.yml`).

**Flujo del pipeline:**

1. **Build & Push** — compila las imágenes Docker de `backend` y `frontend` y las publica en GitHub Container Registry (`ghcr.io`).
2. **Deploy** — se conecta al VPS por SSH, genera el `.env` de producción a partir de los GitHub Secrets, levanta la base de datos, **sincroniza las credenciales de la app en MySQL** (paso idempotente para que un cambio de secret no rompa el deploy) y finalmente levanta `backend` y `frontend` con `docker compose`.

**Stack en el VPS:**

- `docker-compose.prod.yml` — orquesta `db` (MySQL 9.7), `backend` y `frontend`, cada uno solo expuesto en `127.0.0.1` (no directamente a internet).
- **nginx** (fuera de este repo, configurado directamente en el VPS) — hace de reverse proxy con TLS (Let's Encrypt), enruta el tráfico de `/api/` hacia el contenedor `backend` y el resto (`/`) hacia el contenedor `frontend`.

**Secrets requeridos en GitHub** (`Settings → Secrets and variables → Actions`):

| Secret                                                                                                                  | Descripción                                                                           |
| :---------------------------------------------------------------------------------------------------------------------- | :------------------------------------------------------------------------------------ |
| `SSH_HOST`, `SSH_USER`, `SSH_KEY`                                                                                       | Acceso al VPS                                                                         |
| `DB_ROOT_PASSWORD`, `DB_NAME`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `DB_DRIVER`, `HIBERNATE_DIALECT`, `JPA_DDL_AUTO` | Base de datos                                                                         |
| `JWT_SECRET`, `JWT_EXPIRATION_MS`                                                                                       | Autenticación                                                                         |
| `API_URL`, `FRONTEND_URL`                                                                                               | URLs públicas del despliegue (con y sin `/api`, ver detalle en el README del backend) |
| `SMTP_HOST`, `SMTP_PORT`, `SMTP_USERNAME`, `SMTP_PASSWORD`                                                              | Envío de correos transaccionales                                                      |
| `UPLOADS_DIR`, `MAX_FILE_SIZE`, `MAX_REQUEST_SIZE`                                                                      | Subida de archivos                                                                    |

> ⚠️ Si alguna vez rotas `DB_PASSWORD` o `DB_ROOT_PASSWORD`, no necesitas tocar nada manual en el VPS: el paso de sincronización del pipeline actualiza el usuario de MySQL automáticamente en el siguiente deploy.

> 🔒 La configuración concreta de nginx (dominio, puertos internos, rutas de certificados) vive únicamente en el VPS y no se versiona en este repositorio público, por ser información de infraestructura. Si necesitas replicarla, sigue esta guía general: crea un `server` que redirija `/api/` al contenedor del backend y `/` al del frontend, con TLS vía Let's Encrypt.
>
> ⚠️ **Aviso general (válido para cualquier reverse proxy):** no declares el mismo header de proxy dos veces (por ejemplo, con `include proxy_params;` **y además** `proxy_set_header Host ...` manualmente en el mismo `location`). Servlets containers estrictos con RFC 7230 (como Tomcat 11) rechazan con `400 Bad Request` cualquier petición con headers duplicados, como protección contra request smuggling.

### Correos que llegan a Spam

Si los correos de verificación/recuperación caen en Spam, no es un bug de la app (el log mostrará `📧 Correo enviado con éxito`): es un tema de reputación del remitente. En producción, usa un dominio propio + proveedor transaccional (Resend, Brevo, Amazon SES) con SPF/DKIM/DMARC configurados, en vez de una cuenta Gmail personal como `SMTP_HOST`.

## 🆕 Funciones añadidas para la app móvil

El backend se amplió para la app Android (migraciones `V8` a `V12`, se aplican solas al arrancar):

- **Publicaciones** con ubicación (búsqueda por GPS, mapa, "cerca de mí"), **hasta 10 fotos**,
  edición de datos y fotos, búsqueda en el servidor.
- **Interacción**: "me gusta", guardados (privados), seguidores y feed "Siguiendo". Los
  comentarios usan la implementación del equipo, con el check de verificado del autor.
- **Perfil**: biografía, contadores y **Premium** (perfil verificado, gratis por ahora).
- **Moderación**: denuncias de publicaciones y comentarios; con 3 denuncias se ocultan solas y
  un administrador las revisa.
- **Seguridad**: se activó `@EnableMethodSecurity` (las rutas de administrador de categorías no
  estaban protegidas) y el correo ya no se expone en perfiles ajenos.

> ⚠️ **Antes de desplegar:** sube el secret `MAX_REQUEST_SIZE` a `60MB` (10 fotos por publicación).
