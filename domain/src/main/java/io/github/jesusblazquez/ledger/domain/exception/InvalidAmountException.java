package io.github.jesusblazquez.ledger.domain.exception;

/** Thrown when an operation is attempted with a zero or negative amount. */
public class InvalidAmountException extends DomainException {

    public InvalidAmountException(String operation) {
        super("The amount of a %s must be greater than zero".formatted(operation));
    }
}
