package io.github.jesusblazquez.ledger.application.service;

import io.github.jesusblazquez.ledger.application.exception.AccountNotFoundException;
import io.github.jesusblazquez.ledger.application.port.in.MovementResult;
import io.github.jesusblazquez.ledger.application.port.in.TransferUseCase;
import io.github.jesusblazquez.ledger.application.port.out.AccountRepository;
import io.github.jesusblazquez.ledger.application.port.out.DomainEventPublisher;
import io.github.jesusblazquez.ledger.application.port.out.LedgerRepository;
import io.github.jesusblazquez.ledger.application.port.out.ProcessedOperations;
import io.github.jesusblazquez.ledger.application.port.out.TransactionRunner;
import io.github.jesusblazquez.ledger.domain.Account;
import io.github.jesusblazquez.ledger.domain.LedgerEntry;
import io.github.jesusblazquez.ledger.domain.Operation;
import java.time.Clock;

/**
 * Moves money between two accounts of this ledger.
 *
 * <p>Both sides commit together: the debit, the credit and their two ledger entries are written in
 * a single transaction. That works because both accounts live in the same database. If they lived
 * in different services this would have to become a saga with compensating events — see ADR 0005.
 */
public class TransferService implements TransferUseCase {

    private final AccountRepository accounts;
    private final LedgerRepository ledger;
    private final DomainEventPublisher events;
    private final IdempotentMovements movements;

    public TransferService(
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
    public MovementResult transfer(TransferCommand command) {
        return movements.executeOnce(command.operationId(), command.requestFingerprint(), () -> {
            Account source = accounts.findById(command.sourceAccountId())
                    .orElseThrow(() -> new AccountNotFoundException(command.sourceAccountId()));
            Account destination = accounts.findByIban(command.destination())
                    .orElseThrow(() -> new AccountNotFoundException(command.destination()));

            Operation operation = movements.operationFor(command.operationId());

            // Debit first: if the source cannot pay, nothing else has happened yet.
            LedgerEntry debit = source.sendTransfer(command.amount(), destination.iban(), operation);
            LedgerEntry credit = destination.receiveTransfer(command.amount(), source.iban(), operation);

            accounts.save(source);
            accounts.save(destination);
            ledger.append(debit);
            ledger.append(credit);
            events.publish(source.pullEvents());
            events.publish(destination.pullEvents());

            // The caller asked to send money, so the result describes the debit.
            return debit;
        });
    }
}
