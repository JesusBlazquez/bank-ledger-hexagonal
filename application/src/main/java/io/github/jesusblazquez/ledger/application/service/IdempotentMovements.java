package io.github.jesusblazquez.ledger.application.service;

import io.github.jesusblazquez.ledger.application.exception.IdempotencyConflictException;
import io.github.jesusblazquez.ledger.application.port.in.MovementResult;
import io.github.jesusblazquez.ledger.application.port.out.LedgerRepository;
import io.github.jesusblazquez.ledger.application.port.out.ProcessedOperations;
import io.github.jesusblazquez.ledger.application.port.out.ProcessedOperations.ProcessedOperation;
import io.github.jesusblazquez.ledger.application.port.out.TransactionRunner;
import io.github.jesusblazquez.ledger.domain.LedgerEntry;
import io.github.jesusblazquez.ledger.domain.Operation;
import io.github.jesusblazquez.ledger.domain.OperationId;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Runs a movement at most once, inside a transaction.
 *
 * <p>Every command carries an operation id. If that id was already processed, the stored result is
 * returned instead of touching any balance. The check runs inside the same transaction as the
 * movement, and the storage behind {@link ProcessedOperations} has a unique key, so two concurrent
 * retries cannot both get through: the loser fails on the insert rather than moving money twice.
 */
class IdempotentMovements {

    private final ProcessedOperations processedOperations;
    private final LedgerRepository ledger;
    private final TransactionRunner transactions;
    private final Clock clock;

    IdempotentMovements(
            ProcessedOperations processedOperations,
            LedgerRepository ledger,
            TransactionRunner transactions,
            Clock clock) {
        this.processedOperations = processedOperations;
        this.ledger = ledger;
        this.transactions = transactions;
        this.clock = clock;
    }

    MovementResult executeOnce(OperationId operationId, String requestFingerprint, Supplier<LedgerEntry> movement) {
        return transactions.inTransaction(() -> {
            Optional<ProcessedOperation> alreadyProcessed = processedOperations.find(operationId);
            if (alreadyProcessed.isPresent()) {
                return replay(alreadyProcessed.get(), requestFingerprint);
            }

            LedgerEntry entry = movement.get();
            processedOperations.record(new ProcessedOperation(operationId, requestFingerprint, entry.id()));
            return MovementResult.applied(entry);
        });
    }

    Operation operationFor(OperationId operationId) {
        return new Operation(operationId, clock.instant(), LocalDate.now(clock));
    }

    private MovementResult replay(ProcessedOperation previous, String requestFingerprint) {
        if (!previous.requestFingerprint().equals(requestFingerprint)) {
            throw new IdempotencyConflictException(previous.operationId());
        }
        LedgerEntry entry = ledger.findById(previous.resultingEntry())
                .orElseThrow(() -> new IllegalStateException(
                        "Operation %s was recorded but its ledger entry is missing".formatted(previous.operationId())));
        return MovementResult.replayed(entry);
    }
}
