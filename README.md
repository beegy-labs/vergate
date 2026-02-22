# Vergate

> App Gateway Platform — A single API that client apps call on every launch to get version rules, maintenance status, notices, and remote config.

## Overview

Vergate centralizes the control plane for your mobile and web apps. Instead of managing version checks, maintenance windows, feature flags, and legal documents across multiple services, everything is delivered through one endpoint.

```
GET /api/v1/init?app_key={key}&platform={platform}&app_version={version}
```

```json
{
  "service":     { "active": true },
  "update":      { "force": false, "latest_version": "3.0.0", "min_version": "2.0.0" },
  "maintenance": { "active": false },
  "notices":     [],
  "config":      { "feature_x": true },
  "legal":       [{ "type": "PRIVACY_POLICY", "title": "Privacy Policy", "url": "..." }]
}
```

## Features

| Feature | Description |
|---------|-------------|
| **Version Check** | Semver comparison — tells client whether update is optional or forced |
| **Maintenance Mode** | Time-range scheduling; client receives active/inactive status |
| **Service Shutdown** | Permanently block access to terminated apps via `isActive` flag |
| **Notices** | Push popups to clients with display types (ONCE, DAILY, ALWAYS) |
| **Remote Config** | Feature flags and key-value config delivered on every launch |
| **Legal Documents** | Host privacy policy / terms as HTML pages (Markdown → HTML rendering) |
| **Valkey Cache** | 60s TTL on init responses; pattern-based eviction on any admin mutation |

## Tech Stack

| Category | Technology |
|----------|-----------|
| Language | Kotlin 2.1 |
| Framework | Spring Boot 3.4 |
| Build | Gradle (Kotlin DSL) |
| Database | PostgreSQL 17 + Spring Data JPA |
| Cache | Valkey 8 (Redis-compatible) |
| Migration | Flyway |
| Auth | Spring Security + JWT |
| API Docs | SpringDoc OpenAPI (Swagger UI) |
| Test | JUnit5 + MockK + Testcontainers |
| Deploy | Helm + Kubernetes + ESO |

## Quick Start (Local)

**Prerequisites**: Docker, JDK 21

```bash
# 1. Start PostgreSQL + Valkey
docker compose up postgres redis -d

# 2. Run the app
./gradlew bootRun

# 3. Open Swagger UI
open http://localhost:8080/swagger-ui
```

Or run everything with Docker Compose:

```bash
./gradlew bootJar -x test
docker compose up --build -d
```

## API

### Client

```
GET /api/v1/init?app_key={key}&platform={IOS|ANDROID|WEB}&app_version={semver}
```

### Legal Document Pages (public)

```
GET /api/v1/legal/{appKey}/privacy-policy    → text/html
GET /api/v1/legal/{appKey}/terms             → text/html
```

### Admin

```
# Apps
POST/GET/PUT/DELETE  /api/v1/admin/apps
POST/GET/PUT         /api/v1/admin/apps/{appId}/versions
POST/GET/PUT/DELETE  /api/v1/admin/apps/{appId}/maintenances
POST/GET/PUT/DELETE  /api/v1/admin/apps/{appId}/notices
POST/GET/PUT/DELETE  /api/v1/admin/apps/{appId}/configs
POST/GET/PUT/DELETE  /api/v1/admin/apps/{appId}/legal-docs
```

Full API reference: `http://localhost:8080/swagger-ui`

## Database

Six tables, all using **UUIDv7** primary keys (time-ordered, app-generated):

```
apps · app_versions · maintenances · notices · remote_configs · legal_documents
```

Flyway manages schema migrations:

| Version | Description |
|---------|-------------|
| V1 | Initial schema (5 tables) |
| V2 | Add `legal_documents` table |
| V3 | Migrate all primary keys to UUID |

## Deployment

Helm chart with optional in-chart PostgreSQL and Valkey (disabled by default). Secrets injected via [External Secrets Operator](https://external-secrets.io/).

```bash
# Deploy (existing PostgreSQL + Valkey)
helm upgrade --install vergate helm/vergate \
  -f helm/vergate/values-prod.yaml \
  -n vergate --create-namespace
```

**`values-prod.yaml`** — configure your domain, image tag, and ESO secret store:

```yaml
image:
  repository: ghcr.io/beegy-labs/vergate
  tag: "0.1.0"

postgresql:
  enabled: false   # use existing cluster DB

valkey:
  enabled: false   # use existing cluster Valkey

externalSecrets:
  enabled: true
  secretStoreRef:
    name: cluster-secret-store
    kind: ClusterSecretStore
  data:
    - secretKey: DATABASE_URL
      remoteRef: { key: vergate/production, property: database_url }
    # ... DATABASE_USERNAME, DATABASE_PASSWORD, REDIS_HOST, REDIS_PORT, JWT_SECRET

ingress:
  enabled: true
  host: vergate.example.com
```

See [`docs/llm/policies/deployment.md`](docs/llm/policies/deployment.md) for full deployment guide.

## Development

```bash
# Run all tests (64 tests, 90%+ coverage)
./gradlew test

# Build JAR
./gradlew bootJar

# Check coverage
./gradlew test jacocoTestReport jacocoTestCoverageVerification
```

### Test Breakdown

| Type | Count | Tool |
|------|-------|------|
| Unit | 46 | MockK + JUnit5 |
| Slice | 2 | `@WebMvcTest` |
| Integration | 5 | Testcontainers |
| E2E | 11 | `@SpringBootTest` + Testcontainers |

### Architecture

Hexagonal Architecture (Ports & Adapters) — domain is pure Kotlin with no Spring or JPA dependencies.

```
adapters/in  →  ports/in  →  domain  →  ports/out  →  adapters/out
(REST)           (use cases)  (services)  (repos)       (JPA / Valkey)
```

## Environment Variables

| Variable | Description |
|----------|-------------|
| `DATABASE_URL` | JDBC URL (`jdbc:postgresql://host:5432/vergate`) |
| `DATABASE_USERNAME` | DB username |
| `DATABASE_PASSWORD` | DB password |
| `REDIS_HOST` | Valkey/Redis host |
| `REDIS_PORT` | Valkey/Redis port (default: 6379) |
| `JWT_SECRET` | JWT signing secret (min 32 chars) |

Copy `.env.example` to `.env` for local development.

## License

MIT
