package io.github.jesusblazquez.ledger.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OpenAccountRequest(
        @NotBlank @Size(max = 140) String holderName,
        @NotNull @Valid MoneyDto dailyTransferLimit) {}
