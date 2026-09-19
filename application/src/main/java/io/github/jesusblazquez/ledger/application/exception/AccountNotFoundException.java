package io.github.jesusblazquez.ledger.application.exception;

import io.github.jesusblazquez.ledger.domain.AccountId;
import io.github.jesusblazquez.ledger.domain.Iban;

/** Thrown when an operation refers to an account that does not exist. */
public class AccountNotFoundException extends RuntimeException {

    public AccountNotFoundException(AccountId id) {
        super("No account with id %s".formatted(id));
    }

    public AccountNotFoundException(Iban iban) {
        super("No account with IBAN %s".formatted(iban.masked()));
    }
}
