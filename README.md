# Sisyphus Academy

[![Backend CI](https://github.com/FrilLab/sisyphus-academy/actions/workflows/backend.yml/badge.svg?branch=main)](https://github.com/FrilLab/sisyphus-academy/actions/workflows/backend.yml)
[![Frontend CI](https://github.com/FrilLab/sisyphus-academy/actions/workflows/frontend.yml/badge.svg?branch=main)](https://github.com/FrilLab/sisyphus-academy/actions/workflows/frontend.yml)
[![Security CI](https://github.com/FrilLab/sisyphus-academy/actions/workflows/security.yml/badge.svg?branch=main)](https://github.com/FrilLab/sisyphus-academy/actions/workflows/security.yml)
[![Codecov](https://codecov.io/gh/FrilLab/sisyphus-academy/branch/main/graph/badge.svg)](https://codecov.io/gh/FrilLab/sisyphus-academy)

## Overview

Sisyphus Academy is a multi-platform knowledge input system designed to provide ubiquitous access to personal knowledge capture through web, mobile, and browser extensions.

The current **0.0.1 version focuses on a Word Expansion System**, where raw user input is transformed into structured, enriched knowledge entries.

Instead of functioning as a traditional vocabulary application, the system is designed as an **automated data enrichment pipeline for words**.

---

## Core Concept (v0.0.1)

When a user submits a word, the system automatically:

* Stores the word
* Generates definitions
* Resolves or generates related images
* Builds structured metadata
* Persists enriched data into the database

This transforms simple user input into a structured knowledge entity.

---

## System Architecture: Word Expansion Pipeline

### High-Level Flow

```text
User Input
   ↓
Word Intake API
   ↓
Normalization Layer
   ↓
Expansion Engine
   ├── Definition Generator
   ├── Image Resolver
   ├── Metadata Builder
   ↓
Persistence Layer (Database)
   ↓
Retrieval API
   ↓
Clients (Web / App / Extension)
```

---

## 1. Word Intake Layer

### Responsibility

Handles raw input from clients and ensures consistent ingestion into the system.

### Responsibilities

* Accept word input from multiple platforms
* Perform basic normalization (trim, lowercase, validation)
* Prevent duplicate entries per user scope

---

## 2. Normalization Layer

### Responsibility

Transforms raw input into a standardized internal representation.

### Responsibilities

* Normalize text format
* Prepare word key structure
* Ensure consistency for downstream processing

---

## 3. Expansion Engine (Core System)

This is the central component of the system.

### 3.1 Definition Generator

Generates semantic definitions for input words.

* Can be powered by dictionary APIs or LLM-based services
* Designed to support multiple definitions per word in future versions

Output:

```text
word → list of definitions
```

---

### 3.2 Image Resolver

Associates or generates visual representations for words.

* External image search APIs or AI-based generation (future)
* Provides contextual visual enrichment

Output:

```text
word → image_url
```

---

### 3.3 Metadata Builder

Constructs a structured knowledge entity from enriched data.

Includes:

* word
* definitions
* image reference
* timestamps
* tags (future extension)

---

## 4. Persistence Layer

### Data Model

Each word is stored as a structured entity:

```text
Word Entity:
- id
- user_id
- word
- definitions
- image_url
- tags
- created_at
```

### Characteristics

* User-isolated data model
* Schema designed for future knowledge graph expansion
* Optimized for retrieval and enrichment workflows

---

## 5. Retrieval Layer

### Responsibilities

Provides access to enriched word data.

* Word list retrieval
* Single word lookup
* Filtering support (future)
* Learning-oriented query extensions (planned)

---

## Multi-Platform Architecture

The system is designed to support multiple input sources:

* Web application
* Mobile application (planned)
* Browser extension (primary input interface)

All platforms share a unified backend processing pipeline.

---

## Current Status (v0.0.1)

### Completed

* Word-centric data model design
* Expansion pipeline architecture definition
* Backend system structure setup

### In Progress

* Expansion Engine implementation
* External API integration (definition/image)
* Persistence optimization

---

## Roadmap

### v0.0.1 — Word Expansion System (Current)

* Word ingestion
* Automated definition generation
* Image resolution
* Structured storage

---

### v0.2 — Learning Enhancement Layer

* Example sentence generation
* Multi-definition support
* Spaced repetition system
* Difficulty estimation

---

### v0.3 — Personalization Layer

* Personalized word recommendations
* Learning pattern analysis
* Adaptive review scheduling

---

### v1.0 — Knowledge System

* Expansion from words to concepts
* Connection between words and notes
* Knowledge graph structure
* Cross-domain semantic linking

---

## Design Philosophy

The system is built on the principle that:

> Input should be immediately transformed into structured and enriched knowledge.

The goal is not simple storage, but automatic transformation of raw input into usable knowledge structures.

---

## Technology Stack

* Java 21
* Spring Boot 3.4+
* Spring Security (OAuth2 + JWT)
* PostgreSQL
* Redis
* Gradle
* H2 (testing)

---

## Key Characteristics

* Multi-platform input support
* Automated data enrichment pipeline
* Extensible knowledge schema
* Separation of concerns across pipeline stages
* Designed for future AI integration

---

## Project Definition

Sisyphus Academy v0.0.1 is defined as:

> A system that transforms raw word input into structured, enriched knowledge entities through an automated expansion pipeline.

---

## Documentation

| Topic | Document |
| --- | --- |
| API routes, auth, pagination, errors | [docs/API.md](docs/API.md) |
| System architecture | [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) |
| Local development | [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md) |
| OAuth setup (web + extension) | [docs/OAUTH_SETUP.md](docs/OAUTH_SETUP.md) |
| Deployment | [docs/DEPLOYMENT.md](docs/DEPLOYMENT.md) |
| Security | [docs/SECURITY.md](docs/SECURITY.md) |
| UI foundation and design tokens | [docs/design/ui-foundation.md](docs/design/ui-foundation.md) |
| Component and state contracts | [docs/design/component-contracts.md](docs/design/component-contracts.md) |
| Product UX and terminology | [docs/product/ux-language.md](docs/product/ux-language.md) |

Web and Chrome extension UI work follows the design and UX documents above.
They extend the existing Tailwind and shadcn architecture and are the visual
and interaction baseline for page-specific work.

---

## Execution

```bash
cd apps/api
./gradlew clean build
./gradlew test
./gradlew bootRun
```

---

## Backend test strategy

The API suite is split by responsibility:

* unit tests protect domain normalization and service rules without a Spring context
* MVC slice tests verify routes, serialization, validation, status codes, and the shared error contract while mocking only services
* JPA tests verify repository queries, ownership filters, relationships, and database constraints using the existing deterministic H2 test profile
* integration tests exercise representative request-to-database lifecycles without live network services

Run the complete verification locally:

```bash
cd apps/api
./gradlew clean check
```

JaCoCo HTML and XML reports are generated under `apps/api/build/reports/jacoco/test/`.
The initial whole-backend baseline is enforced at 30% line coverage and 15% branch coverage. These conservative thresholds reflect the first measured suite (32.6% line and 17.9% branch coverage) and must only move upward as behavior is covered; tests should not be added solely to increase percentages.

CI runs the same `clean check` task for API changes and publishes the JUnit, JaCoCo, Checkstyle, SpotBugs, and generated OpenAPI reports as the `api-quality-reports` artifact.

---

## OpenAPI and Swagger UI

Start the API locally and open:

* Swagger UI: `http://localhost:8080/swagger-ui/index.html`
* OpenAPI JSON: `http://localhost:8080/v3/api-docs`
* Human-readable catalog: [docs/API.md](docs/API.md)

Documentation is generated from Spring MVC routes, request/response DTOs, validation annotations, and OpenAPI annotations. Protected operations use the `bearerAuth` security scheme; enter an access token through Swagger UI's **Authorize** action. No token or credential is preconfigured.

The documentation endpoints are enabled by default outside production and can be controlled with:

```text
SPRINGDOC_ENABLED=true|false
SWAGGER_UI_ENABLED=true|false
```

The `prod` profile explicitly disables both endpoints in `application-prod.properties`.

When changing an API:

* update `@Operation`, response annotations, and DTO `@Schema` metadata with the implementation
* reuse the generated `ErrorResponse` schema and shared error responses
* keep authenticated and public operations' security requirements accurate
* use realistic examples without credentials, tokens, private hosts, or personal data

`./gradlew clean check` verifies critical paths, schemas, security metadata, and Swagger UI startup. It also exports the checked specification to `apps/api/build/generated/openapi/openapi.json`; API CI publishes that file with the test reports.

---

## License

Apache License 2.0
