# GatewayService

> Core orchestration service for client init API | **Last Updated**: 2026-02-23

## Responsibility

Aggregates all domain data into a single `InitResponse` for `GET /api/v1/init`. This is the primary client-facing API called once on app launch.

## Request Parameters

| Param         | Type     | Required | Description              |
| ------------- | -------- | -------- | ------------------------ |
| `app_key`     | String   | Yes      | 22-char app identifier   |
| `platform`    | Platform | Yes      | `IOS`, `ANDROID`, `WEB` |
| `app_version` | String   | Yes      | Current version e.g. `2.1.0` |
| `os_version`  | String   | No       | OS version               |

## Response Structure

```json
{
  "service":     { "active": true, "message": null },
  "update":      { "force": false, "latest_version": "2.2.0", "min_version": "2.0.0", "store_url": "...", "message": null },
  "maintenance": { "active": false, "title": null, "message": null, "start_at": null, "end_at": null },
  "notices":     [{ "id": "...", "title": "...", "display_type": "ONCE", ... }],
  "config":      { "feature_x": true, "ab_group": "B" },
  "legal":       [{ "type": "PRIVACY_POLICY", "title": "...", "url": "..." }]
}
```

## Cache Strategy

| Key pattern                                    | TTL           | Eviction trigger         |
| ---------------------------------------------- | ------------- | ------------------------ |
| `vergate:init:{appKey}:{platform}:{appVersion}` | configurable (default 60s) | Any write to app's data |

- Cache adapter: `ValkeyCacheAdapter` (implements `GatewayCachePort`)
- Fire-and-forget: cache write failures NEVER block the response
- Pattern-based eviction: `vergate:init:{appKey}:*` on any admin write

## Business Logic

1. **App lookup** — find by `appKey`, return 404 (`APP_001`) if not found
2. **Service status** — if `isActive=false` → return early with `service.active=false`
3. **Version check** — compare `app_version` vs `minVersion`/`latestVersion` (semver)
4. **Maintenance** — find active window overlapping `now()`, return most recent
5. **Notices** — find active notices where `startAt <= now() <= endAt`, sort by priority
6. **Remote config** — all active configs, parse value by `valueType`
7. **Legal docs** — return links to `/api/v1/legal/{appKey}/{type}` (not content)

## Version Comparison

Custom semver logic in `GatewayService.isVersionBelow(a, b)`:
- Splits by `.`, compares each component as Int
- Returns true if `a < b`
- `force=true` if `app_version < minVersion`

## Legal Document URLs

```
GET /api/v1/legal/{appKey}                  → list of docs
GET /api/v1/legal/{appKey}/privacy-policy   → rendered HTML
GET /api/v1/legal/{appKey}/terms            → rendered HTML
```

Markdown → HTML conversion via CommonMark. Styled with inline CSS.

## Error Codes

| Code      | HTTP | Condition                   |
| --------- | ---- | --------------------------- |
| `APP_001` | 404  | `appKey` not found          |
| `APP_003` | 403  | App inactive (`isActive=false`) |
| `CMN_001` | 400  | Invalid request parameters  |
