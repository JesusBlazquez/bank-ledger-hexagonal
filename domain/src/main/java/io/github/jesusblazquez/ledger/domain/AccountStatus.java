package io.github.jesusblazquez.ledger.domain;

/** Lifecycle of an account. Only an open account can move money. */
public enum AccountStatus {
    OPEN,
    FROZEN,
    CLOSED;

    public boolean isOperative() {
        return this == OPEN;
    }
}
