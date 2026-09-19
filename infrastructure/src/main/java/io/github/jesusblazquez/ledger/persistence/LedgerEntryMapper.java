package io.github.jesusblazquez.ledger.persistence;

import io.github.jesusblazquez.ledger.domain.AccountId;
import io.github.jesusblazquez.ledger.domain.Iban;
import io.github.jesusblazquez.ledger.domain.LedgerEntry;
import io.github.jesusblazquez.ledger.domain.LedgerEntryId;
import io.github.jesusblazquez.ledger.domain.LedgerEntryType;
import io.github.jesusblazquez.ledger.domain.Money;
import io.github.jesusblazquez.ledger.domain.OperationId;
import java.util.Currency;

final class LedgerEntryMapper {

    private LedgerEntryMapper() {}

    static LedgerEntryEntity toEntity(LedgerEntry entry) {
        return new LedgerEntryEntity(
                entry.id().value(),
                entry.accountId().value(),
                entry.type().name(),
                entry.amount().amount(),
                entry.amount().currency().getCurrencyCode(),
                entry.balanceAfter().amount(),
                entry.counterpartyIban().map(Iban::value).orElse(null),
                entry.description(),
                entry.operationId().value(),
                entry.occurredAt());
    }

    static LedgerEntry toDomain(LedgerEntryEntity entity) {
        Currency currency = Currency.getInstance(entity.getCurrency());
        return new LedgerEntry(
                new LedgerEntryId(entity.getId()),
                new AccountId(entity.getAccountId()),
                LedgerEntryType.valueOf(entity.getType()),
                Money.of(entity.getAmount(), currency),
                Money.of(entity.getBalanceAfter(), currency),
                entity.getCounterpartyIban() == null ? null : Iban.of(entity.getCounterpartyIban()),
                entity.getDescription(),
                new OperationId(entity.getOperationId()),
                entity.getOccurredAt());
    }
}
