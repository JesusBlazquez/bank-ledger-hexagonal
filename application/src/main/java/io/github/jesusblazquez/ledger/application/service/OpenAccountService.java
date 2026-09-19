package io.github.jesusblazquez.ledger.application.service;

import io.github.jesusblazquez.ledger.application.port.in.AccountView;
import io.github.jesusblazquez.ledger.application.port.in.OpenAccountUseCase;
import io.github.jesusblazquez.ledger.application.port.out.AccountRepository;
import io.github.jesusblazquez.ledger.application.port.out.DomainEventPublisher;
import io.github.jesusblazquez.ledger.application.port.out.TransactionRunner;
import io.github.jesusblazquez.ledger.domain.Account;
import io.github.jesusblazquez.ledger.domain.AccountId;
import io.github.jesusblazquez.ledger.domain.Iban;
import io.github.jesusblazquez.ledger.domain.Operation;
import io.github.jesusblazquez.ledger.domain.OperationId;
import java.time.Clock;
import java.time.LocalDate;

public class OpenAccountService implements OpenAccountUseCase {

    /** Accounts issued by this ledger are Spanish. A multi-country bank would make this a policy. */
    private static final String COUNTRY = "ES";

    private final AccountRepository accounts;
    private final DomainEventPublisher events;
    private final TransactionRunner transactions;
    private final Clock clock;

    public OpenAccountService(
            AccountRepository accounts, DomainEventPublisher events, TransactionRunner transactions, Clock clock) {
        this.accounts = accounts;
        this.events = events;
        this.transactions = transactions;
        this.clock = clock;
    }

    @Override
    public AccountView open(OpenAccountCommand command) {
        return transactions.inTransaction(() -> {
            // The currency of the account is the currency of its limit: one decision, stated once.
            Iban iban = Iban.forAccount(COUNTRY, accounts.nextAccountNumber());
            Operation operation = new Operation(OperationId.newId(), clock.instant(), LocalDate.now(clock));

            Account account = Account.open(
                    AccountId.newId(),
                    iban,
                    command.holderName(),
                    command.dailyTransferLimit().currency(),
                    command.dailyTransferLimit(),
                    operation);

            accounts.save(account);
            events.publish(account.pullEvents());
            return AccountView.of(account);
        });
    }
}
