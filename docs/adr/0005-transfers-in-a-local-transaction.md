# 0005. Transfers in a single local transaction

- **Status:** Accepted
- **Date:** 2026-09-19

## Context

A transfer modifies two aggregates: the source account and the destination account. Strict DDD
recommends changing one aggregate per transaction, so that aggregates remain independent
consistency boundaries and the design stays ready to be distributed.

## Decision

Both accounts live in the same database in this project, so a transfer is applied inside one local
transaction: debit, credit and both ledger entries commit together or not at all. Scope is limited
to accounts held in this system; transfers to external banks are out of scope.

## Alternatives considered

- **Saga with compensating events:** the right answer when the accounts live in different services,
  and the reason the event-driven microservices project exists. Here it would add eventual
  consistency and compensation logic to a problem that does not have them.
- **Two-phase commit:** operationally heavy and rarely used in the systems this project imitates.

## Consequences

The transfer is atomic and easy to reason about. The trade-off is explicit: the day accounts are
split across services this decision has to be revisited, and the replacement is a Saga.
