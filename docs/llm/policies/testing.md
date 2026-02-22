# Testing Policy

> SSOT for test strategy (Kotlin + Spring Boot + Hexagonal) | **Last Updated**: 2026-02-22

## Overview

Vergate follows a **Test Pyramid** adapted for Hexagonal Architecture. Domain logic (inner) gets the heaviest unit coverage; adapters (outer) use integration tests with Testcontainers; E2E covers only critical user journeys.

## Test Pyramid

```
         /\
        /E2E\         ~5%   (2-3 scenarios)
       /------\
      /Integr. \      ~15%  (JPA queries, Valkey TTL)
     /----------\
    /   Slice    \     ~10%  (Controller JSON shape)
   /--------------\
  /   Unit Tests    \  ~70%  (Domain services, models)
 /--------------------\
```

### Why This Ratio

| Level       | Ratio | Reason                                                                  |
| ----------- | ----- | ----------------------------------------------------------------------- |
| Unit        | ~70%  | Domain services are pure Kotlin — fast, no I/O, highest bug-prevention ROI |
| Slice       | ~10%  | Controllers are thin adapters; only verify HTTP contract, not logic      |
| Integration | ~15%  | JPA queries and Valkey behavior can't be unit-tested reliably           |
| E2E         | ~5%   | Expensive to maintain, slow to run; limit to irreplaceable scenarios    |

---

## Layer Coverage Targets

| Layer              | Target | Test Type   | Reason                                                                |
| ------------------ | ------ | ----------- | --------------------------------------------------------------------- |
| **Domain Service** | 90%+   | Unit        | All business decisions live here; bugs directly affect client apps     |
| **Domain Model**   | 95%+   | Unit        | Enums, value parsing are trivially testable and critical for correctness|
| **REST Controller**| 80%+   | Slice       | Must verify param binding, JSON field names, status codes             |
| **JPA Adapter**    | 70%+   | Integration | Custom JPQL/time-range queries can silently break on schema changes   |
| **Valkey Adapter** | 60%+   | Integration | TTL, eviction patterns, and failure fallback need real Valkey to verify|
| **Overall**        | **80%**| Mixed       | Industry standard for production services; CDD rule                   |

---

## Feature Test Matrix

### Gateway Init API (highest priority — called on every app launch)

| Case                                    | Type  | Why Required                                                   |
| --------------------------------------- | ----- | -------------------------------------------------------------- |
| App not found → 404                     | Unit  | Prevents silent failures when clients send wrong app_key       |
| App inactive (terminated) → blocked     | Unit  | Core feature: service shutdown must be guaranteed              |
| Version < minVersion → force update     | Unit  | Incorrect version comparison = users stuck on broken versions  |
| Version >= min, < latest → soft update  | Unit  | Must distinguish "should update" from "must update"            |
| Active maintenance → info included      | Unit  | Time-range query logic is error-prone                          |
| Valkey cache hit → skip DB              | Unit  | Proves caching actually works; high-traffic path               |
| Valkey down → DB fallback               | Unit  | Valkey is not HA-guaranteed; app must never crash on cache fail|
| Config type parsing (BOOL, NUM, JSON)   | Unit  | String→typed conversion has edge cases (null, malformed)       |

### Version Admin API

| Case                                    | Type  | Why Required                                                   |
| --------------------------------------- | ----- | -------------------------------------------------------------- |
| Create version → cache evicted          | Unit  | Stale cache = clients get wrong update info                    |
| Update version → cache evicted          | Unit  | Same as above                                                  |
| Non-existent ID → 404                   | Unit  | Prevents silent no-ops on admin mistakes                       |

### App Admin API

| Case                                    | Type  | Why Required                                                   |
| --------------------------------------- | ----- | -------------------------------------------------------------- |
| Duplicate appKey → 409                  | Unit  | DB unique constraint alone is not user-friendly error          |
| Deactivate app → cache evicted          | Unit  | Service termination must take effect immediately               |
| Delete app → cascade verified           | Integ | ON DELETE CASCADE must actually remove child rows              |

### JPA Adapter (Integration — Testcontainers PostgreSQL)

| Case                                    | Why Required                                                        |
| --------------------------------------- | ------------------------------------------------------------------- |
| Maintenance time-range query accuracy   | `startAt <= now AND endAt >= now` is the #1 query bug source        |
| Notice active + period filter           | Nullable start/end dates make the WHERE clause complex              |
| RemoteConfig unique(app_id, config_key) | Must confirm DB constraint works, not just application-level check  |

### Valkey Adapter (Integration — Testcontainers Valkey)

| Case                                    | Why Required                                                        |
| --------------------------------------- | ------------------------------------------------------------------- |
| Set with TTL → expires after TTL        | Proves cache doesn't grow unbounded                                 |
| Evict by appKey pattern                 | `KEYS vergate:init:{appKey}:*` must match correctly                 |
| Connection failure → graceful fallback  | App must serve from DB, never throw on cache failure                |

### E2E (3 scenarios only)

| Scenario             | Why Required                                                             |
| -------------------- | ------------------------------------------------------------------------ |
| **Happy path**       | Full flow: create app → set version → init → correct response. Proves all layers integrate. |
| **Service termination** | Create app → deactivate → init → blocked. The most critical business feature. |
| **Force update**     | Set minVersion=2.0.0 → init with 1.0.0 → force=true. Version comparison across all layers. |

### Why Only 3 E2E Tests

- E2E requires full Spring context + Testcontainers (PostgreSQL + Valkey) → **~10s startup per run**
- Each additional E2E test adds maintenance cost when schema or API changes
- Unit + Integration already cover 95% of edge cases; E2E only catches wiring bugs
- These 3 scenarios represent the **irreplaceable business-critical paths**

---

## What NOT to Test (and Why)

| Skip                              | Reason                                                          |
| --------------------------------- | --------------------------------------------------------------- |
| Mapper field-by-field assertions  | Mappers are mechanical; integration tests catch mapping bugs    |
| DTO getter/setter                 | Kotlin data classes generate these; testing adds zero value     |
| Spring Config classes             | SecurityConfig, SwaggerConfig are declarative; verified by boot |
| JPA base methods (findById, save) | Spring Data guarantees these; testing them tests the framework  |
| Happy-path CRUD beyond core       | Notice/Config CRUD mirrors App/Version; one set covers pattern  |

---

## Tooling

| Tool              | Purpose                                    |
| ----------------- | ------------------------------------------ |
| JUnit5            | Test runner                                |
| MockK             | Kotlin-native mocking (ports → mock)       |
| SpringMockK       | `@MockkBean` for Spring slice tests        |
| `@WebMvcTest`     | Controller-only context (no DB, no Valkey) |
| Testcontainers    | Real PostgreSQL + Valkey for integration   |
| JaCoCo            | Coverage reporting, CI gate at 80%         |

## CI Integration

```
./gradlew test jacocoTestReport
```

- **Gate**: Build fails if line coverage < 80%
- **Report**: `build/reports/jacoco/test/html/index.html`

---

## References

- **Quick Reference**: `.ai/testing.md`
- **Hexagonal Testing Strategy**: Domain = Unit, Adapter = Integration
