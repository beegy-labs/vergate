# Tasks: MVP Gateway

> Implementation tasks for `scopes/2026-q1-mvp-gateway.md` | **Last Updated**: 2026-02-22

## Task Status

| #  | Task                                            | Status | Blocked By |
| -- | ----------------------------------------------- | ------ | ---------- |
| 1  | Project scaffolding                             | Done   | —          |
| 2  | CDD + SDD setup                                | Done   | —          |
| 3  | Flyway migration V1                             | Done   | 1          |
| 4  | Domain models (all entities)                    | Done   | 3          |
| 5  | Outbound ports (Repository + Cache)             | Done   | 4          |
| 6  | JPA entities + adapters                         | Done   | 5          |
| 7  | Valkey cache adapter                            | Done   | 5          |
| 8  | Domain services (all 6)                         | Done   | 5          |
| 9  | DTOs (client + admin)                           | Done   | 4          |
| 10 | GatewayController (GET /init)                  | Done   | 8, 9       |
| 11 | Admin controllers (all 5)                       | Done   | 8, 9       |
| 12 | Spring config (Security, Redis, Swagger, CORS)  | Done   | 1          |
| 13 | Unit tests (all services)                       | Done   | 8          |
| 14 | Slice tests (GatewayController)                 | Done   | 10         |
| 15 | Integration tests (JPA + Valkey)                | Done   | 6, 7       |
| 16 | E2E tests (admin CRUD + critical scenarios)     | Done   | 15         |
| 17 | JaCoCo coverage gate (80%)                      | Done   | 15, 16     |
| 18 | SDD scope update (clarify Phase 0 goals)        | Done   | —          |
| 19 | Legal Document feature                          | Done   | 3, 8, 9    |
| 20 | UUIDv7 primary keys + Base62 appKey             | Done   | 3, 6       |
| 21 | Helm chart + ESO deployment                     | Done   | 1          |

## Task Details

### T1: Project Scaffolding ✅

- `build.gradle.kts` — Spring Boot 3.4.3, Kotlin 2.1.10, JDK 21
- `settings.gradle.kts`, Gradle wrapper 8.12.1
- `docker-compose.yml` — PostgreSQL 17 + Valkey 8
- `Dockerfile` — multi-stage build (eclipse-temurin:21)
- `application.yml` / `application-local.yml` / `application-prod.yml`

### T2: CDD + SDD Setup ✅

- `agentic-dev-protocol` submodule + policy symlinks
- `.ai/` Tier 1 files (rules, architecture, git-flow, testing, best-practices)
- `docs/llm/` Tier 2 policies (hexagonal-architecture, testing, git-flow)
- `.specs/` SDD structure (roadmap, scopes, tasks)

### T3: Flyway Migration V1 ✅

- `V1__init_schema.sql` — apps, app_versions, maintenances, notices, remote_configs
- TIMESTAMPTZ for all timestamps, BIGSERIAL for IDs
- Foreign keys with ON DELETE CASCADE
- Indexes on `app_id` columns

### T4-T7: Domain + Ports + Adapters ✅

- Domain models: pure Kotlin data classes, no Spring/JPA annotations
- Ports: interfaces only (`AppRepositoryPort`, `GatewayCachePort`, etc.)
- JPA adapters: implement ports, use mappers for entity↔domain conversion
- Valkey adapter: try/catch for graceful fallback, pattern-based eviction

### T8: Domain Services ✅

- `GatewayService`: aggregates all data, Valkey cache read-through
- `AppService`: CRUD + cache eviction on mutation
- `VersionService`: CRUD + cache eviction
- `MaintenanceService`: CRUD + cache eviction

### T10-T11: Controllers ✅

- `GatewayController`: `GET /api/v1/init` with query params
- Admin controllers: full CRUD with `@Valid` input validation

### T15: Integration Tests ✅

