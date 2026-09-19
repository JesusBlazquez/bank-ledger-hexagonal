package io.github.jesusblazquez.ledger.application.service;

import static org.assertj.core.api.Assertions.assertThat;

import io.github.jesusblazquez.ledger.application.port.in.AccountView;
import io.github.jesusblazquez.ledger.domain.AccountStatus;
import io.github.jesusblazquez.ledger.domain.Iban;
import io.github.jesusblazquez.ledger.domain.Money;
import io.github.jesusblazquez.ledger.domain.event.AccountOpened;
import org.junit.jupiter.api.Test;

class OpenAccountServiceTest {

    private final LedgerScenario scenario = new LedgerScenario();

    @Test
    void issuesAValidSpanishIbanAndStartsAtZero() {
        AccountView account = scenario.openAccountFor("Jesús Blázquez");

        assertThat(account.iban().countryCode()).isEqualTo("ES");
        assertThat(Iban.of(account.iban().value())).isEqualTo(account.iban()); // check digits are valid
        assertThat(account.balance()).isEqualTo(Money.of("0.00", "EUR"));
        assertThat(account.status()).isEqualTo(AccountStatus.OPEN);
    }

    @Test
    void issuesADifferentIbanForEachAccount() {
        AccountView first = scenario.openAccountFor("Jesús Blázquez");
        AccountView second = scenario.openAccountFor("Ada Lovelace");

        assertThat(first.iban()).isNotEqualTo(second.iban());
    }

    @Test
    void announcesThatTheAccountWasOpened() {
        scenario.openAccountFor("Jesús Blázquez");

        assertThat(scenario.events.published()).singleElement().isInstanceOf(AccountOpened.class);
    }
}
