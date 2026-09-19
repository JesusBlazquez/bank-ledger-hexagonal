package io.github.jesusblazquez.ledger.persistence;

import io.github.jesusblazquez.ledger.domain.Account;
import io.github.jesusblazquez.ledger.domain.AccountId;
import io.github.jesusblazquez.ledger.domain.AccountStatus;
import io.github.jesusblazquez.ledger.domain.Iban;
import io.github.jesusblazquez.ledger.domain.Money;
import java.util.Currency;

/** Translates between the aggregate and its stored form. The only place that knows both shapes. */
final class AccountMapper {

    private AccountMapper() {}

    static Account toDomain(AccountEntity entity) {
        Currency currency = Currency.getInstance(entity.getCurrency());
        return Account.rehydrate(
                new AccountId(entity.getId()),
                Iban.of(entity.getIban()),
                entity.getHolderName(),
                Money.of(entity.getBalanceAmount(), currency),
                AccountStatus.valueOf(entity.getStatus()),
                Money.of(entity.getDailyTransferLimit(), currency),
                entity.getTransferredOn(),
                Money.of(entity.getTransferredToday(), currency));
    }

    /** Copies the current state of the aggregate onto the entity, keeping its JPA identity. */
    static void copyToEntity(Account account, AccountEntity entity) {
        entity.setIban(account.iban().value());
        entity.setHolderName(account.holderName());
        entity.setCurrency(account.currency().getCurrencyCode());
        entity.setBalanceAmount(account.balance().amount());
        entity.setStatus(account.status().name());
        entity.setDailyTransferLimit(account.dailyTransferLimit().amount());
        entity.setTransferredOn(account.lastTransferDate());
        entity.setTransferredToday(account.transferredToday().amount());
    }
}
