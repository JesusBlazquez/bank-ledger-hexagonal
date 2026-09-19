package io.github.jesusblazquez.ledger.application.service;

import io.github.jesusblazquez.ledger.application.fake.DirectTransactionRunner;
import io.github.jesusblazquez.ledger.application.fake.InMemoryAccountRepository;
import io.github.jesusblazquez.ledger.application.fake.InMemoryLedgerRepository;
import io.github.jesusblazquez.ledger.application.fake.InMemoryProcessedOperations;
import io.github.jesusblazquez.ledger.application.fake.RecordingEventPublisher;
import io.github.jesusblazquez.ledger.application.port.in.AccountView;
import io.github.jesusblazquez.ledger.application.port.in.MovementCommand;
import io.github.jesusblazquez.ledger.application.port.in.OpenAccountUseCase.OpenAccountCommand;
import io.github.jesusblazquez.ledger.domain.AccountId;
import io.github.jesusblazquez.ledger.domain.Money;
import io.github.jesusblazquez.ledger.domain.OperationId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

/** Wires the use cases with in-memory adapters so each test reads as a scenario. */
class LedgerScenario {

    static final Instant NOW = Instant.parse("2026-09-19T09:00:00Z");
    static final ZoneId MADRID = ZoneId.of("Europe/Madrid");

    final InMemoryAccountRepository accounts = new InMemoryAccountRepository();
    final InMemoryLedgerRepository ledger = new InMemoryLedgerRepository();
    final InMemoryProcessedOperations processedOperations = new InMemoryProcessedOperations();
    final RecordingEventPublisher events = new RecordingEventPublisher();
    final DirectTransactionRunner transactions = new DirectTransactionRunner();
    final Clock clock = Clock.fixed(NOW, MADRID);

    final OpenAccountService openAccount = new OpenAccountService(accounts, events, transactions, clock);
    final DepositService deposit =
            new DepositService(accounts, ledger, processedOperations, events, transactions, clock);
    final WithdrawService withdraw =
            new WithdrawService(accounts, ledger, processedOperations, events, transactions, clock);
    final TransferService transfer =
            new TransferService(accounts, ledger, processedOperations, events, transactions, clock);
    final AccountQueryService queries = new AccountQueryService(accounts, ledger);

    AccountView openAccountFor(String holder) {
        return openAccount.open(new OpenAccountCommand(holder, Money.of("1000.00", "EUR")));
    }

    AccountView accountWith(String holder, String balance) {
        AccountView account = openAccountFor(holder);
        deposit.deposit(movement(account.id(), balance));
        return queries.byId(account.id());
    }

    static MovementCommand movement(AccountId accountId, String amount) {
        return new MovementCommand(accountId, Money.of(amount, "EUR"), OperationId.newId(), "fingerprint");
    }
}
