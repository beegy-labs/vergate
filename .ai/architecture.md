# Architecture

> Backend: Hexagonal (Kotlin + Spring Boot) | **Last Updated**: 2026-02-21

## Hexagonal Architecture (Ports & Adapters)

```
Adapters (outer) --> Ports (boundary) --> Domain (inner)
```

| Layer    | Role                     | Directory                                    |
| -------- | ------------------------ | -------------------------------------------- |
| Domain   | Business logic, Entities | `domain/models/`, `domain/services/`         |
| Ports    | Interfaces (in/out)      | `ports/in/`, `ports/out/`                    |
| Adapters | Infrastructure impl      | `adapters/in/rest/`, `adapters/out/persistence/` |

**Dependency flows INWARD only.** Domain MUST NOT import from Adapters or Spring.

## Module Structure

```
src/main/kotlin/com/verobee/vergate/
├── domain/          # Core business logic (pure Kotlin)
├── ports/in/        # Use case interfaces
├── ports/out/       # Repository interfaces
├── adapters/in/     # REST controllers, DTOs
├── adapters/out/    # JPA repositories, Redis cache
└── config/          # Spring config, DI wiring
```

**SSOT**: `docs/llm/policies/hexagonal-architecture.md`
