package io.github.jesusblazquez.ledger.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jesusblazquez.ledger.domain.exception.CurrencyMismatchException;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Nested
    @DisplayName("normalisation")
    class Normalisation {

        @Test
        void keepsTwoDecimals() {
            assertThat(Money.of("10", "EUR").amount()).isEqualByComparingTo("10.00");
        }

        @Test
        void amountsWithDifferentScalesAreEqual() {
            assertThat(Money.of("100.0", "EUR")).isEqualTo(Money.of("100.00", "EUR"));
        }

        @Test
        void roundsHalfToEven() {
            // 2.345 sits exactly between 2.34 and 2.35: half-even rounds to the even digit
            assertThat(Money.of("2.345", "EUR")).isEqualTo(Money.of("2.34", "EUR"));
            assertThat(Money.of("2.355", "EUR")).isEqualTo(Money.of("2.36", "EUR"));
        }

        @Test
        void amountsInDifferentCurrenciesAreNotEqual() {
            assertThat(Money.of("10.00", "EUR")).isNotEqualTo(Money.of("10.00", "USD"));
        }
    }

    @Nested
    @DisplayName("arithmetic")
    class Arithmetic {

        @Test
        void addsAmountsOfTheSameCurrency() {
            assertThat(Money.of("10.50", "EUR").add(Money.of("4.50", "EUR"))).isEqualTo(Money.of("15.00", "EUR"));
        }

        @Test
        void subtractsAmountsOfTheSameCurrency() {
            assertThat(Money.of("10.00", "EUR").subtract(Money.of("2.50", "EUR")))
                    .isEqualTo(Money.of("7.50", "EUR"));
        }

        @Test
        void subtractionCanProduceANegativeAmount() {
            assertThat(Money.of("1.00", "EUR").subtract(Money.of("2.00", "EUR")).isNegative())
                    .isTrue();
        }

        @Test
        void doesNotLosePrecisionWhereDoubleWould() {
            Money total = Money.zero(java.util.Currency.getInstance("EUR"));
            for (int i = 0; i < 10; i++) {
                total = total.add(Money.of("0.10", "EUR"));
            }
            assertThat(total).isEqualTo(Money.of("1.00", "EUR"));
        }

        @Test
        void refusesToAddDifferentCurrencies() {
            assertThatThrownBy(() -> Money.of("10.00", "EUR").add(Money.of("10.00", "USD")))
                    .isInstanceOf(CurrencyMismatchException.class)
                    .hasMessageContaining("EUR")
                    .hasMessageContaining("USD");
        }

        @Test
        void refusesToCompareDifferentCurrencies() {
            assertThatThrownBy(() -> Money.of("10.00", "EUR").isGreaterThan(Money.of("1.00", "USD")))
                    .isInstanceOf(CurrencyMismatchException.class);
        }
    }

    @Nested
    @DisplayName("comparison")
    class Comparison {

        @Test
        void comparesAmountsOfTheSameCurrency() {
            assertThat(Money.of("10.00", "EUR").isGreaterThan(Money.of("9.99", "EUR")))
                    .isTrue();
            assertThat(Money.of("10.00", "EUR").isLessThan(Money.of("10.01", "EUR")))
                    .isTrue();
        }

        @Test
        void recognisesZeroAndPositiveAmounts() {
            assertThat(Money.of("0.00", "EUR").isZero()).isTrue();
            assertThat(Money.of("0.01", "EUR").isPositive()).isTrue();
        }
    }

    @Test
    void rejectsNullValues() {
        assertThatThrownBy(() -> new Money(null, java.util.Currency.getInstance("EUR")))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new Money(BigDecimal.ONE, null)).isInstanceOf(NullPointerException.class);
    }
}
