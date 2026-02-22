# Testing Strategy

> Test Pyramid for Hexagonal Architecture | **Last Updated**: 2026-02-22

## Coverage Targets

| Layer           | Target | Type        |
| --------------- | ------ | ----------- |
| Domain Service  | 90%+   | Unit        |
| Domain Model    | 95%+   | Unit        |
| REST Controller | 80%+   | Slice       |
| JPA Adapter     | 70%+   | Integration |
| Valkey Adapter  | 60%+   | Integration |
| **Overall**     | **80%**| Mixed       |

## Test Pyramid Ratio

| Level       | Ratio | Tool                        |
| ----------- | ----- | --------------------------- |
| Unit        | ~70%  | MockK + JUnit5              |
| Slice       | ~10%  | `@WebMvcTest`               |
| Integration | ~15%  | Testcontainers (PG + Valkey)|
| E2E         | ~5%   | `@SpringBootTest` + TC      |

## Do NOT Test

- Mapper field-by-field, DTO getters, Spring Config, JPA base methods

**SSOT**: `docs/llm/policies/testing.md`
