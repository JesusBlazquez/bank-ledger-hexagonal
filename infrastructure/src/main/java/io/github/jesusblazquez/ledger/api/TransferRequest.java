package io.github.jesusblazquez.ledger.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record TransferRequest(
        @NotBlank String sourceAccountId,
        @NotBlank String destinationIban,
        @NotNull @Valid MoneyDto amount) {}
