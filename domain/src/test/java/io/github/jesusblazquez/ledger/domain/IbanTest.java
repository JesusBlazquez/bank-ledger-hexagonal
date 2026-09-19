package io.github.jesusblazquez.ledger.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jesusblazquez.ledger.domain.exception.InvalidIbanException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class IbanTest {

    @ParameterizedTest
    @ValueSource(strings = {"ES9121000418450200051332", "DE89370400440532013000", "GB33BUKB20201555555555"})
    void acceptsValidIbans(String value) {
        assertThat(Iban.of(value).value()).isEqualTo(value);
    }

    @Test
    void normalisesSpacesAndLowercase() {
        assertThat(Iban.of("es91 2100 0418 4502 0005 1332").value()).isEqualTo("ES9121000418450200051332");
    }

    @Test
    void rejectsWrongCheckDigits() {
        // Same account as the valid one above with the last digit changed
        assertThatThrownBy(() -> Iban.of("ES9121000418450200051333"))
                .isInstanceOf(InvalidIbanException.class)
                .hasMessageContaining("check digits");
    }

    @ParameterizedTest
    @ValueSource(strings = {"1234", "ESES1210004184502000513", "ES91-2100", ""})
    void rejectsMalformedValues(String value) {
        assertThatThrownBy(() -> Iban.of(value)).isInstanceOf(InvalidIbanException.class);
    }

    @Test
    void rejectsNull() {
        assertThatThrownBy(() -> Iban.of(null)).isInstanceOf(NullPointerException.class);
    }

    @Test
    void exposesTheCountryCode() {
        assertThat(Iban.of("ES9121000418450200051332").countryCode()).isEqualTo("ES");
    }

    @Test
    void masksTheAccountForLogs() {
        assertThat(Iban.of("ES9121000418450200051332").masked())
                .startsWith("ES91")
                .endsWith("1332")
                .doesNotContain("2100041845");
    }
}
