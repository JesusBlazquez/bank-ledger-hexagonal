package io.github.jesusblazquez.ledger.application.port.in;

/** Puts money into an account. */
public interface DepositUseCase {

    MovementResult deposit(MovementCommand command);
}
