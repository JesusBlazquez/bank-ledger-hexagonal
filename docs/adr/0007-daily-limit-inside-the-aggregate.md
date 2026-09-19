# 0007. The daily transfer limit is tracked inside the aggregate

- **Status:** Accepted
- **Date:** 2026-09-19

## Context

The rule "an account cannot transfer more than X per day" needs to know how much has already been
transferred today. That figure could be computed by querying the ledger entries, or carried by the
account itself.

## Decision

`Account` keeps the date and the accumulated amount transferred on that date, and resets the
accumulator when the date changes. The rule is evaluated inside the aggregate, which receives the
current date through a clock port rather than calling `LocalDate.now()` itself.

## Alternatives considered

- **Summing today's entries in the use case:** always consistent with the history, but it puts part
  of a business rule in the application layer and adds a query to every transfer.

## Consequences

The rule can be tested without a database by advancing a fake clock, which is exactly the kind of
test that should be fast. The accumulated value duplicates information that is also in the ledger,
so it must be updated in the same transaction as the transfer, and it can be recomputed if it ever
drifts.
