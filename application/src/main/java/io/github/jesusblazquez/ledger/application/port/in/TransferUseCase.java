package io.github.jesusblazquez.ledger.application.port.in;

import io.github.jesusblazquez.ledger.domain.AccountId;
import io.github.jesusblazquez.ledger.domain.Iban;
import io.github.jesusblazquez.ledger.domain.Money;
import io.github.jesusblazquez.ledger.domain.OperationId;
import java.util.Objects;

/** Moves money between two accounts held in this system. */
public interface TransferUseCase {

    MovementResult transfer(TransferCommand command);

    record TransferCommand(
            AccountId sourceAccountId,
            Iban destination,
            Money amount,
            OperationId operationId,
            String requestFingerprint) {

        public TransferCommand {
            Objects.requireNonNull(sourceAccountId, "source account id is required");
            Objects.requireNonNull(destination, "destination IBAN is required");
            Objects.requireNonNull(amount, "amount is required");
            Objects.requireNonNull(operationId, "operation id is required");
            Objects.requireNonNull(requestFingerprint, "request fingerprint is required");
        }
    }
}
