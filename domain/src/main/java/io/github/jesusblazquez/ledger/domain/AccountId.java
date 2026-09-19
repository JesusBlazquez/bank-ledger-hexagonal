package io.github.jesusblazquez.ledger.domain;

import java.util.Objects;
import java.util.UUID;

/** Identity of an account. A typed id cannot be passed where a different id is expected. */
public record AccountId(UUID value) {

    public AccountId {
        Objects.requireNonNull(value, "account id is required");
    }

    public static AccountId newId() {
        return new AccountId(UUID.randomUUID());
    }

    public static AccountId of(String value) {
        return new AccountId(UUID.fromString(value));
    }

    @Override
    public String toString() {
        return value.toString();
    }
}
