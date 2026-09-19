package io.github.jesusblazquez.ledger.application.fake;

import io.github.jesusblazquez.ledger.application.port.out.ProcessedOperations;
import io.github.jesusblazquez.ledger.domain.OperationId;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InMemoryProcessedOperations implements ProcessedOperations {

    private final Map<OperationId, ProcessedOperation> processed = new HashMap<>();

    @Override
    public Optional<ProcessedOperation> find(OperationId operationId) {
        return Optional.ofNullable(processed.get(operationId));
    }

    @Override
    public void record(ProcessedOperation operation) {
        // The real adapter relies on a unique key; this fake mimics that rejection.
        if (processed.putIfAbsent(operation.operationId(), operation) != null) {
            throw new IllegalStateException("Operation %s was already recorded".formatted(operation.operationId()));
        }
    }
}
