# 0010. Return errors as Problem Details (RFC 9457)

- **Status:** Accepted
- **Date:** 2026-09-19

## Context

A client needs to tell apart "this account has no money", "this account does not exist" and "your
request was malformed", and it has to do so without parsing English sentences.

## Decision

Every failure is answered with a Problem Details document: a stable `type` URI, a short `title`, the
`detail` produced by the domain, and the right status code. Business conflicts (insufficient
balance, daily limit, idempotency key reused, account frozen) are 409; invalid values are 422;
malformed bodies are 400; unknown accounts are 404.

## Alternatives considered

- **A custom error envelope:** one more format for clients to learn, with nothing gained.
- **Plain text or bare status codes:** loses the distinction between different conflicts.

## Consequences

Callers can branch on `type` and keep working when the wording changes, and Spring produces the
format natively. The mapping lives in a single `@RestControllerAdvice`, so no controller contains a
try/catch and the domain exceptions stay free of HTTP concepts.
