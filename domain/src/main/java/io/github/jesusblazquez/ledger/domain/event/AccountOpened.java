package io.github.jesusblazquez.ledger.domain.event;

import io.github.jesusblazquez.ledger.domain.AccountId;
import io.github.jesusblazquez.ledger.domain.Iban;
import java.time.Instant;

public record AccountOpened(AccountId accountId, Iban iban, Instant occurredAt) implements DomainEvent {}
