package io.github.jesusblazquez.ledger.domain;

import io.github.jesusblazquez.ledger.domain.event.AccountOpened;
import io.github.jesusblazquez.ledger.domain.event.DomainEvent;
import io.github.jesusblazquez.ledger.domain.event.MoneyDeposited;
import io.github.jesusblazquez.ledger.domain.event.MoneyReceived;
import io.github.jesusblazquez.ledger.domain.event.MoneySent;
import io.github.jesusblazquez.ledger.domain.event.MoneyWithdrawn;
import io.github.jesusblazquez.ledger.domain.exception.AccountNotEmptyException;
import io.github.jesusblazquez.ledger.domain.exception.AccountNotOperativeException;
import io.github.jesusblazquez.ledger.domain.exception.CurrencyMismatchException;
import io.github.jesusblazquez.ledger.domain.exception.DailyLimitExceededException;
import io.github.jesusblazquez.ledger.domain.exception.InsufficientBalanceException;
import io.github.jesusblazquez.ledger.domain.exception.InvalidAmountException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

/**
 * An account and the rules that protect it.
 *
 * <p>This is the aggregate root: every movement goes through one of its methods, so the invariants
 * below cannot be bypassed by writing a new controller or a new query.
 *
 * <ul>
 *   <li>the balance never goes negative — there is no overdraft here
 *   <li>amounts are always greater than zero
 *   <li>the currency of an operation always matches the currency of the account
 *   <li>a frozen or closed account does not move money
 *   <li>outgoing transfers stay within the limit for the business day
 * </ul>
 *
 * <p>Every method returns the {@link LedgerEntry} it produced — the movement that has to be
 * appended to the history — and records a domain event describing what happened.
 */
public class Account {

    private final AccountId id;
    private final Iban iban;
    private final String holderName;
    private final Currency currency;
    private final Money dailyTransferLimit;

    private Money balance;
    private AccountStatus status;
    private LocalDate transferredOn;
    private Money transferredToday;

    private final List<DomainEvent> events = new ArrayList<>();

    private Account(
            AccountId id,
            Iban iban,
            String holderName,
            Currency currency,
            Money balance,
            AccountStatus status,
            Money dailyTransferLimit,
            LocalDate transferredOn,
            Money transferredToday) {
        this.id = Objects.requireNonNull(id, "account id is required");
        this.iban = Objects.requireNonNull(iban, "IBAN is required");
        this.holderName = requireHolderName(holderName);
        this.currency = Objects.requireNonNull(currency, "currency is required");
        this.balance = Objects.requireNonNull(balance, "balance is required");
        this.status = Objects.requireNonNull(status, "status is required");
        this.dailyTransferLimit = Objects.requireNonNull(dailyTransferLimit, "daily transfer limit is required");
        this.transferredOn = transferredOn;
        this.transferredToday = Objects.requireNonNull(transferredToday, "amount transferred today is required");
    }

    /** Opens a new account with a zero balance. */
    public static Account open(
            AccountId id,
            Iban iban,
            String holderName,
            Currency currency,
            Money dailyTransferLimit,
            Operation operation) {
        Objects.requireNonNull(operation, "operation is required");
        if (!dailyTransferLimit.currency().equals(currency)) {
            throw new CurrencyMismatchException(currency, dailyTransferLimit.currency());
        }
        if (dailyTransferLimit.isNegative()) {
            throw new InvalidAmountException("daily transfer limit");
        }

        Account account = new Account(
                id,
                iban,
                holderName,
                currency,
                Money.zero(currency),
                AccountStatus.OPEN,
                dailyTransferLimit,
                null,
                Money.zero(currency));
        account.events.add(new AccountOpened(id, iban, operation.occurredAt()));
        return account;
    }

    /** Rebuilds an account from stored state. Used by persistence adapters; records no events. */
    public static Account rehydrate(
            AccountId id,
            Iban iban,
            String holderName,
            Money balance,
            AccountStatus status,
            Money dailyTransferLimit,
            LocalDate transferredOn,
            Money transferredToday) {
        return new Account(
                id,
                iban,
                holderName,
                balance.currency(),
                balance,
                status,
                dailyTransferLimit,
                transferredOn,
                transferredToday);
    }

    public LedgerEntry deposit(Money amount, Operation operation) {
        requireOperative();
        requireValidAmount(amount, "deposit");

        balance = balance.add(amount);
        events.add(new MoneyDeposited(id, amount, balance, operation.id(), operation.occurredAt()));
        return entry(LedgerEntryType.DEPOSIT, amount, null, "Deposit", operation);
    }

