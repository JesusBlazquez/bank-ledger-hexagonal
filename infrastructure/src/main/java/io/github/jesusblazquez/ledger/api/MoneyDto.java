package io.github.jesusblazquez.ledger.api;

import io.github.jesusblazquez.ledger.domain.Money;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import java.math.BigDecimal;
import java.util.Currency;

/**
 * An amount as it travels over HTTP.
 *
 * <p>The amount is a string, not a JSON number, so that no client turns 10.10 into a float and back
 * into 10.099999999. The pattern rejects anything that is not a plain decimal before it reaches the
 * domain.
 */
public record MoneyDto(
        @NotBlank @Pattern(regexp = "-?\\d{1,15}(\\.\\d{1,2})?", message = "must be a decimal amount, e.g. 100.00")
        String amount,

        @NotBlank @Pattern(regexp = "[A-Z]{3}", message = "must be a three-letter currency code")
        String currency) {

    public Money toMoney() {
        return new Money(new BigDecimal(amount), Currency.getInstance(currency));
    }

    public static MoneyDto of(Money money) {
        return new MoneyDto(money.amount().toPlainString(), money.currency().getCurrencyCode());
    }
}
