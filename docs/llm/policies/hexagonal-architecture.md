# Hexagonal Architecture Policy

> SSOT for backend architecture (Kotlin + Spring Boot) | **Last Updated**: 2026-02-23

## Overview

Vergate uses **Hexagonal Architecture** (Ports & Adapters) with Kotlin + Spring Boot. This isolates business logic from infrastructure, making the service testable, portable, and maintainable.

## Core Principle

```
Adapters (outer) --> Ports (boundary) --> Domain (inner)
```

**Dependency flows INWARD only.** Domain MUST NOT import from Adapters or Spring framework code.

## Layer Definitions

| Layer        | Responsibility                    | Depends On | Example                     |
| ------------ | --------------------------------- | ---------- | --------------------------- |
| **Domain**   | Business logic, entities, rules   | Nothing    | `domain/`                   |
| **Ports**    | Output interfaces (contracts)     | Domain     | `ports/out/`                |
| **Adapters** | Infrastructure implementations    | Ports      | `adapters/in/`, `adapters/out/` |
| **Config**   | Spring Boot wiring, DI            | All        | `config/`                   |

> No `ports/in/` layer — domain services ARE the use cases. No inbound port interfaces.

## Directory Structure

```
src/main/kotlin/com/verobee/vergate/
├── domain/                          # Core business logic (pure Kotlin)
│   ├── model/                       #   Domain entities (no Spring/JPA)
│   │   ├── App.kt
│   │   ├── AppVersion.kt
│   │   ├── LegalDocument.kt
│   │   ├── Maintenance.kt
│   │   ├── Notice.kt
│   │   └── RemoteConfig.kt
│   ├── service/                     #   Business logic (Spring @Service, uses ports)
│   │   ├── AppService.kt
│   │   ├── VersionService.kt
│   │   ├── GatewayService.kt        #   Aggregates init response
│   │   ├── LegalDocumentService.kt
│   │   ├── MaintenanceService.kt
│   │   ├── NoticeService.kt
│   │   └── RemoteConfigService.kt
│   ├── util/                        #   Domain utilities
│   │   └── AppKeyGenerator.kt       #   UUIDv7-based 22-char app key generator
│   └── exception/                   #   Domain exceptions
│       └── DomainException.kt
│
├── ports/
│   └── out/                         #   Outbound ports (repository/cache interfaces)
│       ├── AppRepositoryPort.kt
│       ├── AppVersionRepositoryPort.kt
│       ├── LegalDocumentRepositoryPort.kt
│       ├── MaintenanceRepositoryPort.kt
│       ├── NoticeRepositoryPort.kt
│       ├── RemoteConfigRepositoryPort.kt
│       └── GatewayCachePort.kt      #   Cache abstraction (implemented by ValkeyCacheAdapter)
│
├── adapters/
│   ├── in/                          #   Inbound adapters (driving)
│   │   └── rest/
│   │       ├── client/              #     Client-facing APIs
│   │       │   └── GatewayController.kt   # GET /api/v1/init, GET /api/v1/legal/**
│   │       ├── admin/               #     Admin management APIs
│   │       │   ├── AppAdminController.kt
│   │       │   ├── VersionAdminController.kt
│   │       │   ├── MaintenanceAdminController.kt
│   │       │   ├── NoticeAdminController.kt
│   │       │   ├── ConfigAdminController.kt
│   │       │   └── LegalDocAdminController.kt
│   │       └── dto/
│   │           ├── client/
│   │           │   ├── InitRequest.kt
│   │           │   └── InitResponse.kt
│   │           └── admin/
│   │               ├── AppDto.kt
│   │               ├── VersionDto.kt
│   │               ├── MaintenanceDto.kt
│   │               ├── NoticeDto.kt
│   │               ├── ConfigDto.kt
│   │               └── LegalDocDto.kt
│   └── out/                         #   Outbound adapters (driven)
│       ├── persistence/
│       │   ├── entity/              #     JPA entities (@Entity)
│       │   ├── repository/          #     Spring Data JPA interfaces
│       │   ├── mapper/              #     Domain Model <-> JPA Entity
│       │   └── adapter/             #     Port implementations (JpaAdapter)
│       └── cache/
│           └── ValkeyCacheAdapter.kt  #   Implements GatewayCachePort
│
├── config/                          # Spring Boot configurations
│   ├── SecurityConfig.kt
│   ├── RedisConfig.kt               # Valkey connection config
│   ├── SwaggerConfig.kt
│   └── WebConfig.kt
│
└── common/                          # Cross-cutting concerns
    ├── exception/
    │   ├── GlobalExceptionHandler.kt
    │   └── ErrorCode.kt
    └── response/
        └── ApiResponse.kt
```

