# Core Development Rules

> Essential rules for AI assistants | **Last Updated**: 2026-02-21

## Language Policy

**ALL code, documentation, and commits MUST be in English.**

## Documentation Policy (4-Tier)

| Tier | Path        | LLM Editable | Purpose                |
| ---- | ----------- | ------------ | ---------------------- |
| 1    | `.ai/`      | **YES**      | Pointer (50 lines)     |
| 2    | `docs/llm/` | **YES**      | SSOT (token-optimized) |
| 3    | `docs/en/`  | **NO**       | Generated              |
| 4    | `docs/kr/`  | **NO**       | Translated             |

## NEVER / ALWAYS

| NEVER                        | Alternative                    |
| ---------------------------- | ------------------------------ |
| Domain importing Spring/JPA  | Domain = pure Kotlin           |
| Controller calling Repository| Use Service layer              |
| Hardcode secrets             | Use application.yml + env vars |
| Manual SQL in code           | Use Flyway migrations          |
| `@Entity` in DTOs            | Separate DTO from Entity       |
| Skip validation              | Use `@Valid` on request DTOs   |

| ALWAYS                  | Details                              |
| ----------------------- | ------------------------------------ |
| Hexagonal Architecture  | Ports & Adapters pattern             |
| `@Transactional`        | For multi-step DB operations         |
| 80% test coverage       | CI blocks below threshold            |
| Include tests           | With all code changes                |
| Flyway migrations       | Never modify existing migrations     |
| English only            | Code, docs, commits, PR titles       |

## Stack (2026-02)

| Category  | Technology                                    |
| --------- | --------------------------------------------- |
| Language  | Kotlin                                        |
| Framework | Spring Boot 3.x                               |
| Build     | Gradle (Kotlin DSL)                            |
| Database  | PostgreSQL + Spring Data JPA                   |
| Cache     | Redis (Valkey compatible)                      |
| Migration | Flyway                                         |
| Auth      | Spring Security + JWT                          |
| API Docs  | SpringDoc OpenAPI (Swagger)                    |
| Test      | JUnit5 + MockK + Testcontainers               |
| Deploy    | Helm + Kubernetes (ArgoCD + ESO)              |
| ID        | UUIDv7 (all primary keys, app-generated)      |

**SSOT**: `docs/llm/rules.md`
