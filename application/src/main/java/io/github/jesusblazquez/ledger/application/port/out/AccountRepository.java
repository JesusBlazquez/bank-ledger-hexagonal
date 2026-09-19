package io.github.jesusblazquez.ledger.application.port.out;

import io.github.jesusblazquez.ledger.domain.Account;
import io.github.jesusblazquez.ledger.domain.AccountId;
import io.github.jesusblazquez.ledger.domain.Iban;
import java.util.Optional;

/**
 * Storage for accounts, expressed in the language of the application rather than of a database.
 *
 * <p>Note what is missing: no {@code Page}, no {@code Example}, no JPA type. The application
 * declares what it needs; the infrastructure decides how to provide it.
 */
public interface AccountRepository {

    Optional<Account> findById(AccountId id);

    Optional<Account> findByIban(Iban iban);

    void save(Account account);

    /** Returns the next national account number to issue. */
    String nextAccountNumber();
}
