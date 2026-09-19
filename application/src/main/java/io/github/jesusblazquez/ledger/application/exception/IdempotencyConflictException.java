package io.github.jesusblazquez.ledger.application.exception;

import io.github.jesusblazquez.ledger.domain.OperationId;

/**
 * Thrown when an idempotency key is reused with a different request.
 *
 * <p>Replaying the stored answer would be wrong — the caller asked for something else — so the
 * request is rejected instead.
 */
public class IdempotencyConflictException extends RuntimeException {

    public IdempotencyConflictException(OperationId operationId) {
        super("Operation %s was already used for a different request".formatted(operationId));
    }
}
