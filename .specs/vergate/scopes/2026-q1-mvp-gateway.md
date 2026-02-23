# Scope: MVP Gateway

> Minimum viable gateway for web/app deployment | **Status**: Done | **Last Updated**: 2026-02-22

## Goal

Build the minimum API set required for a production deployment:
1. **Version Check + Force Update** — Force update users on outdated versions
2. **Legal Document Hosting** — Host privacy policy and terms of service as HTML pages; serve URLs via `init` response
3. **Service Shutdown** — Permanently block access to terminated services

All three are delivered through a single `GET /api/v1/init` endpoint that client apps call on every launch.

## Architecture

**Hexagonal Architecture** (Ports & Adapters)

```
Adapters (outer) --> Ports (boundary) --> Domain (inner)
```

| Layer    | Role                     | Directory                                    |
| -------- | ------------------------ | -------------------------------------------- |
| Domain   | Business logic (pure Kotlin) | `domain/model/`, `domain/service/`       |
| Ports    | Output interfaces        | `ports/out/`                                 |
| Adapters | Infrastructure           | `adapters/in/rest/`, `adapters/out/persistence/`, `adapters/out/cache/` |
| Config   | Spring wiring            | `config/`                                    |

**Policy**: `docs/llm/policies/hexagonal-architecture.md`

## Features

### F1: App Registration (Admin)

| Item        | Detail                                                  |
| ----------- | ------------------------------------------------------- |
| Purpose     | Register apps that the gateway manages                  |
| Admin API   | `POST/GET/PUT/DELETE /api/v1/admin/apps`                |
| Key Fields  | `appKey` (unique), `name`, `platform`, `storeUrl`, `isActive` |
| Constraint  | `appKey` must be unique (409 on duplicate)              |
| Cache       | Evict `vergate:init:{appKey}:*` on any mutation         |

### F2: Version Check (Client + Admin)

| Item        | Detail                                                  |
| ----------- | ------------------------------------------------------- |
| Purpose     | Tell client whether an update is available              |
| Client API  | `GET /api/v1/init` → `update` block in response        |
| Admin API   | `POST/GET/PUT /api/v1/admin/apps/{appId}/versions`      |
| Key Fields  | `minVersion`, `latestVersion`, `forceUpdate`, `updateMessage` |
| Domain Rule | Compare client `appVersion` against `minVersion` using semver |

**Version Comparison Logic (Domain Service)**:

```
if clientVersion < minVersion → force = true   (MUST update)
if clientVersion >= minVersion → force = forceUpdate flag value
```

- Semver comparison: split by `.`, compare each segment numerically
- Lives in `GatewayService` as pure Kotlin function (no framework dependency)

**Client Response — `update` block**:

```json
{
  "force": true,
  "latest_version": "2.5.0",
  "min_version": "2.0.0",
  "store_url": "https://play.google.com/...",
  "message": "Critical security update"
}
```

### F3: Maintenance Mode (Client + Admin)

| Item        | Detail                                                  |
| ----------- | ------------------------------------------------------- |
| Purpose     | Block or warn users during scheduled maintenance        |
| Client API  | `GET /api/v1/init` → `maintenance` block in response   |
| Admin API   | `POST/GET/PUT/DELETE /api/v1/admin/apps/{appId}/maintenances` |
| Key Fields  | `title`, `message`, `startAt`, `endAt`, `isActive`     |
| Domain Rule | Active if `isActive=true AND startAt <= now AND endAt >= now` |

**Time-Range Query (JPA Adapter)**:

```sql
SELECT m FROM MaintenanceJpaEntity m
WHERE m.appId = :appId AND m.isActive = true
  AND m.startAt <= :now AND m.endAt >= :now
ORDER BY m.startAt DESC
```

**Client Response — `maintenance` block**:

```json
{
  "active": true,
  "title": "Server Maintenance",
  "message": "We are updating our servers. Please try again later.",
  "start_at": "2026-02-22T02:00:00+09:00",
  "end_at": "2026-02-22T06:00:00+09:00"
}
```

### F4: Service Shutdown (Client + Admin)

| Item        | Detail                                                  |
| ----------- | ------------------------------------------------------- |
| Purpose     | Permanently block access when an app is terminated      |
| Client API  | `GET /api/v1/init` → `service` block in response       |
| Admin API   | `PUT /api/v1/admin/apps/{id}` with `isActive=false`    |
| Domain Rule | If `app.isActive == false` → return terminated response, skip all other logic |

**Why `isActive` flag instead of deletion**:
- Reversible — can reactivate without data loss
- Audit trail — the app record remains with full history
- Client gets a meaningful response instead of 404

