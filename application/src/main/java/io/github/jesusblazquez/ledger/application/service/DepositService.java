package io.github.jesusblazquez.ledger.application.service;

import io.github.jesusblazquez.ledger.application.exception.AccountNotFoundException;
import io.github.jesusblazquez.ledger.application.port.in.DepositUseCase;
import io.github.jesusblazquez.ledger.application.port.in.MovementCommand;
import io.github.jesusblazquez.ledger.application.port.in.MovementResult;
import io.github.jesusblazquez.ledger.application.port.out.AccountRepository;
import io.github.jesusblazquez.ledger.application.port.out.DomainEventPublisher;
import io.github.jesusblazquez.ledger.application.port.out.LedgerRepository;
import io.github.jesusblazquez.ledger.application.port.out.ProcessedOperations;
import io.github.jesusblazquez.ledger.application.port.out.TransactionRunner;
import io.github.jesusblazquez.ledger.domain.Account;
import io.github.jesusblazquez.ledger.domain.LedgerEntry;
import java.time.Clock;

public class DepositService implements DepositUseCase {

    private final AccountRepository accounts;
    private final LedgerRepository ledger;
    private final DomainEventPublisher events;
    private final IdempotentMovements movements;

    public DepositService(
            AccountRepository accounts,
            LedgerRepository ledger,
            ProcessedOperations processedOperations,
            DomainEventPublisher events,
            TransactionRunner transactions,
            Clock clock) {
        this.accounts = accounts;
        this.ledger = ledger;
        this.events = events;
        this.movements = new IdempotentMovements(processedOperations, ledger, transactions, clock);
    }

    @Override
    public MovementResult deposit(MovementCommand command) {
        return movements.executeOnce(command.operationId(), command.requestFingerprint(), () -> {
            Account account = accounts.findById(command.accountId())
                    .orElseThrow(() -> new AccountNotFoundException(command.accountId()));

            // The rule lives in the aggregate; this method only orchestrates.
            LedgerEntry entry = account.deposit(command.amount(), movements.operationFor(command.operationId()));

            accounts.save(account);
            ledger.append(entry);
            events.publish(account.pullEvents());
            return entry;
        });
    }
}
