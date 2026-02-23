<!-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ -->
<!-- BEGIN: STANDARD POLICY (Auto-synced from agentic-dev-protocol)    -->
<!-- DO NOT EDIT this section manually in projects - changes will be    -->
<!-- overwritten. Edit in agentic-dev-protocol and sync to all projects.-->
<!-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ -->

> **Multi-LLM Standard Policy** | **Version**: 1.0.0 | **Last Updated**: 2026-02-21

## Purpose

This file defines the **mandatory standard policy** for all LLM agents across all projects using agentic-dev-protocol.

Each project MUST have agent-specific entry files (CLAUDE.md, GEMINI.md, etc.) that:

1. Import and follow this standard policy
2. Add agent-specific optimizations
3. Reference project-specific CDD (`.ai/`, `docs/llm/`)

---

## Development Methodology

**Full Documentation**: `docs/llm/policies/development-methodology.md`

### CDD (Context-Driven Development)

| Tier | Path        | Role                  | Editable | Max Lines |
| ---- | ----------- | --------------------- | -------- | --------- |
| 1    | `.ai/`      | Indicator (LLM)       | YES      | ≤50       |
| 2    | `docs/llm/` | SSOT (LLM)            | YES      | ≤200      |
| 3    | `docs/en/`  | Human-readable (Auto) | NO       | N/A       |
| 4    | `docs/kr/`  | Translation (Auto)    | NO       | N/A       |

**Policy**: `docs/llm/policies/cdd.md`

### SDD (Spec-Driven Development)

**3-Layer Structure: WHAT -> WHEN -> HOW**

| Layer | Path                 | Role                 | Human Role       |
| ----- | -------------------- | -------------------- | ---------------- |
| L1    | `.specs/roadmap.md`  | WHAT (features)      | Design & Plan    |
| L2    | `.specs/scopes/*.md` | WHEN (work range)    | Define scope     |
| L3    | `.specs/tasks/*.md`  | HOW (implementation) | Review & Approve |

**Policy**: `docs/llm/policies/sdd.md`

---

## CDD Rules

| Rule              | Description                                           |
| ----------------- | ----------------------------------------------------- |
| Tier 1 Max        | ≤50 lines per file                                    |
| Tier 2 Max        | ≤200 lines per file (split if exceeded)               |
| Weekly Cleanup    | Once/week - remove legacy, verify reliability         |
| Edit Permission   | LLM only (Human NEVER edits `.ai/` or `docs/llm/`)    |
| Post-Task         | Update CDD after task completion                      |
| Reference Mapping | Check `docs/llm/README.md` for task reference mapping |

## Essential Reading (All Tasks)

| Priority | File                    | Purpose             |
| -------- | ----------------------- | ------------------- |
| 1        | `.ai/rules.md`          | Core DO/DON'T rules |
| 2        | `.ai/best-practices.md` | Current guidelines  |
| 3        | `.ai/architecture.md`   | System patterns     |

## Key Principles

| Principle         | Description                                   |
| ----------------- | --------------------------------------------- |
| Language          | English only (code, docs, commits)            |
| No Human Doc Edit | LLM handles all `.ai/` and `docs/llm/` edits  |
| Token Efficiency  | Tables > prose, YAML > JSON                   |
| Cross-Reference   | `.ai/` always links to `docs/llm/`            |
| Git = CDD History | Use Git for document history, not manual logs |

---

## Directory Structure (Mandatory)

```
project/
├── AGENTS.md           # This file (synced from agentic-dev-protocol)
├── CLAUDE.md           # Agent entry (project-specific)
│
├── .ai/                # CDD Tier 1 - EDITABLE
│   ├── README.md       # Navigation hub
│   ├── rules.md        # Core DO/DON'T
│   ├── architecture.md # System patterns
│   └── best-practices.md
│
├── .specs/             # SDD - DEVELOPMENT BLUEPRINTS
│   ├── roadmap.md      # L1: WHAT to build
│   ├── scopes/         # L2: WHEN to build
│   ├── tasks/          # L3: HOW to build
│   └── history/        # Archived
│
├── docs/
│   ├── llm/            # CDD Tier 2 - EDITABLE (SSOT)
│   │   ├── README.md
│   │   ├── policies/
│   │   ├── services/
│   │   └── guides/
│   ├── en/             # CDD Tier 3 - GENERATED (DO NOT EDIT)
│   └── kr/             # CDD Tier 4 - TRANSLATED (DO NOT EDIT)
```

<!-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ -->
<!-- END: STANDARD POLICY                                               -->
<!-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ -->

---

<!-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ -->
<!-- BEGIN: PROJECT CUSTOM (Safe to edit in project repositories)      -->
<!-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ -->

## Project-Specific Configuration (Vergate)

> **Last Updated**: 2026-02-23

### Architecture & Stack

| Layer         | Tech                                              |
| ------------- | ------------------------------------------------- |
| **Language**  | Kotlin 2.1                                        |
| **Framework** | Spring Boot 3.4                                   |
| **Build**     | Gradle (Kotlin DSL)                                |
| **Database**  | PostgreSQL + Spring Data JPA                       |
| **Cache**     | Valkey (Redis-compatible)                          |
| **Migration** | Flyway                                             |
| **Auth**      | Spring Security + JWT (JJWT)                      |
| **API Docs**  | SpringDoc OpenAPI (Swagger)                        |
| **Markdown**  | CommonMark (legal document rendering)             |
| **Test**      | JUnit5 + MockK + Testcontainers                   |
| **Deploy**    | GitOps (ArgoCD + platform-gitops + ESO)           |

### Backend Architecture

All backend code uses **Hexagonal Architecture** (Ports & Adapters):

| Layer    | Role                    | Directory                         |
| -------- | ----------------------- | --------------------------------- |
| Domain   | Business logic (pure Kotlin) | `domain/model/`, `domain/service/` |
| Ports    | Output interfaces       | `ports/out/`                      |
| Adapters | Infrastructure impl     | `adapters/in/`, `adapters/out/`   |

> No `ports/in/` — domain services ARE the use cases directly.

**Full Policy**: `docs/llm/policies/hexagonal-architecture.md`

### Project-Specific Rules

| Principle         | Description                          |
| ----------------- | ------------------------------------ |
| Language          | English only (code, docs, commits)   |
| GitFlow           | `feat/* -> develop -> main`          |
| Test Coverage     | 80% minimum (JaCoCo gate)            |
| Transaction       | `@Transactional` for multi-step DB   |
| No Human Doc Edit | LLM handles all documentation        |

### Essential Reading

| Priority | File                    |
| -------- | ----------------------- |
| 1        | `.ai/rules.md`          |
| 2        | `.ai/best-practices.md` |
| 3        | `.ai/architecture.md`   |

<!-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ -->
<!-- END: PROJECT CUSTOM                                                -->
<!-- ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━ -->
