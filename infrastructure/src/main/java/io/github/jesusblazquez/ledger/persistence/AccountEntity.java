package io.github.jesusblazquez.ledger.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * How an account is stored.
 *
 * <p>This is deliberately a different class from the {@code Account} aggregate. The domain must not
 * carry JPA annotations, lazy proxies or a no-args constructor it does not want, and the database
 * schema must be free to change without dragging the model behind it. The price is a mapper; the
 * gain is that neither side blackmails the other.
 */
@Entity
@Table(name = "accounts")
class AccountEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true)
    private String iban;

    @Column(name = "holder_name", nullable = false)
    private String holderName;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "balance_amount", nullable = false)
    private BigDecimal balanceAmount;

    @Column(nullable = false)
    private String status;

    @Column(name = "daily_transfer_limit", nullable = false)
    private BigDecimal dailyTransferLimit;

    @Column(name = "transferred_on")
    private LocalDate transferredOn;

    @Column(name = "transferred_today", nullable = false)
    private BigDecimal transferredToday;

    /** Optimistic locking: two concurrent movements on the same account cannot both win. */
    @Version
    @Column(nullable = false)
    private long version;

    protected AccountEntity() {
        // required by JPA
    }

    AccountEntity(UUID id) {
        this.id = id;
    }

    UUID getId() {
        return id;
    }

    String getIban() {
        return iban;
    }

    void setIban(String iban) {
        this.iban = iban;
    }

    String getHolderName() {
        return holderName;
    }

    void setHolderName(String holderName) {
        this.holderName = holderName;
    }

    String getCurrency() {
        return currency;
    }

    void setCurrency(String currency) {
        this.currency = currency;
    }

    BigDecimal getBalanceAmount() {
        return balanceAmount;
    }

    void setBalanceAmount(BigDecimal balanceAmount) {
        this.balanceAmount = balanceAmount;
    }

    String getStatus() {
        return status;
    }

    void setStatus(String status) {
        this.status = status;
    }

    BigDecimal getDailyTransferLimit() {
        return dailyTransferLimit;
    }

    void setDailyTransferLimit(BigDecimal dailyTransferLimit) {
        this.dailyTransferLimit = dailyTransferLimit;
    }

    LocalDate getTransferredOn() {
        return transferredOn;
    }

    void setTransferredOn(LocalDate transferredOn) {
        this.transferredOn = transferredOn;
    }

    BigDecimal getTransferredToday() {
        return transferredToday;
    }

    void setTransferredToday(BigDecimal transferredToday) {
        this.transferredToday = transferredToday;
    }
}
