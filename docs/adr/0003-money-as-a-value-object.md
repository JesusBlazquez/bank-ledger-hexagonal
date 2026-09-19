# 0003. Money as a value object over BigDecimal

- **Status:** Accepted
- **Date:** 2026-09-19

## Context

Money appears everywhere in this domain: balances, amounts, limits. Representing it as a `double`
is wrong — 0.10 has no exact binary representation, and errors accumulate. Representing it as a
plain `BigDecimal` loses the currency, so nothing prevents adding euros to dollars.

## Decision

We will model money as an immutable `Money` value object holding a `BigDecimal` amount and a
`Currency`. It is always scaled to 2 decimals with an explicit rounding mode, arithmetic returns new
instances, and operating on two different currencies throws `CurrencyMismatchException`.

## Alternatives considered

- **`double` or `float`:** unacceptable, rounding errors in financial data.
- **Plain `BigDecimal`:** correct arithmetic, but the currency and the invariants live nowhere.
- **A money library (Joda-Money, JSR-354):** solid, but this is a portfolio project where the point
  is to show that I know *why* the rules exist, and the implementation is small.

## Consequences

Every amount in the system is valid by construction, and currency mistakes fail fast at the domain
boundary instead of silently producing a wrong balance. Persistence stores amount and currency in
separate columns and rebuilds the object when reading.
