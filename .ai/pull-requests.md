# AI Guide: Pull Requests

> Pull request conventions for AI assistants | **Last Updated**: 2026-02-21

## PR Title

- **Format**: Conventional Commits: `<type>(<scope>): <subject>`
- **Merge Strategy**: Feature branches squash-merged into `develop`
- **Example**: `feat(gateway): add init endpoint`

## PR Body

- **`## Summary`**: Brief "what" and "why"
- **`## Test plan`**: Checklist for manual verification

## Workflow

1. Analyze existing PRs: `gh pr list --state merged -L 5`
2. Create PR: `gh pr create --base develop`

**SSOT**: `docs/llm/pull-requests.md`
