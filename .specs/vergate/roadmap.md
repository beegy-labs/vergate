# Vergate Roadmap

> App Gateway Platform | **Last Updated**: 2026-02-22

## Vision

Central gateway that client apps call once on launch (`/api/v1/init`) to get version rules, maintenance status, notices, and remote config. Admins manage everything from a single API.

## Architecture

- **Pattern**: Hexagonal Architecture (Ports & Adapters)
- **Stack**: Kotlin + Spring Boot 3.x + PostgreSQL + Valkey
- **Policy**: `docs/llm/policies/hexagonal-architecture.md`

## 2026 Roadmap

| Phase | Period | Scope                                                                       | Status      |
| ----- | ------ | --------------------------------------------------------------------------- | ----------- |
| 0     | Q1     | Version check + force update, legal document hosting, service shutdown      | **Done**    |
| 0.5   | Q1     | UUIDv7 primary keys, Helm chart + ESO deployment                            | **Done**    |
| 1     | Q2     | Admin Auth (JWT)                                                            | Planned     |
| 2     | Q2     | Notice/Popup management                                                     | Planned     |
| 3     | Q2     | A/B Testing + Audit Log                                                     | Planned     |
| 4     | Q3     | Admin Dashboard (Web UI)                                                    | Planned     |
| 5     | Q3     | Webhook Notifications                                                       | Planned     |

## Feature Map

| #   | Feature                | Client API                   | Admin API                             |
| --- | ---------------------- | ---------------------------- | ------------------------------------- |
| F1  | App Registration       | —                            | CRUD `/admin/apps`                    |
| F2  | Version Check          | `init` response              | CRUD `/admin/apps/{id}/versions`      |
| F3  | Force Update           | `init` response              | Set `forceUpdate` flag                |
| F4  | Maintenance Mode       | `init` response              | CRUD `/admin/apps/{id}/maintenances`  |
| F5  | Service Shutdown       | `init` response              | Set `isActive=false`                  |
| F6  | Notice/Popup           | `init` response              | CRUD `/admin/apps/{id}/notices`       |
| F7  | Remote Config          | `init` response              | CRUD `/admin/apps/{id}/configs`       |
| F8  | Admin Auth             | —                            | JWT login/refresh                     |
| F9  | Valkey Cache           | Transparent                  | Auto-eviction on write                |
| F10 | Legal Document Hosting | `GET /legal/{appKey}/{type}` | CRUD `/admin/apps/{id}/legal-docs`    |

## Key Decisions

| #  | Decision                           | Rationale                                   |
| -- | ---------------------------------- | ------------------------------------------- |
| 1  | Single `/init` endpoint            | Minimize client round-trips                 |
| 2  | Hexagonal Architecture             | Domain testable without Spring/JPA          |
| 3  | Valkey cache with DB fallback      | High traffic resilience; no single point of failure |
| 4  | Flyway for migrations              | Version-controlled schema; no manual DDL    |
| 5  | `isActive` flag for shutdown       | Reversible; no data deletion needed         |
| 6  | Semver comparison in domain        | No library dependency; pure Kotlin logic    |
| 7  | UUIDv7 for all primary keys        | Time-ordered, app-generated, URL-safe appKey (Base62) |
| 8  | Legal docs as hosted HTML pages    | Client opens URL directly; no content in init response |
| 9  | GitOps via ArgoCD + platform-gitops | Secrets via Vault ESO; no direct helm install in prod |
