# Deployment Policy

> Helm + Kubernetes + ESO | **Last Updated**: 2026-02-22

## Overview

Vergate is deployed to Kubernetes via Helm. Secrets are injected using External Secrets Operator (ESO). PostgreSQL and Valkey can be deployed as part of the chart or connected externally.

## Helm Chart Structure

```
helm/vergate/
├── Chart.yaml
├── values.yaml          # defaults (postgresql/valkey disabled)
├── values-prod.yaml     # production overrides example
└── templates/
    ├── _helpers.tpl
    ├── deployment.yaml
    ├── service.yaml
    ├── serviceaccount.yaml
    ├── configmap.yaml
    ├── externalsecret.yaml
    ├── ingress.yaml
    ├── hpa.yaml
    ├── postgresql.yaml  # conditional (postgresql.enabled)
    └── valkey.yaml      # conditional (valkey.enabled)
```

## Deployment Commands

```bash
# Install / upgrade
helm upgrade --install vergate helm/vergate \
  -f helm/vergate/values-prod.yaml \
  -n vergate --create-namespace

# Dry-run
helm upgrade --install vergate helm/vergate \
  -f helm/vergate/values-prod.yaml \
  -n vergate --dry-run

# Uninstall
helm uninstall vergate -n vergate
```

## Optional Components

| Component  | `values.yaml` key    | Default | Notes                              |
| ---------- | -------------------- | ------- | ---------------------------------- |
| PostgreSQL | `postgresql.enabled` | `false` | StatefulSet + PVC + ClusterIP svc  |
| Valkey     | `valkey.enabled`     | `false` | StatefulSet + PVC + ClusterIP svc  |

When disabled: credentials must be provided via ESO (`externalSecrets.enabled: true`).

## External Secrets Operator (ESO)

### Required CRD

`ExternalSecret` (`external-secrets.io/v1beta1`) — reads from `SecretStore` / `ClusterSecretStore` and creates a Kubernetes `Secret` named `vergate-secret`.

### Secret Keys

| Kubernetes Secret Key | Spring Boot Env Var | Required When          |
| --------------------- | ------------------- | ---------------------- |
| `DATABASE_URL`        | `DATABASE_URL`      | `postgresql.enabled: false` |
| `DATABASE_USERNAME`   | `DATABASE_USERNAME` | `postgresql.enabled: false` |
| `DATABASE_PASSWORD`   | `DATABASE_PASSWORD` | `postgresql.enabled: false` |
| `REDIS_HOST`          | `REDIS_HOST`        | `valkey.enabled: false`     |
| `REDIS_PORT`          | `REDIS_PORT`        | always                      |
| `JWT_SECRET`          | `JWT_SECRET`        | always                      |

### ESO values.yaml Configuration

```yaml
externalSecrets:
  enabled: true
  refreshInterval: 1h
  secretStoreRef:
    name: cluster-secret-store   # name of your ClusterSecretStore
    kind: ClusterSecretStore
  data:
    - secretKey: DATABASE_URL
      remoteRef:
        key: vergate/production
        property: database_url
    - secretKey: DATABASE_USERNAME
      remoteRef:
        key: vergate/production
        property: database_username
    - secretKey: DATABASE_PASSWORD
      remoteRef:
        key: vergate/production
        property: database_password
    - secretKey: REDIS_HOST
      remoteRef:
        key: vergate/production
        property: redis_host
    - secretKey: REDIS_PORT
      remoteRef:
        key: vergate/production
        property: redis_port
    - secretKey: JWT_SECRET
      remoteRef:
        key: vergate/production
        property: jwt_secret
```

## Application Configuration

The app runs with `SPRING_PROFILES_ACTIVE=prod` which reads:

| Spring property          | Env Var             |
| ------------------------ | ------------------- |
| `spring.datasource.url`  | `DATABASE_URL`      |
| `spring.datasource.username` | `DATABASE_USERNAME` |
| `spring.datasource.password` | `DATABASE_PASSWORD` |
| `spring.data.redis.host` | `REDIS_HOST`        |
| `spring.data.redis.port` | `REDIS_PORT`        |

## Production Checklist

- [ ] `ClusterSecretStore` configured and connected to secret backend
- [ ] All required secrets exist in secret backend
- [ ] `ExternalSecret` synced (`kubectl get externalsecret -n vergate`)
- [ ] Image tag pinned (not `latest`) in `values-prod.yaml`
- [ ] Ingress host and TLS configured
- [ ] HPA enabled with appropriate min/max replicas
- [ ] Resource requests/limits set
- [ ] `postgresql.enabled: false` (use existing cluster DB)
- [ ] `valkey.enabled: false` (use existing cluster Valkey)

## References

- `.ai/deployment.md` — Quick reference
- `helm/vergate/values.yaml` — All configurable values
- `helm/vergate/values-prod.yaml` — Production example
