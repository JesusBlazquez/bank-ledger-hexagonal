package io.github.jesusblazquez.ledger.application.port.in;

import io.github.jesusblazquez.ledger.domain.AccountId;

/** Reads an account. */
public interface GetAccountUseCase {

    AccountView byId(AccountId id);
}
