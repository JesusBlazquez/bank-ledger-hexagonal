package io.github.jesusblazquez.ledger.application.fake;

import io.github.jesusblazquez.ledger.application.port.out.AccountRepository;
import io.github.jesusblazquez.ledger.domain.Account;
import io.github.jesusblazquez.ledger.domain.AccountId;
import io.github.jesusblazquez.ledger.domain.Iban;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

/**
 * A hand-written fake instead of a mock.
 *
 * <p>Fakes behave like the real thing, so the tests read as scenarios ("deposit, then check the
 * balance") rather than as a list of expected calls. They also survive refactoring: changing how
 * many times the service calls save() does not break a single test.
 */
public class InMemoryAccountRepository implements AccountRepository {

    private final Map<AccountId, Account> accounts = new HashMap<>();
    private final AtomicLong accountNumbers = new AtomicLong(1);

    @Override
    public Optional<Account> findById(AccountId id) {
        return Optional.ofNullable(accounts.get(id));
    }

    @Override
    public Optional<Account> findByIban(Iban iban) {
        return accounts.values().stream()
                .filter(account -> account.iban().equals(iban))
                .findFirst();
    }

    @Override
    public void save(Account account) {
        accounts.put(account.id(), account);
    }

    @Override
    public String nextAccountNumber() {
        return "2100%016d".formatted(accountNumbers.getAndIncrement());
    }

    public int size() {
        return accounts.size();
    }
}
