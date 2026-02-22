# Git Flow Policy

> SSOT for branching and merge strategy | **Last Updated**: 2026-02-21

## Overview

Vergate follows **GitFlow** branching model with squash merges for feature branches.

## Branch Model

```
feat/* ──squash──▶ develop ──merge──▶ main
  fix/*               (Dev)              (Prod)
  refactor/*
  chore/*
```

## Branch Naming

| Type     | Pattern              | Example                    |
| -------- | -------------------- | -------------------------- |
| Feature  | `feat/{description}` | `feat/add-gateway-init`    |
| Bug Fix  | `fix/{description}`  | `fix/version-comparison`   |
| Refactor | `refactor/{desc}`    | `refactor/cache-layer`     |
| Chore    | `chore/{description}`| `chore/update-deps`        |
| Docs     | `docs/{description}` | `docs/api-documentation`   |

## Merge Strategy

| Source -> Target | Type   | Rationale                              |
| ---------------- | ------ | -------------------------------------- |
| feat -> develop  | Squash | Clean history, one commit per feature  |
| fix -> develop   | Squash | Clean history, one commit per fix      |
| develop -> main  | Merge  | Preserve develop history in production |

## Commit Convention

```
<type>(<scope>): <subject>

[optional body]

[optional footer]
```

### Types

| Type       | Description                 |
| ---------- | --------------------------- |
| `feat`     | New feature                 |
| `fix`      | Bug fix                     |
| `refactor` | Code refactoring            |
| `docs`     | Documentation only          |
| `test`     | Adding/updating tests       |
| `chore`    | Build, CI, deps, etc.       |

### Scopes (Vergate-specific)

| Scope         | Description            |
| ------------- | ---------------------- |
| `gateway`     | Client init API        |
| `app`         | App management         |
| `version`     | Version management     |
| `maintenance` | Maintenance management |
| `notice`      | Notice management      |
| `config`      | Remote config          |
| `auth`        | Authentication/JWT     |
| `infra`       | Docker, K8s, CI/CD     |

### Examples

```
feat(gateway): add init endpoint with Redis cache
fix(version): correct semver comparison logic
refactor(config): extract cache adapter to separate class
chore(infra): add docker-compose for local dev
```

## Feature Workflow

```bash
# 1. Create feature branch from develop
git checkout develop
git pull origin develop
git checkout -b feat/add-gateway-init

# 2. Develop with conventional commits
git commit -m "feat(gateway): add init endpoint"

# 3. Push and create PR targeting develop
git push -u origin feat/add-gateway-init
gh pr create --base develop

# 4. After review, squash merge
gh pr merge --squash --delete-branch
```

## Release Workflow

```bash
# Merge develop into main for production release
gh pr create --base main --head develop --title "release: v1.0.0"
gh pr merge --merge
git tag v1.0.0
git push --tags
```

## Protected Branches

| Branch    | Protection                              |
| --------- | --------------------------------------- |
| `main`    | No direct push, PR required, CI must pass |
| `develop` | No direct push, PR required, CI must pass |

## Rules

| Rule                         | Description                              |
| ---------------------------- | ---------------------------------------- |
| No direct push to protected  | Always use PRs                           |
| Squash on feature merge      | Keep develop history clean               |
| Delete branch after merge    | Prevent branch clutter                   |
| English only                 | All commit messages and PR titles        |
| Conventional commits         | Required for all commits                 |

## References

- **Quick Reference**: `.ai/git-flow.md`
- **PR Conventions**: `.ai/pull-requests.md`
