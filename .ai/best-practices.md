# 2026 Best Practices

> Monthly review checklist | **Last Updated**: 2026-02-21

| Category       | Do                                            | Don't                                        |
| -------------- | --------------------------------------------- | -------------------------------------------- |
| **Database**   | Flyway, TIMESTAMPTZ, BIGSERIAL                | Manual DDL, TEXT IDs                         |
| **Kotlin**     | Data classes, sealed classes, null safety      | Java-style code, `!!` abuse                  |
| **Spring**     | Constructor injection, `@Valid`                | Field injection, skipping validation          |
| **Backend**    | Hexagonal, `@Transactional`, DTO mapping      | Layered spaghetti, entity as response        |
| **Testing**    | 80% overall, Domain 90%+, Test Pyramid        | Skip tests, test framework code, E2E bloat   |
| **Git**        | Squash feat->develop, Merge develop->main     | Merge on features, direct push to protected  |
| **Cache**      | Redis TTL, cache invalidation on write        | Cache without TTL, stale data                |
| **Security**   | JWT validation, input sanitization            | Trust client input, expose internal errors   |

## Anti-Patterns

```
❌ Over-engineering
❌ Domain depending on infrastructure
❌ Business logic in controllers
❌ Skipping port layer (controller -> repository)
❌ Features beyond requirements
```

**Testing SSOT**: `docs/llm/policies/testing.md`
**SSOT**: `docs/llm/best-practices.md`
