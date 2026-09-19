package io.github.jesusblazquez.ledger.domain.exception;

/** Thrown when an account that is frozen or closed is asked to move money. */
public class AccountNotOperativeException extends DomainException {

    public AccountNotOperativeException(String iban, String status) {
        super("Account %s cannot operate because it is %s".formatted(iban, status));
    }
}
