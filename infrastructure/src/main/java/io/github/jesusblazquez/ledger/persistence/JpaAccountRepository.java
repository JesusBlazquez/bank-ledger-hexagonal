package io.github.jesusblazquez.ledger.persistence;

import io.github.jesusblazquez.ledger.application.port.out.AccountRepository;
import io.github.jesusblazquez.ledger.domain.Account;
import io.github.jesusblazquez.ledger.domain.AccountId;
import io.github.jesusblazquez.ledger.domain.Iban;
import java.util.Optional;
import org.springframework.stereotype.Repository;

/** Adapter: fulfils the application's {@link AccountRepository} port with JPA. */
@Repository
class JpaAccountRepository implements AccountRepository {

    /** Spanish bank and branch code used for the IBANs this ledger issues. */
    private static final String BANK_AND_BRANCH = "21000418";

    private final AccountJpaRepository accounts;

    JpaAccountRepository(AccountJpaRepository accounts) {
        this.accounts = accounts;
    }

    @Override
    public Optional<Account> findById(AccountId id) {
        return accounts.findById(id.value()).map(AccountMapper::toDomain);
    }

    @Override
    public Optional<Account> findByIban(Iban iban) {
        return accounts.findByIban(iban.value()).map(AccountMapper::toDomain);
    }

    @Override
    public void save(Account account) {
        // Loading the row first keeps JPA's version field, and with it the optimistic locking check.
        AccountEntity entity = accounts.findById(account.id().value())
                .orElseGet(() -> new AccountEntity(account.id().value()));
        AccountMapper.copyToEntity(account, entity);
        accounts.save(entity);
    }

    @Override
    public String nextAccountNumber() {
        return "%s%012d".formatted(BANK_AND_BRANCH, accounts.nextAccountNumber());
    }
}
