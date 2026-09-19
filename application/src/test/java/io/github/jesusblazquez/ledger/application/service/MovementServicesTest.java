package io.github.jesusblazquez.ledger.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jesusblazquez.ledger.application.exception.AccountNotFoundException;
import io.github.jesusblazquez.ledger.application.exception.IdempotencyConflictException;
import io.github.jesusblazquez.ledger.application.port.in.AccountView;
import io.github.jesusblazquez.ledger.application.port.in.MovementCommand;
import io.github.jesusblazquez.ledger.application.port.in.MovementResult;
import io.github.jesusblazquez.ledger.domain.AccountId;
import io.github.jesusblazquez.ledger.domain.LedgerEntryType;
import io.github.jesusblazquez.ledger.domain.Money;
import io.github.jesusblazquez.ledger.domain.OperationId;
import io.github.jesusblazquez.ledger.domain.exception.InsufficientBalanceException;
import org.junit.jupiter.api.Test;

class MovementServicesTest {

    private final LedgerScenario scenario = new LedgerScenario();

    @Test
    void aDepositIncreasesTheBalanceAndIsRecordedInTheLedger() {
        AccountView account = scenario.openAccountFor("Jesús Blázquez");

        MovementResult result = scenario.deposit.deposit(LedgerScenario.movement(account.id(), "250.00"));

        assertThat(result.balance()).isEqualTo(Money.of("250.00", "EUR"));
        assertThat(result.replayed()).isFalse();
        assertThat(scenario.ledger.all()).singleElement().satisfies(entry -> {
            assertThat(entry.type()).isEqualTo(LedgerEntryType.DEPOSIT);
            assertThat(entry.balanceAfter()).isEqualTo(Money.of("250.00", "EUR"));
        });
    }

    @Test
    void aWithdrawalDecreasesTheBalance() {
        AccountView account = scenario.accountWith("Jesús Blázquez", "300.00");

        MovementResult result = scenario.withdraw.withdraw(LedgerScenario.movement(account.id(), "100.00"));

        assertThat(result.balance()).isEqualTo(Money.of("200.00", "EUR"));
    }

    @Test
    void aFailedWithdrawalLeavesNoTrace() {
        AccountView account = scenario.accountWith("Jesús Blázquez", "50.00");
        int entriesBefore = scenario.ledger.all().size();

        assertThatThrownBy(() -> scenario.withdraw.withdraw(LedgerScenario.movement(account.id(), "80.00")))
                .isInstanceOf(InsufficientBalanceException.class);

        assertThat(scenario.ledger.all()).hasSize(entriesBefore);
        assertThat(scenario.queries.byId(account.id()).balance()).isEqualTo(Money.of("50.00", "EUR"));
    }

    @Test
    void repeatingTheSameRequestDoesNotApplyItTwice() {
        AccountView account = scenario.openAccountFor("Jesús Blázquez");
        MovementCommand command = LedgerScenario.movement(account.id(), "100.00");

        MovementResult first = scenario.deposit.deposit(command);
        MovementResult retry = scenario.deposit.deposit(command);

        assertThat(retry.replayed()).isTrue();
        assertThat(retry.entryId()).isEqualTo(first.entryId());
        assertThat(retry.balance()).isEqualTo(Money.of("100.00", "EUR"));
        assertThat(scenario.queries.byId(account.id()).balance()).isEqualTo(Money.of("100.00", "EUR"));
        assertThat(scenario.ledger.all()).hasSize(1);
    }

    @Test
    void reusingAnOperationIdForADifferentRequestIsRejected() {
        AccountView account = scenario.openAccountFor("Jesús Blázquez");
        OperationId operationId = OperationId.newId();
        scenario.deposit.deposit(
                new MovementCommand(account.id(), Money.of("100.00", "EUR"), operationId, "first-request"));

        assertThatThrownBy(() -> scenario.deposit.deposit(
                        new MovementCommand(account.id(), Money.of("999.00", "EUR"), operationId, "another-request")))
                .isInstanceOf(IdempotencyConflictException.class);

        assertThat(scenario.queries.byId(account.id()).balance()).isEqualTo(Money.of("100.00", "EUR"));
    }

    @Test
    void movingMoneyOnAnUnknownAccountFails() {
        assertThatThrownBy(() -> scenario.deposit.deposit(LedgerScenario.movement(AccountId.newId(), "10.00")))
                .isInstanceOf(AccountNotFoundException.class);
    }
}
