# Architecture

> Backend: Hexagonal (Kotlin + Spring Boot) | **Last Updated**: 2026-02-23

## Hexagonal Architecture (Ports & Adapters)

```
Adapters (outer) --> Ports (boundary) --> Domain (inner)
```

| Layer    | Role                     | Directory                                    |
| -------- | ------------------------ | -------------------------------------------- |
| Domain   | Business logic, Entities | `domain/model/`, `domain/service/`           |
| Ports    | Output interfaces        | `ports/out/`                                 |
| Adapters | Infrastructure impl      | `adapters/in/rest/`, `adapters/out/`         |

**Dependency flows INWARD only.** Domain MUST NOT import from Adapters or Spring.

## Module Structure

```
src/main/kotlin/com/verobee/vergate/
├── domain/
│   ├── model/           # App, AppVersion, LegalDocument, Notice, Maintenance, RemoteConfig
│   ├── service/         # AppService, VersionService, GatewayService, ...
│   ├── util/            # AppKeyGenerator (UUIDv7-based)
│   └── exception/       # DomainException
├── ports/
│   └── out/             # AppRepositoryPort, GatewayCachePort, ...
├── adapters/
│   ├── in/rest/
│   │   ├── client/      # GatewayController (GET /api/v1/init)
│   │   ├── admin/       # App/Version/Notice/Maintenance/Config/LegalDoc controllers
│   │   └── dto/         # Request/Response DTOs
│   └── out/
│       ├── persistence/ # JPA entities, mappers, adapters
│       └── cache/       # ValkeyCacheAdapter (implements GatewayCachePort)
├── config/              # SecurityConfig, RedisConfig, SwaggerConfig, WebConfig
└── common/              # ApiResponse, ErrorCode, GlobalExceptionHandler
```

> No `ports/in/` — domain services are the use cases directly (no inbound port interface layer).

**SSOT**: `docs/llm/policies/hexagonal-architecture.md`
