package io.github.jesusblazquez.ledger.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jesusblazquez.ledger.application.exception.AccountNotFoundException;
import io.github.jesusblazquez.ledger.application.port.in.AccountView;
import io.github.jesusblazquez.ledger.application.port.in.MovementResult;
import io.github.jesusblazquez.ledger.application.port.in.TransferUseCase.TransferCommand;
import io.github.jesusblazquez.ledger.domain.Iban;
import io.github.jesusblazquez.ledger.domain.LedgerEntryType;
import io.github.jesusblazquez.ledger.domain.Money;
import io.github.jesusblazquez.ledger.domain.OperationId;
import io.github.jesusblazquez.ledger.domain.exception.DailyLimitExceededException;
import io.github.jesusblazquez.ledger.domain.exception.InsufficientBalanceException;
import org.junit.jupiter.api.Test;

class TransferServiceTest {

    private final LedgerScenario scenario = new LedgerScenario();

    private TransferCommand transferOf(AccountView from, AccountView to, String amount) {
        return new TransferCommand(from.id(), to.iban(), Money.of(amount, "EUR"), OperationId.newId(), "fingerprint");
    }

    @Test
    void movesMoneyFromOneAccountToTheOther() {
        AccountView source = scenario.accountWith("Jesús Blázquez", "500.00");
        AccountView destination = scenario.accountWith("Ada Lovelace", "100.00");

        MovementResult result = scenario.transfer.transfer(transferOf(source, destination, "200.00"));

        assertThat(scenario.queries.byId(source.id()).balance()).isEqualTo(Money.of("300.00", "EUR"));
        assertThat(scenario.queries.byId(destination.id()).balance()).isEqualTo(Money.of("300.00", "EUR"));
        assertThat(result.balance()).isEqualTo(Money.of("300.00", "EUR"));
    }

    @Test
    void writesOneEntryOnEachSide() {
        AccountView source = scenario.accountWith("Jesús Blázquez", "500.00");
        AccountView destination = scenario.accountWith("Ada Lovelace", "0.01");

        scenario.transfer.transfer(transferOf(source, destination, "50.00"));

        assertThat(scenario.ledger.findByAccount(source.id(), 0, 10))
                .anySatisfy(entry -> assertThat(entry.type()).isEqualTo(LedgerEntryType.TRANSFER_OUT));
        assertThat(scenario.ledger.findByAccount(destination.id(), 0, 10))
                .anySatisfy(entry -> assertThat(entry.type()).isEqualTo(LedgerEntryType.TRANSFER_IN));
    }

    @Test
    void leavesBothAccountsUntouchedWhenTheSourceCannotPay() {
        AccountView source = scenario.accountWith("Jesús Blázquez", "10.00");
        AccountView destination = scenario.accountWith("Ada Lovelace", "10.00");

        assertThatThrownBy(() -> scenario.transfer.transfer(transferOf(source, destination, "50.00")))
                .isInstanceOf(InsufficientBalanceException.class);

        assertThat(scenario.queries.byId(source.id()).balance()).isEqualTo(Money.of("10.00", "EUR"));
        assertThat(scenario.queries.byId(destination.id()).balance()).isEqualTo(Money.of("10.00", "EUR"));
    }

    @Test
    void respectsTheDailyTransferLimitOfTheSource() {
        AccountView source = scenario.accountWith("Jesús Blázquez", "5000.00");
        AccountView destination = scenario.accountWith("Ada Lovelace", "0.01");
        scenario.transfer.transfer(transferOf(source, destination, "1000.00"));

        assertThatThrownBy(() -> scenario.transfer.transfer(transferOf(source, destination, "1.00")))
                .isInstanceOf(DailyLimitExceededException.class);
    }

    @Test
    void failsWhenTheDestinationDoesNotExist() {
        AccountView source = scenario.accountWith("Jesús Blázquez", "500.00");
        TransferCommand command = new TransferCommand(
                source.id(),
                Iban.of("DE89370400440532013000"),
                Money.of("10.00", "EUR"),
                OperationId.newId(),
                "fingerprint");

        assertThatThrownBy(() -> scenario.transfer.transfer(command)).isInstanceOf(AccountNotFoundException.class);
    }

    @Test
    void aRetriedTransferIsNotAppliedTwice() {
        AccountView source = scenario.accountWith("Jesús Blázquez", "500.00");
        AccountView destination = scenario.accountWith("Ada Lovelace", "0.01");
        TransferCommand command = transferOf(source, destination, "100.00");

        scenario.transfer.transfer(command);
        MovementResult retry = scenario.transfer.transfer(command);

        assertThat(retry.replayed()).isTrue();
        assertThat(scenario.queries.byId(source.id()).balance()).isEqualTo(Money.of("400.00", "EUR"));
        assertThat(scenario.queries.byId(destination.id()).balance()).isEqualTo(Money.of("100.01", "EUR"));
    }
}
