# 0009. Separate JPA entities from the domain model

- **Status:** Accepted
- **Date:** 2026-09-19

## Context

The aggregate has to be stored somewhere. The quickest option is to annotate `Account` with
`@Entity` and let Hibernate manage it directly, which is what most tutorials do.

## Decision

Persistence uses its own entity classes (`AccountEntity`, `LedgerEntryEntity`,
`ProcessedOperationEntity`) and mappers. The domain classes carry no persistence annotations.

## Alternatives considered

- **Annotating the aggregate:** it forces a no-args constructor and mutable fields on a model whose
  whole point is that it cannot be built in an invalid state, puts a framework on the domain's
  classpath, and ties the database schema to the shape of the model — renaming a field becomes a
  migration.

## Consequences

The model stays free to express rules the way the business states them, and the schema is free to
change for database reasons. The cost is real: two shapes and a mapper to keep in step, plus the
discipline of loading the row before saving so JPA keeps the version used for optimistic locking.
For a system this size that cost is small; in a CRUD with no rules it would not be worth paying.
