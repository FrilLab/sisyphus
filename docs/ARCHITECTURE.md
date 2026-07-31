# Sisyphus Academy Architecture

Sisyphus Academy is a self-hosted knowledge capture platform built with Spring Boot, React, a Chrome extension, PostgreSQL, Redis, and Docker.

The project separates reusable platform infrastructure from academy-specific business logic.

For the endpoint catalog, auth flows, and client integration notes, see [API Reference](./API.md).

---

# High Level Architecture

```mermaid
flowchart TB
  subgraph Clients
    WEB[Web App\napps/web]
    EXT[Chrome Extension\napps/chrome-extension]
  end

  subgraph Edge
    NGINX[Nginx Gateway\noptional Docker stack]
  end

  subgraph Backend["Spring Boot API (apps/api)"]
    SEC[Security + JWT/OAuth]
    FEAT[Feature modules]
  end

  subgraph Storage
    PG[(PostgreSQL)]
    REDIS[(Redis)]
    FILES[(Upload storage)]
  end

  WEB --> NGINX
  EXT --> NGINX
  NGINX --> SEC --> FEAT
  FEAT --> PG
  FEAT --> REDIS
  FEAT --> FILES
```

---

# API Architecture

The API application lives in `apps/api`. It uses feature packages rather than a
second, parallel layer tree. Each feature may contain:

- `controller`: HTTP validation, authentication principal extraction, and response mapping
- `service`: use-case orchestration, business rules, and transaction boundaries
- `repository`: persistence queries
- `entity`: JPA persistence and domain state
- `dto`: explicit request and response contracts
- `exception`: expected domain failures

Cross-cutting framework configuration and shared response types live in `global`;
JWT filters and Spring Security adapters live in `security`; external storage,
mail, Redis, and OAuth adapters remain in their owning feature packages.

## Request flow

```mermaid
flowchart LR
  REQ[HTTP Request] --> FIL[JwtAuthenticationFilter]
  FIL --> CTL[Controller]
  CTL --> SVC[Service]
  SVC --> REPO[Repository]
  REPO --> ENT[Entity]
  ENT --> SVC
  SVC --> DTO[Response DTO]
  DTO --> RES[JSON Response]
```

Controllers must not access repositories, make business decisions, or return
entities. Request and response DTOs remain separate when their roles differ.
Entity-to-response conversion is completed inside the service transaction so
lazy persistence state does not cross the application boundary.

## Transactions

Write use cases declare `@Transactional` on service methods. Query use cases that
need a persistence context declare `@Transactional(readOnly = true)`. Controllers
and repositories do not define application transaction boundaries. A propagation
override such as `REQUIRES_NEW` must remain visible and be justified by the use case.

## Error handling

Expected failures extend `BaseException` and are translated once by
`GlobalExceptionHandler`. Authentication entry points use the same JSON shape.
Validation failures include every rejected field; internal exceptions and stack
traces are logged server-side and never returned.

```json
{
  "status": 400,
  "code": "VALIDATION_ERROR",
  "message": "email: 올바른 이메일 형식이어야 합니다.",
  "path": "/api/auth/check",
  "timestamp": "2026-07-26T00:00:00Z",
  "fieldErrors": [
    {
      "field": "email",
      "message": "올바른 이메일 형식이어야 합니다."
    }
  ]
}
```

The `status` field is retained for backward compatibility. Domain failures use
stable application codes with empty `fieldErrors`; validation failures use
`VALIDATION_ERROR`. Unexpected failures return `INTERNAL_SERVER_ERROR` without
implementation details. Ownership failures that previously surfaced as 500 now
consistently return 403, and missing resources return 404.

## Configuration and logging

Environment-dependent values are bound through `AppProps`, `JwtProps`,
`FileProps`, and `MailProps`. Security owns CORS configuration, while MVC configuration owns static
resources. Refresh-token persistence derives its TTL from `JwtProps`; callers do
not duplicate expiration constants. Application code uses SLF4J and must not log
tokens, passwords, cookies, or complete sensitive payloads.

## Testing boundaries

- unit tests cover service rules and token utilities
- MVC slice tests lock transport and error contracts
- integration tests verify application wiring and persistence

Issue-specific changes add focused regression tests. The broader layered test
matrix is maintained separately so architecture refactoring does not create a
parallel test implementation.

The backend is also divided into two conceptual product layers.

## Platform Layer

Reusable infrastructure that can be adopted by other projects.

### Authentication

- JWT authentication
- Access token management
- Refresh token management

### OAuth

- Google Login
- Naver Login
- Kakao Login

### Email Verification

- Verification email generation
- Verification code validation
- Redis-based expiration management

### File Management

- File upload
- Image serving
- Storage abstraction

### API Documentation

- OpenAPI
- Swagger UI

### Infrastructure

- PostgreSQL
- Redis
- Flyway
- Docker

---

## Domain Layer

Business logic specific to Sisyphus Academy.

### User

Responsible for:

- Account management
- Profile management
- User settings

### Vocabulary

Responsible for:

- Vocabulary registration
- Vocabulary management
- Search and filtering

### Wordbook

Responsible for:

- Wordbook creation
- Wordbook management
- Vocabulary grouping

### Learning

Responsible for:

- Learning sessions
- Progress tracking
- Review workflows

### Statistics

Responsible for:

- Learning history
- Progress analysis
- User achievements

---

# Web Architecture

The web application lives in `apps/web` and follows a feature-oriented structure.

```mermaid
flowchart TB
  APP[app/\nrouter, providers]
  FEAT[features/\nbusiness UI + hooks]
  UI[components/\nshared UI]
  STYLES[styles/\ntokens + index.css]

  APP --> FEAT
  FEAT --> UI
  FEAT --> STYLES
```

Guidelines:

- Business logic belongs to features.
- Routing and application bootstrapping belong to `app/`.
- Shared UI components belong to `components/`.
- Feature modules should avoid direct coupling.

UI contracts: [ui-foundation.md](./design/ui-foundation.md),
[component-contracts.md](./design/component-contracts.md),
[ux-language.md](./product/ux-language.md).

---

# Chrome Extension Architecture

The browser extension lives in `apps/chrome-extension` and uses WXT entrypoints.

```mermaid
flowchart TB
  BG[background.ts\nselection + popup launch]
  POP[popup/\nauth + quick note form]
  PUB[public/\nlocales + assets]

  BG --> POP
  POP --> API[(Backend /api)]
```

Popup UI aligns with web semantic tokens but uses plain CSS instead of shadcn
components. See [ui-foundation.md](./design/ui-foundation.md#chrome-extension-alignment).

---

# Infrastructure

## Containers

- web
- api
- postgres
- redis
- nginx

Managed using Docker Compose.

---

# Design Principles

## Separation of Concerns

Platform functionality must remain independent from academy-specific business logic.

## Environment Driven Configuration

No production URL, secret, or environment-specific configuration should be committed.

All runtime configuration must come from environment variables.

## Open Source Friendly

The project should be executable locally with:

```bash
docker compose up -d
```

after copying:

```bash
.env.example
→ .env
```

without requiring code modifications.

---

# Future Direction

The long-term goal is to keep Sisyphus Academy as a concrete implementation while extracting reusable platform components where appropriate.

Potential extraction candidates:

- Authentication module
- OAuth integration module
- Email verification module
- File management module
- Docker deployment templates
- CI/CD templates
