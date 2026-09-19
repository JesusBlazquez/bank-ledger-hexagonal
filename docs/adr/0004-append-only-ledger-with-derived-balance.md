# 0004. Append-only ledger with a derived balance

- **Status:** Accepted
- **Date:** 2026-09-19

## Context

The history of movements has to be auditable: in banking, a record is never edited or deleted, it is
corrected with a new entry. At the same time, reading a balance must be fast and must not require
summing every movement since the account was opened.

## Decision

Ledger entries are append-only: once written, a row is never updated or deleted. Each entry stores
the amount, the type, the counterparty, the originating operation and the resulting balance. The
account keeps its current balance as a derived value, which can always be recomputed from the
entries.

## Alternatives considered

- **Event sourcing:** the balance is rebuilt from the event stream every time, giving a complete
  history by construction. It is heavier (snapshots, projections, versioned events) and further from
  what Spanish banking teams actually run today.
- **Mutable balance with no history:** trivial to implement and impossible to audit.

## Consequences

Reads are cheap and the history is trustworthy, at the cost of one redundancy: the balance is stored
in two forms. A reconciliation check that recomputes balances from entries is therefore worth
having, and it is the kind of job that runs as a batch process.
