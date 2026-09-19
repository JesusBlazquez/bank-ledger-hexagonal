package io.github.jesusblazquez.ledger.domain.event;

import java.time.Instant;

/**
 * Something that happened in the domain, stated in the past tense.
 *
 * <p>A sealed interface lets the compiler check that every consumer handles all the cases: adding a
 * new event without handling it breaks the build instead of failing silently at runtime.
 */
public sealed interface DomainEvent permits AccountOpened, MoneyDeposited, MoneyWithdrawn, MoneySent, MoneyReceived {

    Instant occurredAt();
}