**Client Response — terminated app**:

```json
{
  "service": { "active": false, "message": "This service has been terminated." },
  "update": null,
  "maintenance": null,
  "notices": [],
  "config": {}
}
```

### F5: Legal Document Hosting (Client + Admin)

| Item        | Detail                                                  |
| ----------- | ------------------------------------------------------- |
| Purpose     | Host privacy policy and terms of service as HTML pages  |
| Client API  | `GET /api/v1/legal/{appKey}/{type}` → HTML page         |
| Init API    | `GET /api/v1/init` → `legal` array with type + title + URL |
| Admin API   | `POST/GET/PUT/DELETE /api/v1/admin/apps/{appId}/legal-docs` |
| Key Fields  | `docType` (PRIVACY_POLICY, TERMS_OF_SERVICE), `title`, `content`, `contentType` (MARKDOWN, HTML) |
| Domain Rule | MARKDOWN content is rendered to HTML on request; HTML is served as-is |

**URL Patterns**:

```
GET /api/v1/legal/{appKey}/privacy-policy   → PRIVACY_POLICY document
GET /api/v1/legal/{appKey}/terms            → TERMS_OF_SERVICE document
```

**Init Response — `legal` array**:

```json
{
  "legal": [
    { "type": "PRIVACY_POLICY", "title": "Privacy Policy", "url": "https://api.example.com/api/v1/legal/{appKey}/privacy-policy" },
    { "type": "TERMS_OF_SERVICE", "title": "Terms of Service", "url": "https://api.example.com/api/v1/legal/{appKey}/terms" }
  ]
}
```

**Why dedicated entity instead of RemoteConfig**:
- Content needs markdown → HTML rendering
- One unique doc per app per type (DB unique constraint)
- `isActive` flag for publish/unpublish control

### F6: Valkey Cache Layer

| Item        | Detail                                                  |
| ----------- | ------------------------------------------------------- |
| Purpose     | Handle high traffic on `/api/v1/init` without DB pressure |
| Cache Key   | `vergate:init:{appKey}:{platform}:{appVersion}`         |
| TTL         | 60 seconds (configurable via `vergate.cache.init-ttl-seconds`) |
| Eviction    | Pattern-based: `vergate:init:{appKey}:*` on any admin mutation |
| Fallback    | On Valkey failure → serve from DB, log warning          |

**Why cache at this level (not Spring `@Cacheable`)**:
- Custom cache key includes `appVersion` for version-specific responses
- Pattern-based eviction (`KEYS`) not supported by Spring Cache abstraction
- Graceful fallback on Valkey failure requires try/catch, not annotation

## Database Schema

All primary keys are **UUIDv7** (time-ordered, app-generated). `app_key` is 22-char Base62 encoding of the app UUID.

```sql
-- V1: apps, app_versions, maintenances, notices, remote_configs (BIGSERIAL → replaced in V3)
-- V2: legal_documents
-- V3: all tables recreated with UUID PRIMARY KEY

apps          (id UUID PK, app_key VARCHAR(22) UNIQUE, name, platform, store_url, is_active, timestamps)
app_versions  (id UUID PK, app_id UUID FK, min_version, latest_version, force_update, update_message, is_active, timestamps)
maintenances  (id UUID PK, app_id UUID FK, title, message, start_at, end_at, is_active, timestamps)
notices       (id UUID PK, app_id UUID FK, title, message, image_url, deep_link, display_type, priority, date_range, is_active, timestamps)
remote_configs(id UUID PK, app_id UUID FK, config_key, config_value, value_type, ab_ratio, is_active, timestamps, UNIQUE(app_id, config_key))
legal_documents(id UUID PK, app_id UUID FK, doc_type, title, content, content_type, is_active, timestamps, UNIQUE(app_id, doc_type))
```

**Migrations**: `V1__init_schema.sql`, `V2__add_legal_documents.sql`, `V3__use_uuid_primary_keys.sql`

## Hexagonal Structure (this scope)

```
src/main/kotlin/com/verobee/vergate/
├── domain/
│   ├── model/           App, AppVersion, Maintenance, Notice, RemoteConfig, LegalDocument
│   ├── service/         GatewayService, AppService, VersionService, MaintenanceService,
│   │                    NoticeService, RemoteConfigService, LegalDocumentService
│   ├── util/            AppKeyGenerator (UUIDv7 + Base62 appKey)
│   └── exception/       DomainException
├── ports/out/
│   ├── AppRepositoryPort, AppVersionRepositoryPort, MaintenanceRepositoryPort
│   ├── NoticeRepositoryPort, RemoteConfigRepositoryPort, LegalDocumentRepositoryPort
│   └── GatewayCachePort
├── adapters/in/rest/
│   ├── client/          GatewayController, LegalDocumentController
│   ├── admin/           AppAdminController, VersionAdminController, MaintenanceAdminController,
│   │                    NoticeAdminController, RemoteConfigAdminController, LegalDocAdminController
│   └── dto/             InitResponse (with legal[]), LegalDocDto, ...
├── adapters/out/
│   ├── persistence/     JPA entities (UUID PK), repositories, mappers, adapters
│   └── cache/           ValkeyCacheAdapter
└── config/              SecurityConfig, RedisConfig, SwaggerConfig, WebConfig
```

