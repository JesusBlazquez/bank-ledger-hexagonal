package io.github.jesusblazquez.ledger.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.UUID;

/**
 * The record that an operation was already applied.
 *
 * <p>The primary key is the operation id, so a duplicate insert fails at the database. That
 * constraint — not the check in the service — is what really prevents a retried request from moving
 * money twice.
 */
@Entity
@Table(name = "processed_operations")
class ProcessedOperationEntity {

    @Id
    @Column(name = "idempotency_key")
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false)
    private String requestHash;

    @Column(name = "resulting_entry_id", nullable = false)
    private UUID resultingEntryId;

    protected ProcessedOperationEntity() {
        // required by JPA
    }

    ProcessedOperationEntity(UUID operationId, String requestHash, UUID resultingEntryId) {
        this.idempotencyKey = operationId.toString();
        this.requestHash = requestHash;
        this.resultingEntryId = resultingEntryId;
    }

    String getIdempotencyKey() {
        return idempotencyKey;
    }

    String getRequestHash() {
        return requestHash;
    }

    UUID getResultingEntryId() {
        return resultingEntryId;
    }
}
