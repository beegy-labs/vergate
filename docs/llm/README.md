# docs/llm Index

> CDD Tier 2 - LLM Detailed Information (SSOT) | Multi-LLM Compatible

## Tier 2 Rules

| Rule           | Value     | Description                       |
| -------------- | --------- | --------------------------------- |
| Max Lines      | ≤300      | Split file if exceeded            |
| Weekly Cleanup | Once/week | Remove legacy, verify reliability |

## Tier 2 Role Definition

| Path          | Role               | When to Read                            |
| ------------- | ------------------ | --------------------------------------- |
| **policies/** | Project-wide rules | First read or policy-related tasks      |
| **services/** | Service SSOT       | When working on that service (required) |
| **guides/**   | How-to guides      | When implementing specific features     |

## Task Reference Mapping

| Task Type        | Required Files                        |
| ---------------- | ------------------------------------- |
| **Backend Arch** | `policies/hexagonal-architecture.md`  |
| **DB migration** | `policies/database.md`                |
| **Testing**      | `policies/testing.md`                 |
| **Deployment**   | `policies/deployment.md`              |

## Policies

| File                          | Topic        | Source              |
| ----------------------------- | ------------ | ------------------- |
| cdd.md                        | CDD policy   | symlink (submodule) |
| sdd.md                        | SDD policy   | symlink (submodule) |
| add.md                        | ADD policy   | symlink (submodule) |
| hexagonal-architecture.md     | Backend arch | project-specific    |
| testing.md                    | Test strategy| project-specific    |
| deployment.md                 | Helm + ESO   | project-specific    |

## Entry Points

| Task         | File                                 |
| ------------ | ------------------------------------ |
| Rules        | `policies/cdd.md`                    |
| Backend Arch | `policies/hexagonal-architecture.md` |
| Git flow     | `.ai/git-flow.md`                    |
| Testing      | `policies/testing.md`                |
| Deployment   | `policies/deployment.md`             |
