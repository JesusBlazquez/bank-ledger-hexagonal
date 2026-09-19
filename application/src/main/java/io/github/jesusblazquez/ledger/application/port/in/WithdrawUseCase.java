package io.github.jesusblazquez.ledger.application.port.in;

/** Takes money out of an account. */
public interface WithdrawUseCase {

    MovementResult withdraw(MovementCommand command);
}
