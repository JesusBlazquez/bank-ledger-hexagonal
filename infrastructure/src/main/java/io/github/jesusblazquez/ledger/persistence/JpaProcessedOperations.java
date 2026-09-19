package io.github.jesusblazquez.ledger.persistence;

import io.github.jesusblazquez.ledger.application.port.out.ProcessedOperations;
import io.github.jesusblazquez.ledger.domain.LedgerEntryId;
import io.github.jesusblazquez.ledger.domain.OperationId;
import jakarta.persistence.EntityManager;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
class JpaProcessedOperations implements ProcessedOperations {

    private final ProcessedOperationJpaRepository operations;
    private final EntityManager entityManager;

    JpaProcessedOperations(ProcessedOperationJpaRepository operations, EntityManager entityManager) {
        this.operations = operations;
        this.entityManager = entityManager;
    }

    @Override
    public Optional<ProcessedOperation> find(OperationId operationId) {
        return operations
                .findById(operationId.value().toString())
                .map(entity -> new ProcessedOperation(
                        operationId, entity.getRequestHash(), new LedgerEntryId(entity.getResultingEntryId())));
    }

    @Override
    public void record(ProcessedOperation operation) {
        // persist, not save: save() would happily update an existing row, and an operation that was
        // already applied must make the second attempt fail. flush() surfaces that failure here
        // rather than at commit time, where it would be harder to attribute.
        entityManager.persist(new ProcessedOperationEntity(
                operation.operationId().value(),
                operation.requestFingerprint(),
                operation.resultingEntry().value()));
        entityManager.flush();
    }
}