    public LedgerEntry withdraw(Money amount, Operation operation) {
        requireOperative();
        requireValidAmount(amount, "withdrawal");
        requireEnoughBalance(amount);

        balance = balance.subtract(amount);
        events.add(new MoneyWithdrawn(id, amount, balance, operation.id(), operation.occurredAt()));
        return entry(LedgerEntryType.WITHDRAWAL, amount, null, "Withdrawal", operation);
    }

    /** Debits this account as the source of a transfer, respecting the limit for the business day. */
    public LedgerEntry sendTransfer(Money amount, Iban destination, Operation operation) {
        requireOperative();
        requireValidAmount(amount, "transfer");
        requireEnoughBalance(amount);
        Objects.requireNonNull(destination, "destination IBAN is required");

        Money transferredAfterThisOne = transferredOn(operation.businessDate()).add(amount);
        if (transferredAfterThisOne.isGreaterThan(dailyTransferLimit)) {
            throw new DailyLimitExceededException(
                    iban.masked(),
                    dailyTransferLimit.toString(),
                    transferredOn(operation.businessDate()).toString());
        }

        balance = balance.subtract(amount);
        transferredOn = operation.businessDate();
        transferredToday = transferredAfterThisOne;
        events.add(new MoneySent(id, destination, amount, balance, operation.id(), operation.occurredAt()));
        return entry(
                LedgerEntryType.TRANSFER_OUT, amount, destination, "Transfer to " + destination.masked(), operation);
    }

    /** Credits this account as the destination of a transfer. */
    public LedgerEntry receiveTransfer(Money amount, Iban origin, Operation operation) {
        requireOperative();
        requireValidAmount(amount, "transfer");
        Objects.requireNonNull(origin, "origin IBAN is required");

        balance = balance.add(amount);
        events.add(new MoneyReceived(id, origin, amount, balance, operation.id(), operation.occurredAt()));
        return entry(LedgerEntryType.TRANSFER_IN, amount, origin, "Transfer from " + origin.masked(), operation);
    }

    public void freeze() {
        status = AccountStatus.FROZEN;
    }

    public void unfreeze() {
        if (status == AccountStatus.CLOSED) {
            throw new AccountNotOperativeException(iban.masked(), status.name());
        }
        status = AccountStatus.OPEN;
    }

    public void close() {
        if (!balance.isZero()) {
            throw new AccountNotEmptyException(iban.masked(), balance.toString());
        }
        status = AccountStatus.CLOSED;
    }

    /** Returns the events recorded since the last call and clears them. */
    public List<DomainEvent> pullEvents() {
        List<DomainEvent> recorded = List.copyOf(events);
        events.clear();
        return recorded;
    }

    public AccountId id() {
        return id;
    }

    public Iban iban() {
        return iban;
    }

    public String holderName() {
        return holderName;
    }

    public Currency currency() {
        return currency;
    }

    public Money balance() {
        return balance;
    }

    public AccountStatus status() {
        return status;
    }

    public Money dailyTransferLimit() {
        return dailyTransferLimit;
    }

    public LocalDate lastTransferDate() {
        return transferredOn;
    }

    public Money transferredToday() {
        return transferredToday;
    }

    /** How much has been transferred on the given business day; zero once the day changes. */
    private Money transferredOn(LocalDate businessDate) {
        return businessDate.equals(transferredOn) ? transferredToday : Money.zero(currency);
    }

    private LedgerEntry entry(
            LedgerEntryType type, Money amount, Iban counterparty, String description, Operation operation) {
        return new LedgerEntry(
                LedgerEntryId.newId(),
                id,
                type,
                amount,
                balance,
                counterparty,
                description,
                operation.id(),
                operation.occurredAt());
    }

    private void requireOperative() {
        if (!status.isOperative()) {
            throw new AccountNotOperativeException(iban.masked(), status.name().toLowerCase());
        }
    }

    private void requireValidAmount(Money amount, String operation) {
        Objects.requireNonNull(amount, "amount is required");
        if (!amount.currency().equals(currency)) {
            throw new CurrencyMismatchException(currency, amount.currency());
        }
        if (!amount.isPositive()) {
            throw new InvalidAmountException(operation);
        }
    }

    private void requireEnoughBalance(Money amount) {
        if (amount.isGreaterThan(balance)) {
            throw new InsufficientBalanceException(iban.masked());
        }
    }

    private static String requireHolderName(String holderName) {
        Objects.requireNonNull(holderName, "holder name is required");
        if (holderName.isBlank()) {
            throw new IllegalArgumentException("holder name cannot be blank");
        }
        return holderName.trim();
    }
}
