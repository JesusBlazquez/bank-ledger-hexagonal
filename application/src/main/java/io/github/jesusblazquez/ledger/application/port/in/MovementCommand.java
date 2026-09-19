package io.github.jesusblazquez.ledger.application.port.in;

import io.github.jesusblazquez.ledger.domain.AccountId;
import io.github.jesusblazquez.ledger.domain.Money;
import io.github.jesusblazquez.ledger.domain.OperationId;
import java.util.Objects;

/**
 * A deposit or a withdrawal.
 *
 * @param requestFingerprint hash of the original request, used to tell a retry from a reused key
 */
public record MovementCommand(AccountId accountId, Money amount, OperationId operationId, String requestFingerprint) {

    public MovementCommand {
        Objects.requireNonNull(accountId, "account id is required");
        Objects.requireNonNull(amount, "amount is required");
        Objects.requireNonNull(operationId, "operation id is required");
        Objects.requireNonNull(requestFingerprint, "request fingerprint is required");
    }
}
