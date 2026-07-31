# Development Guide

This document explains how to develop Sisyphus Academy locally.

## Project Structure

```text
apps/
  api/
  web/
  chrome-extension/
gateway/
docs/
```

---

## API

Start the API server:

```bash
cd apps/api
./gradlew bootRun
```

Human-readable route documentation:

- [docs/API.md](./API.md)
- Swagger UI: `http://localhost:8080/swagger-ui/index.html`
- OpenAPI JSON: `http://localhost:8080/v3/api-docs`

Build the API:

```bash
./gradlew build
```

Run API tests:

```bash
./gradlew test
```

Run the full backend quality gate:

```bash
./gradlew clean check
```

This runs:

- unit, slice, repository, and integration tests
- JaCoCo coverage verification
- Checkstyle on main and test sources
- SpotBugs on main sources

Generated reports live under:

- `apps/api/build/reports/tests/test`
- `apps/api/build/reports/jacoco/test`
- `apps/api/build/reports/checkstyle`
- `apps/api/build/reports/spotbugs`
- `apps/api/build/generated/openapi/openapi.json`

Swagger and OpenAPI are available during local API runs at:

- `http://localhost:8080/swagger-ui/index.html`
- `http://localhost:8080/v3/api-docs`

See [API.md](./API.md) for the route catalog, auth model, and mermaid flow diagrams.

---

## Environment variables

Copy the repository template before local work:

```bash
cp .env.example .env
```

| Variable | Used by | Purpose |
| --- | --- | --- |
| `VITE_API_BASE_URL` | web | API origin for Vite dev/build |
| `VITE_PROXY_TARGET` | web | dev proxy target (defaults to API) |
| `APP_HOST` | api | web app origin for OAuth success redirect |
| `API_HOST` | api | public API origin |
| `APP_EXTENSION_HOST` | api | `chrome-extension://<id>` used to validate extension OAuth |
| `CORS_ALLOWED_ORIGINS` | api | exact allowed browser/extension origins |
| `GOOGLE_CLIENT_ID` / `GOOGLE_CLIENT_SECRET` | api | Google OAuth provider |

Web loads the root `.env` through Vite (`apps/web/vite.config.ts`).

---

## Web

Start the web app:

```bash
cd apps/web

npm install
npm run dev
```

Build the web app:

```bash
npm run build
```

---

## Chrome Extension

Install dependencies:

```bash
cd apps/chrome-extension

bun install
```

Run in development:

```bash
bun run dev
```

Build extension:

```bash
bun run build
```

Type check extension sources:

```bash
bun run compile
```

Run extension tests:

```bash
bun run test
```

### Extension configuration today

Unlike the web app, the extension does **not** yet read the root `.env` at runtime.
These values are currently defined in code:

| Concern | File | Local behavior |
| --- | --- | --- |
| API base URL | `entrypoints/popup/auth/auth.constants.ts` | `http://localhost:8080/api` when not production build |
| App home URL | same file | hardcoded production host for header/footer links |
| OAuth redirect | same file + backend `.env` | `https://<extension-id>.chromiumapp.org` |

For Google OAuth to work locally you must keep these aligned:

1. load the extension built with `bun run dev` or a dev build that points at localhost
2. set `APP_EXTENSION_HOST=chrome-extension://<your-extension-id>` in root `.env`
3. include the same extension origin in `CORS_ALLOWED_ORIGINS`
4. register Google redirect URI `http://localhost:8080/login/oauth2/code/google`

See [OAUTH_SETUP.md](./OAUTH_SETUP.md) and [API.md](./API.md#chrome-extension-appschrome-extension).

---

## Local Stack

Start the full stack with Docker Compose:

```bash
docker compose up -d
```

---

## Coding Guidelines

- Prefer small focused functions.
- Avoid duplicated logic.
- Use meaningful names.
- Keep business logic separated from infrastructure concerns.
- Add tests when introducing new behavior.

---

## Commit Convention

Examples:

```text
feat: add vocabulary search
fix: resolve login issue
docs: update deployment guide
refactor: simplify oauth flow
test: add email verification tests
```