- `JpaAdapterIntegrationTest` — Maintenance time-range query, RemoteConfig unique constraint, cascade delete
- `ValkeyCacheIntegrationTest` — TTL expiry, pattern-based eviction
- Tool: Testcontainers (PostgreSQL + Valkey) — containers managed manually (no `@Testcontainers` to prevent Spring context cache invalidation)

### T16: E2E Tests ✅

| # | Scenario                  | File                     |
| - | ------------------------- | ------------------------ |
| 1 | Happy path                | `GatewayE2ETest`         |
| 2 | Service termination       | `GatewayE2ETest`         |
| 3 | Force update              | `GatewayE2ETest`         |
| 4 | App CRUD (list/get/update/delete) | `AdminCrudE2ETest` |
| 5 | Version CRUD              | `AdminCrudE2ETest`       |
| 6 | Maintenance CRUD          | `AdminCrudE2ETest`       |
| 7 | Notice CRUD               | `AdminCrudE2ETest`       |
| 8 | Config CRUD               | `AdminCrudE2ETest`       |
| 9 | Init with active maintenance + notice + config | `AdminCrudE2ETest` |

### T17: JaCoCo Coverage Gate ✅

- JaCoCo plugin in `build.gradle.kts`, minimum 80%
- **Achieved: 90%** (3 contexts: unit, integration, E2E)
- Command: `./gradlew test jacocoTestReport jacocoTestCoverageVerification`

### T18: SDD Scope Update ✅

- Clarified Phase 0 goals: version check, privacy policy URL, service shutdown
- Removed Maintenance from Phase 0 primary goals (still implemented as supporting infrastructure)
- Updated test results in scope document

### T19: Legal Document Feature ✅

- `V2__add_legal_documents.sql` — `legal_documents` table (doc_type, content, content_type, UNIQUE(app_id, doc_type))
- Domain model: `LegalDocument`, `LegalDocType` (PRIVACY_POLICY, TERMS_OF_SERVICE), `LegalContentType` (MARKDOWN, HTML)
- Port: `LegalDocumentRepositoryPort`
- JPA: `LegalDocumentJpaEntity`, `LegalDocumentJpaRepository`, `LegalDocumentMapper`, `LegalDocumentJpaAdapter`
- Service: `LegalDocumentService` (CRUD + markdown→HTML rendering via `commonmark`)
- Controllers: `LegalDocAdminController` (CRUD), `LegalDocumentController` (public HTML page)
- `GatewayService`: enriches `init` response `legal[]` with document URLs
- Tests: 8 unit + 4 E2E (`LegalDocE2ETest`)

### T20: UUIDv7 Primary Keys + Base62 appKey ✅

- `V3__use_uuid_primary_keys.sql` — drops and recreates all 6 tables with `UUID PRIMARY KEY`
- `AppKeyGenerator` (domain/util): `generateUuidV7()` (RFC 9562 48-bit timestamp), `generate()` → Pair(UUID, Base62 22-char appKey)
- All JPA entities: `id: UUID`, `appId: UUID`, `@Column(columnDefinition = "uuid")`, no `@GeneratedValue`
- All JPA repositories: `JpaRepository<Entity, UUID>`
- All JPA mappers: `entity.id.toString()` ↔ `UUID.fromString(domain.id)`
- All JPA adapters: `id.isEmpty()` check for new entity detection, `orElse(null) ?: toEntity()` for safe upsert
- `AppService.create()`: auto-generates UUID + appKey — `appKey` removed from `CreateAppRequest`
- Sub-entity services: pre-assign UUIDv7 before `save()`
- All tests updated: UUID string constants, ID parsing as String

### T21: Helm Chart + ESO Deployment ✅

- `helm/vergate/` chart with conditional PostgreSQL and Valkey StatefulSets
- `postgresql.enabled: false`, `valkey.enabled: false` by default (use external)
- `ExternalSecret` creates `vergate-secret` from ClusterSecretStore
- `Dockerfile.local` for docker-compose local build (copies pre-built JAR)
- `docker-compose.yml` updated: app service with healthcheck-based `depends_on`
- CDD updated: `.ai/deployment.md`, `docs/llm/policies/deployment.md`
