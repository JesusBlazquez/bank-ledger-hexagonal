package io.github.jesusblazquez.ledger.domain;

import java.util.Objects;
import java.util.UUID;

/** Identity of a ledger entry. */
public record LedgerEntryId(UUID value) {

    public LedgerEntryId {
        Objects.requireNonNull(value, "ledger entry id is required");
    }

    public static LedgerEntryId newId() {
        return new LedgerEntryId(UUID.randomUUID());
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
