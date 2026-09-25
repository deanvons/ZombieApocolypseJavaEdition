# Deploying to Render

The API runs on Render as a Docker image pulled from GitHub Container Registry (GHCR). Every push to `main` deploys automatically (see [ci-cd-pipeline.md](ci-cd-pipeline.md)).

```
GitHub Actions ──push image──► GHCR ──pull image──► Render
       │                                              ▲
       └──────────── deploy hook (POST) ──────────────┘
```

## One-time setup

### 1. Make the image pullable
The image is `ghcr.io/deanvons/zombieapocolypsejavaedition`. GHCR names are always lowercase.

- **Public package (simplest):** GitHub → Packages → the package → **Package settings → Change visibility → Public**.
- **Private package:** in Render, add a **Container Registry Credential** with a GitHub token that has the `read:packages` scope.

### 2. Create the Render service
1. **New → Web Service → Existing Image**.
2. **Image URL:** `ghcr.io/deanvons/zombieapocolypsejavaedition:latest`.
3. **Region:** Frankfurt.
4. **Environment variable:** `PORT` = `8080`, the port Spring Boot listens on. Add the database variables too (see [Database](#database)).
5. **Health check path:** `/api/health/database`.
6. Create it. The first deploy pulls `:latest`.

Check that it works: `https://<service>.onrender.com/api/health/database` should return `200 Database connected`.

### 3. Connect GitHub Actions to Render
1. Render → the service → **Settings → Deploy Hook** → copy the URL. **Keep it secret**: anyone with it can trigger a deploy.
2. GitHub → repo → **Settings → Secrets and variables → Actions → New repository secret**:
   - Name: `RENDER_DEPLOY_HOOK_URL`
   - Value: the hook URL

That's it. The `deploy` job in `cd.yml` calls the hook after every successful image push.

## Database

Each developer runs their own Postgres in Docker. Production uses a Postgres database on Render. The schema is generated from the `@Entity` classes (`ddl-auto=update`), so there are no migration scripts.

### Local
```
docker compose up -d                                   # start Postgres on localhost:5433
docker exec -it zombie-db psql -U zombie -d zombies    # open a SQL prompt (\dt = tables, \q = quit)
docker compose down -v                                 # wipe it and start fresh
```
No configuration needed: `application.properties` defaults to this database.

### Production (Render)
1. **New → Postgres**, in the **same region** as the API.
2. On the API service, set these environment variables, built from the **Internal Database URL** (`postgresql://USER:PASSWORD@HOST/DBNAME`):

| Key | Value |
|---|---|
| `DB_URL` | `jdbc:postgresql://HOST:5432/DBNAME` |
| `DB_USERNAME` | `USER` |
| `DB_PASSWORD` | `PASSWORD` |

When these are set, they override the local defaults. That's the only difference between local and prod.

> `ddl-auto=update` only **adds** tables and columns. Renaming or removing a field in an entity needs manual SQL on the prod database.

**Browsing the prod database:** use the **External Database URL** (hostname ends in `.render.com`) in pgAdmin, with SSL mode `require`. This is live data, so be careful.

## Keycloak

Keycloak runs as a second Render web service. It uses the **same Postgres server** as the API, but its own `keycloak` database.

1. Create the database, using the External Database URL:
   ```
   docker exec -it zombie-db psql "<external-url>" -c "CREATE DATABASE keycloak;"
   ```
2. **New → Web Service → Existing Image:** `quay.io/keycloak/keycloak:26.2`
   - **Instance:** at least **2 GB** of memory. 512 MB runs out of memory.
   - **Docker command:** `/opt/keycloak/bin/kc.sh start`, with the full path. Just `start` exits with status 128.
3. Environment variables:

| Key | Value |
|---|---|
| `KC_DB` | `postgres` |
| `KC_DB_URL` | `jdbc:postgresql://HOST:5432/keycloak` |
| `KC_DB_USERNAME` / `KC_DB_PASSWORD` | same as the API's |
| `KC_HOSTNAME` | `https://<keycloak-service>.onrender.com`. Copy it from the service page after creating it. |
| `KC_HTTP_ENABLED` | `true` (Render handles HTTPS) |
| `KC_PROXY_HEADERS` | `xforwarded` |
| `KC_BOOTSTRAP_ADMIN_USERNAME` / `KC_BOOTSTRAP_ADMIN_PASSWORD` | first admin login. Replace it with a proper admin afterwards. |
| `PORT` | `8080` |

### Registering the frontend (React SPA)
Admin console → your realm → **Clients → Create client**:
- **Client authentication:** OFF (public client, because a browser app can't keep a secret)
- **Standard flow:** ON. **Direct access grants:** OFF. **PKCE:** `S256`
- **Root URL:** `https://<frontend>.onrender.com`
- **Valid redirect URIs:** `https://<frontend>.onrender.com/*`
- **Valid post logout redirect URIs:** `+`
- **Web origins:** `+`

## How a deploy works

The deploy job calls the hook with the image for that exact commit:

```
POST <deploy hook URL>&imgURL=ghcr.io/deanvons/zombieapocolypsejavaedition:sha-<commit>
```

So you always know which commit is live.

**Rolling back:** in Render, go to **Manual Deploy**, deploy an older `:sha-<commit>` image, or redeploy an earlier deploy from the **Events** list.

## Troubleshooting

| Symptom | Likely cause |
|---|---|
| Deploy job fails with `401`/`404` | Wrong or missing `RENDER_DEPLOY_HOOK_URL` secret |
| Render can't pull the image | The package is private and there's no registry credential in Render |
| Deploy "succeeds" but the service keeps restarting | The health check fails: check `PORT` = `8080` and the `/api/health/database` path |
| Old version still live | The deploy job didn't run. Check the Actions tab: an earlier job (tests) probably failed |
| API won't start: connection refused to `localhost:5433` | `DB_URL` / `DB_USERNAME` / `DB_PASSWORD` aren't set on Render |
| `/api/health/database` returns `503` | Wrong DB credentials, or the database is in a different region from the API |
| pgAdmin: `getaddrinfo failed` | You used the internal hostname. Use the external one (`...render.com`). |
| Keycloak: exit status 128 | Docker command isn't the full path `/opt/keycloak/bin/kc.sh start` |
| Keycloak: out of memory | Instance too small. Use 2 GB. |
| Keycloak: `database "keycloak" does not exist` | Run the `CREATE DATABASE` step first |
