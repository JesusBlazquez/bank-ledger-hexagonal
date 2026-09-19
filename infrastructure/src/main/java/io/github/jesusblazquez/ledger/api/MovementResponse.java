package io.github.jesusblazquez.ledger.api;

import io.github.jesusblazquez.ledger.application.port.in.MovementResult;

/**
 * @param replayed true when this answer comes from a previously processed request instead of a new
 *     movement, which lets a client tell a retry from a fresh operation
 */
public record MovementResponse(String entryId, MoneyDto balance, boolean replayed) {

    static MovementResponse of(MovementResult result) {
        return new MovementResponse(result.entryId().toString(), MoneyDto.of(result.balance()), result.replayed());
    }
}
