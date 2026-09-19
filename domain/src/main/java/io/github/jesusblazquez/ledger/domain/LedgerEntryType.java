package io.github.jesusblazquez.ledger.domain;

/** The kind of movement recorded in the ledger. */
public enum LedgerEntryType {
    DEPOSIT,
    WITHDRAWAL,
    TRANSFER_OUT,
    TRANSFER_IN;

    public boolean isCredit() {
        return this == DEPOSIT || this == TRANSFER_IN;
    }
}
