# 0008. Integration tests run against PostgreSQL with Testcontainers

- **Status:** Accepted
- **Date:** 2026-09-19

## Context

Integration tests need a database. The common shortcut is H2 in memory, which starts instantly and
needs nothing installed.

## Decision

Integration tests start a real PostgreSQL container with Testcontainers, and Flyway applies the same
migrations that run in production.

## Alternatives considered

- **H2 in PostgreSQL compatibility mode:** fast, but it is a different database. Types, constraints,
  locking behaviour and SQL dialect differ, so tests can pass against H2 and fail in production —
  and, worse, the migrations themselves are never really tested.

## Consequences

Tests exercise the real engine, including the unique constraint that backs idempotency and the
optimistic locking behaviour. The cost is Docker as a prerequisite and a few seconds of container
start-up per run, which is why unit tests carry most of the coverage.
