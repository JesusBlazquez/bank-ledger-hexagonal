package io.github.jesusblazquez.ledger.application.port.in;

import io.github.jesusblazquez.ledger.domain.LedgerEntry;
import io.github.jesusblazquez.ledger.domain.LedgerEntryId;
import io.github.jesusblazquez.ledger.domain.Money;

/** The outcome of a movement: which entry was written and how the balance ended up. */
public record MovementResult(LedgerEntryId entryId, Money balance, boolean replayed) {

    public static MovementResult applied(LedgerEntry entry) {
        return new MovementResult(entry.id(), entry.balanceAfter(), false);
    }

    public static MovementResult replayed(LedgerEntry entry) {
        return new MovementResult(entry.id(), entry.balanceAfter(), true);
    }
}
