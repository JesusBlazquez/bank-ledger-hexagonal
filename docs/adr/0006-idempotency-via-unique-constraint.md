# 0006. Idempotency enforced by a unique constraint

- **Status:** Accepted
- **Date:** 2026-09-19

## Context

Clients retry: a user taps twice, a gateway repeats a request after a timeout. A money movement must
not be applied twice because of a retry.

## Decision

Every command accepts an `Idempotency-Key` header. Processed operations are stored in a table whose
primary key is that key, together with a hash of the request and the response that was returned. A
repeated key returns the stored response instead of applying the operation again; the same key sent
with a different payload is rejected as a conflict.

## Alternatives considered

- **Check first, then write, in application code:** two concurrent requests both pass the check
  before either writes. It fails exactly when it matters.
- **Deduplicating by (account, amount, timestamp):** guesses intent, and rejects legitimate repeated
  payments of the same amount.

## Consequences

The database, not the application, is what guarantees uniqueness: a duplicate insert fails and the
stored response is returned. The cost is one extra table and deciding how long those records are
kept.
