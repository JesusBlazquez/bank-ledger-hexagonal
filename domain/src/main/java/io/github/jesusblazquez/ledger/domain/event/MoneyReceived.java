package io.github.jesusblazquez.ledger.domain.event;

import io.github.jesusblazquez.ledger.domain.AccountId;
import io.github.jesusblazquez.ledger.domain.Iban;
import io.github.jesusblazquez.ledger.domain.Money;
import io.github.jesusblazquez.ledger.domain.OperationId;
import java.time.Instant;

public record MoneyReceived(
        AccountId accountId, Iban origin, Money amount, Money balanceAfter, OperationId operationId, Instant occurredAt)
        implements DomainEvent {}