## Client Init Flow

```
Client App                  GatewayController              GatewayService                    Valkey / DB
    |                            |                              |                               |
    |-- GET /api/v1/init ------->|                              |                               |
    |                            |-- init(appKey,platform,ver)->|                               |
    |                            |                              |-- getInitResponse(cacheKey) -->|
    |                            |                              |<-- cached JSON (or null) ------|
    |                            |                              |                               |
    |                            |                              |  [cache miss]                 |
    |                            |                              |-- findByAppKey() ------------>|
    |                            |                              |<-- App ----------------------|
    |                            |                              |                               |
    |                            |                              |  [isActive=false? → return terminated]
    |                            |                              |                               |
    |                            |                              |-- findActiveVersion() ------->|
    |                            |                              |-- findActiveMaintenance() --->|
    |                            |                              |<-- version + maintenance ------|
    |                            |                              |                               |
    |                            |                              |-- setInitResponse(key, json)->|
    |                            |<-- InitResponse -------------|                               |
    |<-- ApiResponse<InitResponse>|                              |                               |
```

## API Endpoints (this scope)

### Client

```
GET /api/v1/init?app_key={key}&platform={platform}&app_version={version}
```

### Admin — Apps

```
POST   /api/v1/admin/apps
GET    /api/v1/admin/apps
GET    /api/v1/admin/apps/{id}
PUT    /api/v1/admin/apps/{id}
DELETE /api/v1/admin/apps/{id}
```

### Admin — Versions

```
POST   /api/v1/admin/apps/{appId}/versions
GET    /api/v1/admin/apps/{appId}/versions
PUT    /api/v1/admin/apps/{appId}/versions/{id}
```

### Admin — Maintenances

```
POST   /api/v1/admin/apps/{appId}/maintenances
GET    /api/v1/admin/apps/{appId}/maintenances
PUT    /api/v1/admin/apps/{appId}/maintenances/{id}
DELETE /api/v1/admin/apps/{appId}/maintenances/{id}
```

### Admin — Notices, Configs, Legal Docs

```
POST/GET/PUT/DELETE /api/v1/admin/apps/{appId}/notices
POST/GET/PUT/DELETE /api/v1/admin/apps/{appId}/configs
POST/GET/PUT/DELETE /api/v1/admin/apps/{appId}/legal-docs
```

### Client — Legal Document Pages

```
GET /api/v1/legal/{appKey}/privacy-policy   → text/html
GET /api/v1/legal/{appKey}/terms            → text/html
```

## Dependencies

```
App Registration ← Version Check (needs app_id)
App Registration ← Maintenance Mode (needs app_id)
App Registration ← Service Shutdown (isActive field on App)
Valkey ← All client reads (cache layer)
```

## Out of Scope

| Excluded              | Reason                                     | When            |
| --------------------- | ------------------------------------------ | --------------- |
| Policy acceptance tracking | No user consent flow needed — URL only | Phase 2+      |
| Notice/Popup          | Not a Phase 0 requirement                  | Phase 1 (Q1)   |
| Admin Auth (JWT)      | Permit all for MVP; add in Phase 1         | Phase 1 (Q2)   |
| A/B Testing           | Enhancement over base config               | Phase 2 (Q2)   |
| Admin Dashboard       | API-first; UI later                        | Phase 3 (Q3)   |

## Test Results

| Category    | Cases | Tool                         | Status |
| ----------- | ----- | ---------------------------- | ------ |
| Unit        | 46    | MockK + JUnit5               | ✅ Pass |
| Slice       | 2     | `@WebMvcTest`                | ✅ Pass |
| Integration | 5     | Testcontainers (PG + Valkey) | ✅ Pass |
| E2E         | 11    | `@SpringBootTest` + TC       | ✅ Pass |
| **Total**   | **64**| —                            | ✅ All pass |

**Coverage achieved**: **90%+** (gate: 80%) | `./gradlew test jacocoTestCoverageVerification`

**SSOT**: `docs/llm/policies/testing.md`
