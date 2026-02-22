# Deployment

> Helm + Kubernetes + ESO | **Last Updated**: 2026-02-22

## Helm Chart

Located at `helm/vergate/`. Deploy with:

```bash
helm upgrade --install vergate helm/vergate \
  -f helm/vergate/values-prod.yaml \
  -n vergate --create-namespace
```

## Optional Dependencies

| Component  | Default   | values.yaml key          |
| ---------- | --------- | ------------------------ |
| PostgreSQL | `false`   | `postgresql.enabled`     |
| Valkey     | `false`   | `valkey.enabled`         |

When disabled, credentials are injected via ESO secret (`vergate-secret`).

## Secrets (ESO)

| Env Var             | Source                  |
| ------------------- | ----------------------- |
| `DATABASE_URL`      | ESO → `vergate-secret`  |
| `DATABASE_USERNAME` | ESO → `vergate-secret`  |
| `DATABASE_PASSWORD` | ESO → `vergate-secret`  |
| `REDIS_HOST`        | ESO → `vergate-secret`  |
| `REDIS_PORT`        | ESO → `vergate-secret`  |
| `JWT_SECRET`        | ESO → `vergate-secret`  |

**SSOT**: `docs/llm/policies/deployment.md`
