package io.github.jesusblazquez.ledger.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** A stored ledger entry. It has no setters: once written, an entry never changes. */
@Entity
@Table(name = "ledger_entries")
class LedgerEntryEntity {

    @Id
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "balance_after", nullable = false)
    private BigDecimal balanceAfter;

    @Column(name = "counterparty_iban")
    private String counterpartyIban;

    @Column
    private String description;

    @Column(name = "operation_id", nullable = false)
    private UUID operationId;

    @Column(name = "occurred_at", nullable = false)
    private Instant occurredAt;

    protected LedgerEntryEntity() {
        // required by JPA
    }

    LedgerEntryEntity(
            UUID id,
            UUID accountId,
            String type,
            BigDecimal amount,
            String currency,
            BigDecimal balanceAfter,
            String counterpartyIban,
            String description,
            UUID operationId,
            Instant occurredAt) {
        this.id = id;
        this.accountId = accountId;
        this.type = type;
        this.amount = amount;
        this.currency = currency;
        this.balanceAfter = balanceAfter;
        this.counterpartyIban = counterpartyIban;
        this.description = description;
        this.operationId = operationId;
        this.occurredAt = occurredAt;
    }

    UUID getId() {
        return id;
    }

    UUID getAccountId() {
        return accountId;
    }

    String getType() {
        return type;
    }

    BigDecimal getAmount() {
        return amount;
    }

    String getCurrency() {
        return currency;
    }

    BigDecimal getBalanceAfter() {
        return balanceAfter;
    }

    String getCounterpartyIban() {
        return counterpartyIban;
    }

    String getDescription() {
        return description;
    }

    UUID getOperationId() {
        return operationId;
    }

    Instant getOccurredAt() {
        return occurredAt;
    }
}
