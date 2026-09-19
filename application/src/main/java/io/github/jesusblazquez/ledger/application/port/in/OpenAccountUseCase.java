package io.github.jesusblazquez.ledger.application.port.in;

import io.github.jesusblazquez.ledger.domain.Money;
import java.util.Objects;

/** Opens a new account and issues its IBAN. */
public interface OpenAccountUseCase {

    AccountView open(OpenAccountCommand command);

    record OpenAccountCommand(String holderName, Money dailyTransferLimit) {

        public OpenAccountCommand {
            Objects.requireNonNull(holderName, "holder name is required");
            Objects.requireNonNull(dailyTransferLimit, "daily transfer limit is required");
        }
    }
}
