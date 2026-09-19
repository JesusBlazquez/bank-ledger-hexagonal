package io.github.jesusblazquez.ledger.application.port.in;

import io.github.jesusblazquez.ledger.domain.Account;
import io.github.jesusblazquez.ledger.domain.AccountId;
import io.github.jesusblazquez.ledger.domain.AccountStatus;
import io.github.jesusblazquez.ledger.domain.Iban;
import io.github.jesusblazquez.ledger.domain.Money;

/**
 * What the outside world is told about an account.
 *
 * <p>The aggregate is never handed out: exposing it would let a caller reach its behaviour and
 * would tie the API shape to the model. This is a read-only snapshot.
 */
public record AccountView(
        AccountId id,
        Iban iban,
        String holderName,
        Money balance,
        AccountStatus status,
        Money dailyTransferLimit,
        Money transferredToday) {

    public static AccountView of(Account account) {
        return new AccountView(
                account.id(),
                account.iban(),
                account.holderName(),
                account.balance(),
                account.status(),
                account.dailyTransferLimit(),
                account.transferredToday());
    }
}
