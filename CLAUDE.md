# CLAUDE.md

> **Claude Entry Point** | Based on [AGENTS.md](AGENTS.md) | **Last Updated**: 2026-02-21

## Standard Policy

**MUST READ**: [AGENTS.md](AGENTS.md) - Multi-LLM Standard Policy (SSOT)

---

## Quick Start

**Start here**: [.ai/README.md](.ai/README.md) - Navigation for all AI documentation

## Essential Reading

**For ANY task, read these first:**

1. **[.ai/rules.md](.ai/rules.md)** - Core DO/DON'T rules (CRITICAL)
2. **[.ai/best-practices.md](.ai/best-practices.md)** - 2026 Best Practices
3. **[.ai/architecture.md](.ai/architecture.md)** - Hexagonal Architecture

## Documentation Policy

### CDD (Context-Driven Development)

| Tier | Path        | Role             | Editable |
| ---- | ----------- | ---------------- | -------- |
| 1    | `.ai/`      | Indicator (≤50)  | YES      |
| 2    | `docs/llm/` | SSOT (≤200)      | YES      |
| 3    | `docs/en/`  | Human-readable   | NO       |
| 4    | `docs/kr/`  | Translation      | NO       |

### SDD (Spec-Driven Development)

| Path      | Role                   | Editable |
| --------- | ---------------------- | -------- |
| `.specs/` | Development blueprints | YES      |

**Edit Rules**: Edit `.ai/` + `docs/llm/` + `.specs/` only. Never edit `docs/en/` or `docs/kr/` directly.

## Key Principles

- **Language**: English only (code, docs, commits)
- **GitFlow**: `feat/* -> develop -> main`
- **Architecture**: Hexagonal (Ports & Adapters)
- **Test Coverage**: 80% minimum

## Policies

| Topic        | Path                                          |
| ------------ | --------------------------------------------- |
| Backend Arch | `docs/llm/policies/hexagonal-architecture.md` |
| CDD          | `docs/llm/policies/cdd.md`                    |
| SDD          | `docs/llm/policies/sdd.md`                    |
| Git Flow     | `docs/llm/git-flow.md`                        |

---

**Start**: [.ai/README.md](.ai/README.md)
