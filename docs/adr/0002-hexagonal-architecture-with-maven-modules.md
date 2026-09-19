# 0002. Hexagonal architecture enforced by Maven modules

- **Status:** Accepted
- **Date:** 2026-09-19

## Context

Hexagonal architecture is usually implemented as package conventions inside a single module:
`domain`, `application` and `infrastructure` packages side by side. Conventions rely on discipline,
and in practice a framework annotation eventually creeps into the domain because nothing stops it.

## Decision

We will split the project into three Maven modules — `domain`, `application` and `infrastructure` —
where `infrastructure` depends on `application`, `application` depends on `domain`, and `domain`
depends on nothing. Spring and Jakarta are declared only in `infrastructure`.

## Alternatives considered

- **Single module with package conventions:** simpler to set up, but an import from the domain into
  Spring still compiles. The rule only exists while someone is watching.
- **Single module plus an ArchUnit test:** catches the violation, but at test time rather than at
  compile time, and only for the rules someone remembered to write.

## Consequences

The domain cannot reference a framework, because the framework is not on its classpath. Domain
tests need no Spring context and run in milliseconds. The cost is a slightly heavier build and
having to decide, for every new dependency, which module really needs it — which is the point.