## Domain Models

| Model           | Key Fields                                                  |
| --------------- | ----------------------------------------------------------- |
| `App`           | appKey (22-char), name, platform (IOS/ANDROID/WEB), storeUrl, isActive |
| `AppVersion`    | minVersion, latestVersion, forceUpdate, updateMessage       |
| `LegalDocument` | docType (PRIVACY_POLICY/TERMS_OF_SERVICE), contentType (MARKDOWN/HTML) |
| `Notice`        | displayType (ONCE/DAILY/ALWAYS), priority, startAt, endAt  |
| `Maintenance`   | title, message, startAt, endAt                              |
| `RemoteConfig`  | configKey, configValue, valueType (STRING/NUMBER/BOOLEAN/JSON), abRatio |

## Naming Conventions

| Type              | Pattern                          | Example                       |
| ----------------- | -------------------------------- | ----------------------------- |
| Outbound Port     | `{Entity}RepositoryPort`         | `AppRepositoryPort`           |
| Cache Port        | `{Name}CachePort`                | `GatewayCachePort`            |
| Domain Model      | `{Entity}`                       | `App`, `LegalDocument`        |
| Domain Service    | `{Name}Service`                  | `GatewayService`              |
| REST Controller   | `{Entity}Controller`             | `GatewayController`           |
| Admin Controller  | `{Entity}AdminController`        | `AppAdminController`          |
| JPA Entity        | `{Entity}JpaEntity`              | `AppJpaEntity`                |
| JPA Adapter       | `{Entity}JpaAdapter`             | `AppJpaAdapter`               |
| DTO (request)     | `Create{Entity}Request`          | `CreateAppRequest`            |
| DTO (response)    | `{Entity}Response`               | `AppResponse`                 |
| Mapper            | `{Entity}Mapper`                 | `AppMapper`                   |

## Dependency Injection

```kotlin
// Adapter implements Port
@Repository
class AppJpaAdapter(
    private val jpaRepository: AppJpaRepository,
    private val mapper: AppMapper
) : AppRepositoryPort { ... }

// Domain service depends on Port (interface, not impl)
@Service
class AppService(
    private val appRepository: AppRepositoryPort
) { ... }

// Cache adapter implements cache port
@Component
class ValkeyCacheAdapter(
    private val redisTemplate: RedisTemplate<String, String>
) : GatewayCachePort { ... }
```

## Testing Strategy

| Layer        | Test Type   | Tooling                     |
| ------------ | ----------- | --------------------------- |
| Domain Model | Unit        | Pure Kotlin (no mocks)      |
| Domain Service | Unit      | MockK                       |
| REST Controller | Slice    | `@WebMvcTest` + MockK       |
| JPA Adapter  | Integration | Testcontainers (PostgreSQL) |
| Cache Adapter | Integration | Testcontainers (Valkey)     |
| E2E          | Integration | Full stack Testcontainers   |

## Rules

| Rule                          | Description                                       |
| ----------------------------- | ------------------------------------------------- |
| No Spring in Domain           | Domain MUST NOT import `org.springframework.*`    |
| No JPA in Domain              | Domain models have no `@Entity`, `@Column`        |
| Ports are interfaces          | Ports define contracts, never implementations     |
| Adapters implement Ports      | Each adapter implements one or more ports         |
| DTOs live in Adapters         | Domain models are separate from DTOs              |
| Mappers bridge layers         | DTO <-> Domain Model conversion via mapper        |
| JPA Entities in Adapters      | `@Entity` classes live in `adapters/out/persistence/entity/` |
| Cache fire-and-forget         | Cache writes MUST NOT block the response          |

## Anti-Patterns

```
❌ Domain importing from org.springframework
❌ Controllers calling JPA repositories directly
❌ Domain models with @Entity or @Column decorators
❌ Business logic in controllers or adapters
❌ Skipping the port layer (controller -> JPA directly)
❌ Returning JPA entities from controllers
❌ Blocking on cache write failure
```

## References

- **Quick Reference**: `.ai/architecture.md`
