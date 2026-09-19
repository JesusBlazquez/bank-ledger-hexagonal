package io.github.jesusblazquez.ledger.domain;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;

/**
 * The context in which a movement happens: which request caused it, when it happened and which
 * business day it is booked on.
 *
 * <p>Time is passed in rather than read from the system clock, so the rules that depend on it — the
 * daily transfer limit above all — can be tested by moving a date instead of waiting for midnight.
 * The business day is explicit because a bank's day does not always match the calendar day of the
 * server.
 */
public record Operation(OperationId id, Instant occurredAt, LocalDate businessDate) {

    public Operation {
        Objects.requireNonNull(id, "operation id is required");
        Objects.requireNonNull(occurredAt, "operation time is required");
        Objects.requireNonNull(businessDate, "business date is required");
    }
}
