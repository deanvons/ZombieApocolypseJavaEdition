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
4. **Environment variable:** `PORT` = `8080`, the port Spring Boot listens on.
5. **Health check path:** `/api/health`.
6. Create it. The first deploy pulls `:latest`.

Check that it works: `https://<service>.onrender.com/api/health` should return `200`.

### 3. Connect GitHub Actions to Render
1. Render → the service → **Settings → Deploy Hook** → copy the URL. **Keep it secret**: anyone with it can trigger a deploy.
2. GitHub → repo → **Settings → Secrets and variables → Actions → New repository secret**:
   - Name: `RENDER_DEPLOY_HOOK_URL`
   - Value: the hook URL

That's it. The `deploy` job in `cd.yml` calls the hook after every successful image push.

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
| Deploy "succeeds" but the service keeps restarting | The health check fails: check `PORT` = `8080` and the `/api/health` path |
| Old version still live | The deploy job didn't run. Check the Actions tab: an earlier job (tests) probably failed |
