package io.github.jesusblazquez.ledger.domain;

import java.util.Objects;
import java.util.UUID;

/**
 * Identity of the request that caused a movement.
 *
 * <p>It ties every ledger entry back to the client request that produced it, which is what makes
 * the idempotency guarantee visible in the history: the same operation id can only appear once.
 */
public record OperationId(UUID value) {

    public OperationId {
        Objects.requireNonNull(value, "operation id is required");
    }

    public static OperationId newId() {
        return new OperationId(UUID.randomUUID());
    }

    public static OperationId of(String value) {
        return new OperationId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
