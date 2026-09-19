package io.github.jesusblazquez.ledger.domain.exception;

/** Thrown when a string does not represent a valid IBAN. */
public class InvalidIbanException extends DomainException {

    public InvalidIbanException(String reason) {
        super("Invalid IBAN: " + reason);
    }
}
