# Deployment

> GitOps via ArgoCD + platform-gitops | **Last Updated**: 2026-02-23

## Environments

| Env  | Domain                  | Namespace            | ArgoCD App         |
| ---- | ----------------------- | -------------------- | ------------------ |
| dev  | `vergate-dev.girok.dev` | `app-shared-gateway` | `app-vergate-dev`  |
| prod | `vergate.girok.dev`     | `app-shared-gateway` | `app-vergate-prod` |

## Deployment Flow

```
push develop/main → CI builds image → tag update in platform-gitops → ArgoCD applies
```

## Secrets (Vault → ESO)

| Vault Path               | K8s Secret           |
| ------------------------ | -------------------- |
| `secret/app/vergate-dev` | `vergate-dev-secret` |
| `secret/app/vergate-prod`| `vergate-prod-secret`|

Keys: `database_url`, `database_username`, `database_password`, `redis_host`, `redis_port`, `redis_password`, `jwt_secret`

**SSOT**: `docs/llm/policies/deployment.md`
