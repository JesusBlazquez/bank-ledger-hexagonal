package io.github.jesusblazquez.ledger.domain.exception;

/** Thrown when an account does not hold enough money for a withdrawal or a transfer. */
public class InsufficientBalanceException extends DomainException {

    public InsufficientBalanceException(String iban) {
        super("Account %s does not have enough balance for this operation".formatted(iban));
    }
}
