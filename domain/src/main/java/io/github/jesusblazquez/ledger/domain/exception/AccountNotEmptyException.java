package io.github.jesusblazquez.ledger.domain.exception;

/** Thrown when closing an account that still holds money. */
public class AccountNotEmptyException extends DomainException {

    public AccountNotEmptyException(String iban, String balance) {
        super("Account %s cannot be closed while it holds %s".formatted(iban, balance));
    }
}
