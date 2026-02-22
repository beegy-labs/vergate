# Hexagonal Architecture Policy

> SSOT for backend architecture (Kotlin + Spring Boot) | **Last Updated**: 2026-02-21

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
| **Ports**    | Interfaces (contracts)            | Domain     | `ports/in/`, `ports/out/`   |
| **Adapters** | Infrastructure implementations    | Ports      | `adapters/in/`, `adapters/out/` |
| **Config**   | Spring Boot wiring, DI            | All        | `config/`                   |

## Directory Structure

```
src/main/kotlin/com/verobee/vergate/
├── domain/                      # Core business logic (pure Kotlin)
│   ├── model/                   #   Entities, Value Objects
│   │   ├── App.kt
│   │   ├── AppVersion.kt
│   │   ├── Maintenance.kt
│   │   ├── Notice.kt
│   │   └── RemoteConfig.kt
│   ├── service/                 #   Domain services (orchestration)
│   │   └── GatewayDomainService.kt
│   └── exception/               #   Domain-specific exceptions
│       └── DomainException.kt
│
├── ports/
│   ├── in/                      #   Inbound ports (use case interfaces)
│   │   ├── GatewayUseCase.kt
│   │   ├── AppUseCase.kt
│   │   ├── VersionUseCase.kt
│   │   ├── MaintenanceUseCase.kt
│   │   ├── NoticeUseCase.kt
│   │   └── RemoteConfigUseCase.kt
│   └── out/                     #   Outbound ports (repository interfaces)
│       ├── AppRepositoryPort.kt
│       ├── AppVersionRepositoryPort.kt
│       ├── MaintenanceRepositoryPort.kt
│       ├── NoticeRepositoryPort.kt
│       └── RemoteConfigRepositoryPort.kt
│
├── adapters/
│   ├── in/                      #   Inbound adapters (driving)
│   │   └── rest/                #     REST controllers
│   │       ├── client/          #       Client-facing APIs
│   │       │   └── GatewayController.kt
│   │       ├── admin/           #       Admin management APIs
│   │       │   ├── AppAdminController.kt
│   │       │   ├── VersionAdminController.kt
│   │       │   ├── MaintenanceAdminController.kt
│   │       │   ├── NoticeAdminController.kt
│   │       │   └── ConfigAdminController.kt
│   │       └── dto/             #       Request/Response DTOs
│   │           ├── client/
│   │           │   ├── InitRequest.kt
│   │           │   └── InitResponse.kt
│   │           └── admin/
│   │               ├── AppDto.kt
│   │               ├── VersionDto.kt
│   │               ├── MaintenanceDto.kt
│   │               ├── NoticeDto.kt
│   │               └── ConfigDto.kt
│   └── out/                     #   Outbound adapters (driven)
│       ├── persistence/         #     JPA repository implementations
│       │   ├── entity/          #       JPA entities (@Entity)
│       │   ├── repository/      #       Spring Data JPA interfaces
│       │   ├── mapper/          #       JPA Entity <-> Domain Model
│       │   └── adapter/         #       Port implementations
│       └── cache/               #     Redis cache adapter
│           └── RedisCacheAdapter.kt
│
├── config/                      # Spring Boot configurations
│   ├── SecurityConfig.kt
│   ├── RedisConfig.kt
│   ├── SwaggerConfig.kt
│   └── WebConfig.kt
│
└── common/                      # Cross-cutting concerns
    ├── exception/
    │   ├── GlobalExceptionHandler.kt
    │   └── ErrorCode.kt
    └── response/
        └── ApiResponse.kt
```

## Naming Conventions

| Type            | Pattern                        | Example                       |
| --------------- | ------------------------------ | ----------------------------- |
| Inbound Port    | `{UseCase}UseCase`             | `GatewayUseCase`              |
| Outbound Port   | `{Entity}RepositoryPort`       | `AppRepositoryPort`           |
| Domain Model    | `{Entity}`                     | `App`                         |
| Domain Service  | `{Name}DomainService`          | `GatewayDomainService`        |
| REST Controller | `{Entity}Controller`           | `GatewayController`           |
| JPA Entity      | `{Entity}JpaEntity`            | `AppJpaEntity`                |
| Repository Impl | `{Entity}JpaAdapter`           | `AppJpaAdapter`               |
| DTO             | `{Action}{Entity}Request/Response` | `CreateAppRequest`        |
| Mapper          | `{Entity}Mapper`               | `AppMapper`                   |

## Dependency Injection

Use Spring DI (`@Component`, `@Service`) to wire ports to adapters:

```kotlin
// Adapter implements Port
@Repository
class AppJpaAdapter(
    private val jpaRepository: AppJpaRepository,
    private val mapper: AppMapper
) : AppRepositoryPort {
    // ...
}

// Domain service depends on Port (interface)
@Service
class AppService(
    private val appRepositoryPort: AppRepositoryPort
) : AppUseCase {
    // ...
}
```

## Testing Strategy

| Layer    | Test Type   | Dependencies          |
| -------- | ----------- | --------------------- |
| Domain   | Unit        | None (pure Kotlin)    |
| Ports    | Unit        | Mock domain           |
| Adapters | Integration | MockK / Testcontainers|
| E2E      | E2E         | Full stack            |

## Rules

| Rule                          | Description                                     |
| ----------------------------- | ----------------------------------------------- |
| No Spring in Domain           | Domain MUST NOT import Spring annotations       |
| No JPA in Domain              | Domain models have no `@Entity`, `@Column` etc  |
| Ports are interfaces          | Ports define contracts, never implementations   |
| Adapters implement Ports      | Each adapter implements one or more ports        |
| DTOs live in Adapters         | Domain models are separate from DTOs            |
| Mappers bridge layers         | Use mappers to convert DTO <-> Domain Model     |
| JPA Entities in Adapters      | `@Entity` classes live in adapters/out/persistence |

## Anti-Patterns

```
❌ Domain importing from org.springframework
❌ Controllers calling JPA repositories directly
❌ Domain models with @Entity or @Column decorators
❌ Business logic in controllers or adapters
❌ Skipping the port layer (controller -> repository)
❌ Returning JPA entities from controllers
```

## References

- **Quick Reference**: `.ai/architecture.md`
