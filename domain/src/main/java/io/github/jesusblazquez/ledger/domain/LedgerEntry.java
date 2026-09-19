package io.github.jesusblazquez.ledger.domain;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

/**
 * One line of the ledger: a movement that already happened.
 *
 * <p>Entries are immutable and append-only. A mistake is never corrected by editing an entry, it is
 * corrected by writing a new one, exactly as double-entry bookkeeping has worked for centuries.
 * Each entry stores the balance that resulted from it, so a statement can be produced without
 * replaying the whole history.
 */
public record LedgerEntry(
        LedgerEntryId id,
        AccountId accountId,
        LedgerEntryType type,
        Money amount,
        Money balanceAfter,
        Iban counterparty,
        String description,
        OperationId operationId,
        Instant occurredAt) {

    public LedgerEntry {
        Objects.requireNonNull(id, "entry id is required");
        Objects.requireNonNull(accountId, "account id is required");
        Objects.requireNonNull(type, "entry type is required");
        Objects.requireNonNull(amount, "amount is required");
        Objects.requireNonNull(balanceAfter, "resulting balance is required");
        Objects.requireNonNull(operationId, "operation id is required");
        Objects.requireNonNull(occurredAt, "time is required");
    }

    public Optional<Iban> counterpartyIban() {
        return Optional.ofNullable(counterparty);
    }
}
