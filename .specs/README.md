# Specs Navigation

> SDD - Development Blueprints | **Last Updated**: 2026-02-22

## Core Concept

Transform roadmap into executable plans through staged automation.

## 3-Layer Structure (WHAT → WHEN → HOW)

| Layer | Path                          | Role                 | Human Role       |
| ----- | ----------------------------- | -------------------- | ---------------- |
| L1    | `vergate/roadmap.md`          | WHAT (features)      | Design & Plan    |
| L2    | `vergate/scopes/{scope}.md`   | WHEN (work range)    | Define scope     |
| L3    | `vergate/tasks/{scope}.md`    | HOW (implementation) | Review & Approve |
| Done  | `vergate/history/`            | Archived scopes      | Reference        |

## Token Load Strategy

| Situation  | Load                          |
| ---------- | ----------------------------- |
| Planning   | README + roadmap              |
| Work Start | README + roadmap + scope      |
| Continue   | scope + tasks                 |
| Review     | scope + tasks + history       |

## Specs Index

| Project  | Status |
| -------- | ------ |
| vergate  | Active |

## Active Scopes

| Scope                       | Status | Description                            |
| --------------------------- | ------ | -------------------------------------- |
| 2026-q1-mvp-gateway.md      | Active | MVP: version check, maintenance, shutdown |
