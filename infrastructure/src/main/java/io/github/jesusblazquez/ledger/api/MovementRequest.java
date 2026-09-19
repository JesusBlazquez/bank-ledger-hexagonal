package io.github.jesusblazquez.ledger.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record MovementRequest(@NotNull @Valid MoneyDto amount) {}
