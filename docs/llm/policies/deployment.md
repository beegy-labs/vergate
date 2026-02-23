# Deployment Policy

> GitOps via ArgoCD + platform-gitops | **Last Updated**: 2026-02-23

## Overview

Vergate is deployed via **GitOps** (ArgoCD + platform-gitops). Direct `helm install` is NOT used in production. Secrets are injected via External Secrets Operator (ESO) from Vault.

## Deployment Flow

```
vergate repo (develop/main push)
  → CI builds image → pushes to gitea.girok.dev/beegy-labs/vergate
  → CI updates image tag in platform-gitops values file
  → ArgoCD detects diff → applies to cluster
```

## Environments

| Env  | Namespace          | ArgoCD App       | Domain                    | Image Tag  | Replicas |
| ---- | ------------------ | ---------------- | ------------------------- | ---------- | -------- |
| dev  | `app-shared-gateway` | `app-vergate-dev`  | `vergate-dev.girok.dev` | `develop`  | 1        |
| prod | `app-shared-gateway` | `app-vergate-prod` | `vergate.girok.dev`     | `main`     | 2        |

## platform-gitops Files

```
apps/shared/vergate/              ← Helm chart (shared template)
  Chart.yaml
  values.yaml                     ← defaults
  templates/
    deployment.yaml
    service.yaml
    externalsecret.yaml
    httproute.yaml

clusters/home/values/
  vergate-dev-values.yaml         ← dev overrides (image tag, domain, resources)
  vergate-prod-values.yaml        ← prod overrides

clusters/home/applications/
  app-vergate-dev.yaml            ← ArgoCD Application
  app-vergate-prod.yaml           ← ArgoCD Application
```

## HTTPRoute (Cilium Gateway API)

Traffic routing is via Gateway API `HTTPRoute`, **not** Kubernetes `Ingress`.

```yaml
httpRoute:
  enabled: true
  hostname: vergate-dev.girok.dev   # or vergate.girok.dev
  gatewayName: web-gateway
  gatewayNamespace: system-network
```

External path: `vergate[-dev].girok.dev` → Cloudflare Tunnel (proxied) → `web-gateway` → `HTTPRoute` → Service

## Vault Secrets (ESO)

| Vault Path              | Keys                                                                           |
| ----------------------- | ------------------------------------------------------------------------------ |
| `secret/app/vergate-dev`  | `database_url`, `database_username`, `database_password`, `redis_host`, `redis_port`, `redis_password`, `jwt_secret` |
| `secret/app/vergate-prod` | same keys                                                                      |

ESO syncs to Kubernetes Secret named `vergate-{env}-secret` in `app-shared-gateway`.

## Database

| Env  | DB Name        | User           | Host                        |
| ---- | -------------- | -------------- | --------------------------- |
| dev  | `vergate_dev`  | `vergate_dev`  | `db-postgres-001.beegy.net` |
| prod | `vergate_prod` | `vergate_prod` | `db-postgres-001.beegy.net` |

## Application Config

Spring Boot reads from environment variables (injected from ESO Secret):

| Env Var             | Spring Property                     |
| ------------------- | ----------------------------------- |
| `DATABASE_URL`      | `spring.datasource.url`             |
| `DATABASE_USERNAME` | `spring.datasource.username`        |
| `DATABASE_PASSWORD` | `spring.datasource.password`        |
| `REDIS_HOST`        | `spring.data.redis.host`            |
| `REDIS_PORT`        | `spring.data.redis.port`            |
| `REDIS_PASSWORD`    | `spring.data.redis.password`        |
| `JWT_SECRET`        | `app.jwt.secret`                    |

Active profile: `SPRING_PROFILES_ACTIVE=prod`

## DNS (Cloudflare Terraform)

DNS records managed in `bootstrap/terraform/variables.tf` (platform-gitops):

```hcl
"vergate"     = true   # proxied — prod API
"vergate-dev" = true   # proxied — dev API
```

Both CNAMEs point to `tunnel-home.girok.dev` (Cloudflare Tunnel).

## Production Checklist

- [ ] Vault secrets exist at `secret/app/vergate-prod`
- [ ] ExternalSecret synced (`kubectl get externalsecret -n app-shared-gateway`)
- [ ] Image tag pinned in `vergate-prod-values.yaml` (not `latest`)
- [ ] Flyway migrations applied (auto on startup, validate-only in prod)
- [ ] Replicas ≥ 2 for HA
- [ ] HPA configured if traffic spikes expected
- [ ] DNS record live (`dig vergate.girok.dev`)

## References

- `.ai/deployment.md` — Quick reference
- `apps/shared/vergate/` — Helm chart (platform-gitops)
- `clusters/home/values/vergate-*-values.yaml` — Per-env config
- `docs/llm/components/vergate.md` — Component doc (platform-gitops repo)
