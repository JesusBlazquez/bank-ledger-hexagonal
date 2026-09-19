package io.github.jesusblazquez.ledger.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jesusblazquez.ledger.domain.event.AccountOpened;
import io.github.jesusblazquez.ledger.domain.event.MoneyDeposited;
import io.github.jesusblazquez.ledger.domain.event.MoneySent;
import io.github.jesusblazquez.ledger.domain.exception.AccountNotEmptyException;
import io.github.jesusblazquez.ledger.domain.exception.AccountNotOperativeException;
import io.github.jesusblazquez.ledger.domain.exception.CurrencyMismatchException;
import io.github.jesusblazquez.ledger.domain.exception.DailyLimitExceededException;
import io.github.jesusblazquez.ledger.domain.exception.InsufficientBalanceException;
import io.github.jesusblazquez.ledger.domain.exception.InvalidAmountException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AccountTest {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Iban IBAN = Iban.of("ES9121000418450200051332");
    private static final Iban OTHER_IBAN = Iban.of("DE89370400440532013000");
    private static final Instant NOW = Instant.parse("2026-09-19T10:15:30Z");
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 19);
    private static final LocalDate TOMORROW = TODAY.plusDays(1);

    private static Operation operationOn(LocalDate businessDate) {
        return new Operation(OperationId.newId(), NOW, businessDate);
    }

    private static Operation operation() {
        return operationOn(TODAY);
    }

    private static Account accountWith(String balance) {
        Account account =
                Account.open(AccountId.newId(), IBAN, "Jesús Blázquez", EUR, Money.of("1000.00", "EUR"), operation());
        if (!balance.equals("0.00")) {
            account.deposit(Money.of(balance, "EUR"), operation());
        }
        account.pullEvents();
        return account;
    }

    @Nested
    @DisplayName("opening")
    class Opening {

        @Test
        void startsEmptyAndOperative() {
            Account account = Account.open(
                    AccountId.newId(), IBAN, "Jesús Blázquez", EUR, Money.of("1000.00", "EUR"), operation());

            assertThat(account.balance()).isEqualTo(Money.zero(EUR));
            assertThat(account.status()).isEqualTo(AccountStatus.OPEN);
            assertThat(account.transferredToday()).isEqualTo(Money.zero(EUR));
        }

        @Test
        void recordsThatItWasOpened() {
            Account account = Account.open(
                    AccountId.newId(), IBAN, "Jesús Blázquez", EUR, Money.of("1000.00", "EUR"), operation());

            assertThat(account.pullEvents()).singleElement().isInstanceOf(AccountOpened.class);
        }

        @Test
        void refusesALimitInAnotherCurrency() {
            assertThatThrownBy(() -> Account.open(
                            AccountId.newId(), IBAN, "Jesús Blázquez", EUR, Money.of("1000.00", "USD"), operation()))
                    .isInstanceOf(CurrencyMismatchException.class);
        }

        @Test
        void refusesABlankHolderName() {
            assertThatThrownBy(() ->
                            Account.open(AccountId.newId(), IBAN, "  ", EUR, Money.of("1000.00", "EUR"), operation()))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }

    @Nested
    @DisplayName("deposits")
    class Deposits {

        @Test
        void increaseTheBalanceAndRecordAnEntry() {
            Account account = accountWith("0.00");

            LedgerEntry entry = account.deposit(Money.of("150.75", "EUR"), operation());

            assertThat(account.balance()).isEqualTo(Money.of("150.75", "EUR"));
            assertThat(entry.type()).isEqualTo(LedgerEntryType.DEPOSIT);
            assertThat(entry.balanceAfter()).isEqualTo(Money.of("150.75", "EUR"));
            assertThat(account.pullEvents()).singleElement().isInstanceOf(MoneyDeposited.class);
        }

        @Test
        void rejectZeroAndNegativeAmounts() {
            Account account = accountWith("0.00");

            assertThatThrownBy(() -> account.deposit(Money.of("0.00", "EUR"), operation()))
                    .isInstanceOf(InvalidAmountException.class);
            assertThatThrownBy(() -> account.deposit(Money.of("-10.00", "EUR"), operation()))
                    .isInstanceOf(InvalidAmountException.class);
        }

        @Test
        void rejectAnotherCurrency() {
            Account account = accountWith("0.00");

            assertThatThrownBy(() -> account.deposit(Money.of("10.00", "USD"), operation()))
                    .isInstanceOf(CurrencyMismatchException.class);
        }
    }

    @Nested
    @DisplayName("withdrawals")
    class Withdrawals {

        @Test
        void decreaseTheBalance() {
            Account account = accountWith("100.00");

            LedgerEntry entry = account.withdraw(Money.of("40.00", "EUR"), operation());

            assertThat(account.balance()).isEqualTo(Money.of("60.00", "EUR"));
            assertThat(entry.type()).isEqualTo(LedgerEntryType.WITHDRAWAL);
        }

        @Test
        void mayEmptyTheAccountButNeverOverdrawIt() {
            Account account = accountWith("100.00");

            account.withdraw(Money.of("100.00", "EUR"), operation());
            assertThat(account.balance()).isEqualTo(Money.zero(EUR));

            assertThatThrownBy(() -> account.withdraw(Money.of("0.01", "EUR"), operation()))
                    .isInstanceOf(InsufficientBalanceException.class);
        }

        @Test
        void leaveTheBalanceUntouchedWhenTheyFail() {
            Account account = accountWith("50.00");

            assertThatThrownBy(() -> account.withdraw(Money.of("50.01", "EUR"), operation()))
                    .isInstanceOf(InsufficientBalanceException.class);
            assertThat(account.balance()).isEqualTo(Money.of("50.00", "EUR"));
        }
    }

    @Nested
    @DisplayName("outgoing transfers")
    class OutgoingTransfers {

        @Test
        void debitTheAccountAndRecordTheDestination() {
            Account account = accountWith("500.00");

            LedgerEntry entry = account.sendTransfer(Money.of("200.00", "EUR"), OTHER_IBAN, operation());

            assertThat(account.balance()).isEqualTo(Money.of("300.00", "EUR"));
            assertThat(entry.type()).isEqualTo(LedgerEntryType.TRANSFER_OUT);
            assertThat(entry.counterpartyIban()).contains(OTHER_IBAN);
            assertThat(account.pullEvents()).singleElement().isInstanceOf(MoneySent.class);
        }

        @Test
        void addUpAgainstTheDailyLimit() {
            Account account = accountWith("5000.00");

            account.sendTransfer(Money.of("600.00", "EUR"), OTHER_IBAN, operation());
            account.sendTransfer(Money.of("400.00", "EUR"), OTHER_IBAN, operation());

            assertThat(account.transferredToday()).isEqualTo(Money.of("1000.00", "EUR"));
            assertThatThrownBy(() -> account.sendTransfer(Money.of("0.01", "EUR"), OTHER_IBAN, operation()))
                    .isInstanceOf(DailyLimitExceededException.class);
        }

        @Test
        void startFromZeroAgainOnTheNextBusinessDay() {
            Account account = accountWith("5000.00");
            account.sendTransfer(Money.of("1000.00", "EUR"), OTHER_IBAN, operationOn(TODAY));

            account.sendTransfer(Money.of("1000.00", "EUR"), OTHER_IBAN, operationOn(TOMORROW));

            assertThat(account.transferredToday()).isEqualTo(Money.of("1000.00", "EUR"));
            assertThat(account.balance()).isEqualTo(Money.of("3000.00", "EUR"));
        }

        @Test
        void areRejectedWithoutEnoughBalanceEvenWithinTheLimit() {
            Account account = accountWith("100.00");

            assertThatThrownBy(() -> account.sendTransfer(Money.of("150.00", "EUR"), OTHER_IBAN, operation()))
                    .isInstanceOf(InsufficientBalanceException.class);
        }

        @Test
        void doNotConsumeTheDailyAllowanceWhenTheyFail() {
            Account account = accountWith("5000.00");

            assertThatThrownBy(() -> account.sendTransfer(Money.of("1500.00", "EUR"), OTHER_IBAN, operation()))
                    .isInstanceOf(DailyLimitExceededException.class);
            assertThat(account.transferredToday()).isEqualTo(Money.zero(EUR));
            assertThat(account.balance()).isEqualTo(Money.of("5000.00", "EUR"));
        }
    }

    @Nested
    @DisplayName("incoming transfers")
    class IncomingTransfers {

        @Test
        void creditTheAccountWithoutTouchingTheDailyAllowance() {
            Account account = accountWith("100.00");

            LedgerEntry entry = account.receiveTransfer(Money.of("250.00", "EUR"), OTHER_IBAN, operation());

            assertThat(account.balance()).isEqualTo(Money.of("350.00", "EUR"));
            assertThat(entry.type()).isEqualTo(LedgerEntryType.TRANSFER_IN);
            assertThat(account.transferredToday()).isEqualTo(Money.zero(EUR));
        }
    }

    @Nested
    @DisplayName("lifecycle")
    class Lifecycle {

        @Test
        void aFrozenAccountDoesNotMoveMoney() {
            Account account = accountWith("100.00");
            account.freeze();

            assertThatThrownBy(() -> account.deposit(Money.of("10.00", "EUR"), operation()))
                    .isInstanceOf(AccountNotOperativeException.class);
            assertThatThrownBy(() -> account.withdraw(Money.of("10.00", "EUR"), operation()))
                    .isInstanceOf(AccountNotOperativeException.class);
        }

        @Test
        void aFrozenAccountCanBeUnfrozen() {
            Account account = accountWith("100.00");
            account.freeze();

            account.unfreeze();

            assertThat(account.status()).isEqualTo(AccountStatus.OPEN);
        }

        @Test
        void anAccountHoldingMoneyCannotBeClosed() {
            Account account = accountWith("0.01");

            assertThatThrownBy(account::close).isInstanceOf(AccountNotEmptyException.class);
        }

        @Test
        void anEmptyAccountCanBeClosedAndThenDoesNotOperate() {
            Account account = accountWith("0.00");

            account.close();

            assertThat(account.status()).isEqualTo(AccountStatus.CLOSED);
            assertThatThrownBy(() -> account.deposit(Money.of("10.00", "EUR"), operation()))
                    .isInstanceOf(AccountNotOperativeException.class);
        }
    }

    @Test
    void eventsAreReturnedOnceAndThenCleared() {
        Account account = accountWith("0.00");
        account.deposit(Money.of("10.00", "EUR"), operation());

        assertThat(account.pullEvents()).hasSize(1);
        assertThat(account.pullEvents()).isEmpty();
    }
}
