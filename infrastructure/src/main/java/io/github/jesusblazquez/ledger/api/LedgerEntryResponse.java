package io.github.jesusblazquez.ledger.api;

import io.github.jesusblazquez.ledger.domain.Iban;
import io.github.jesusblazquez.ledger.domain.LedgerEntry;
import java.time.Instant;

public record LedgerEntryResponse(
        String id,
        String type,
        MoneyDto amount,
        MoneyDto balanceAfter,
        String counterpartyIban,
        String description,
        Instant occurredAt) {

    static LedgerEntryResponse of(LedgerEntry entry) {
        return new LedgerEntryResponse(
                entry.id().toString(),
                entry.type().name(),
                MoneyDto.of(entry.amount()),
                MoneyDto.of(entry.balanceAfter()),
                entry.counterpartyIban().map(Iban::value).orElse(null),
                entry.description(),
                entry.occurredAt());
    }
}
