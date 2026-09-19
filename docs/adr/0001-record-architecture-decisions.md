# 0001. Record architecture decisions

- **Status:** Accepted
- **Date:** 2026-09-19

## Context

Design decisions in this project should be traceable: anyone reading the code
(including a future me) should be able to find out *why* something was built
the way it was, not only *what* was built.

## Decision

We will keep Architecture Decision Records in `docs/adr/`, one Markdown file
per significant decision, numbered sequentially and never deleted. A decision
that is replaced is marked as *Superseded* and links to its replacement.

## Alternatives considered

- **Comments in the code:** too scattered and they do not capture the options that were rejected.
- **A wiki:** it lives apart from the code and drifts out of date.

## Consequences

Each important decision costs a few minutes of writing, and in return the
README can summarise decisions and link here for the details.
