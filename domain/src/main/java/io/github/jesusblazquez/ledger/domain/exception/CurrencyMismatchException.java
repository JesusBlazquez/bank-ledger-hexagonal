package io.github.jesusblazquez.ledger.domain.exception;

import java.util.Currency;

/** Thrown when two amounts in different currencies are combined or compared. */
public class CurrencyMismatchException extends DomainException {

    public CurrencyMismatchException(Currency expected, Currency actual) {
        super("Expected an amount in %s but got %s".formatted(expected.getCurrencyCode(), actual.getCurrencyCode()));
    }
}
