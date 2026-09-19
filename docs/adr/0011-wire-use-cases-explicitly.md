# 0011. Wire the use cases explicitly instead of scanning them

- **Status:** Accepted
- **Date:** 2026-09-19

## Context

The application services could be annotated with `@Service` and picked up by component scanning,
which is the usual Spring arrangement and needs no configuration class.

## Decision

The application module contains no Spring annotations. Its services are plain objects, assembled in
a `@Configuration` class in the infrastructure module, which is also where the `Clock` is defined.

## Alternatives considered

- **`@Service` on each use case:** convenient, but it puts Spring on the application's classpath and
  quietly makes the layer untestable without a container.

## Consequences

The use cases can be constructed in a test with fakes and no Spring at all — which is why the
application tests run in milliseconds. The price is that every dependency is written out by hand,
and mistakes there are real: declaring the same object as three beans produced two beans of the same
type and an ambiguous injection point, which only showed up when the context started.
