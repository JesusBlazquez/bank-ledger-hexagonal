package io.github.jesusblazquez.ledger.application.port.out;

import io.github.jesusblazquez.ledger.domain.LedgerEntryId;
import io.github.jesusblazquez.ledger.domain.OperationId;
import java.util.Optional;

/**
 * Remembers which operations have already been applied, so a retried request is not applied twice.
 *
 * <p>The fingerprint is a hash of the request. It is what allows the system to tell a genuine retry
 * (same key, same body) from a mistake (same key, different body), which has to be rejected.
 */
public interface ProcessedOperations {

    Optional<ProcessedOperation> find(OperationId operationId);

    void record(ProcessedOperation operation);

    record ProcessedOperation(OperationId operationId, String requestFingerprint, LedgerEntryId resultingEntry) {}
}
