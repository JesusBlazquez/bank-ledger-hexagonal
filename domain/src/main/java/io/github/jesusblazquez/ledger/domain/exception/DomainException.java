package io.github.jesusblazquez.ledger.domain.exception;

/**
 * Base type for every business rule violation.
 *
 * <p>Adapters can map this single type to an HTTP status instead of knowing each subclass, while
 * the domain keeps expressing failures in its own language.
 */
public abstract class DomainException extends RuntimeException {

    protected DomainException(String message) {
        super(message);
    }
}
