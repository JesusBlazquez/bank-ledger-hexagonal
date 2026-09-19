package io.github.jesusblazquez.ledger.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.jesusblazquez.ledger.TestcontainersConfiguration;
import io.github.jesusblazquez.ledger.application.port.out.AccountRepository;
import io.github.jesusblazquez.ledger.application.port.out.LedgerRepository;
import io.github.jesusblazquez.ledger.application.port.out.ProcessedOperations;
import io.github.jesusblazquez.ledger.application.port.out.ProcessedOperations.ProcessedOperation;
import io.github.jesusblazquez.ledger.application.port.out.TransactionRunner;
import io.github.jesusblazquez.ledger.domain.Account;
import io.github.jesusblazquez.ledger.domain.AccountId;
import io.github.jesusblazquez.ledger.domain.AccountStatus;
import io.github.jesusblazquez.ledger.domain.Iban;
import io.github.jesusblazquez.ledger.domain.LedgerEntry;
import io.github.jesusblazquez.ledger.domain.Money;
import io.github.jesusblazquez.ledger.domain.Operation;
import io.github.jesusblazquez.ledger.domain.OperationId;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Currency;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

/**
 * Checks the adapters against a real PostgreSQL: the mapping round-trips, the ledger reads back in
 * the right order, and the idempotency key is enforced by the database itself.
 */
@Import(TestcontainersConfiguration.class)
@SpringBootTest
class LedgerPersistenceIT {

    private static final Currency EUR = Currency.getInstance("EUR");
    private static final Instant NOW = Instant.parse("2026-09-19T09:00:00Z");
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 19);

    @Autowired
    private AccountRepository accounts;

    @Autowired
    private LedgerRepository ledger;

    @Autowired
    private ProcessedOperations processedOperations;

    @Autowired
    private TransactionRunner transactions;

    private Operation operation() {
        return new Operation(OperationId.newId(), NOW, TODAY);
    }

    private Account newAccount(String holder) {
        return Account.open(
                AccountId.newId(),
                Iban.forAccount("ES", accounts.nextAccountNumber()),
                holder,
                EUR,
                Money.of("1000.00", "EUR"),
                operation());
    }

    @Test
    void anAccountSurvivesARoundTrip() {
        Account account = newAccount("Jesús Blázquez");
        account.deposit(Money.of("250.55", "EUR"), operation());
        account.sendTransfer(Money.of("100.00", "EUR"), Iban.of("DE89370400440532013000"), operation());

        transactions.inTransaction(() -> {
            accounts.save(account);
            return null;
        });

        Account reloaded = accounts.findById(account.id()).orElseThrow();
        assertThat(reloaded.iban()).isEqualTo(account.iban());
        assertThat(reloaded.holderName()).isEqualTo("Jesús Blázquez");
        assertThat(reloaded.balance()).isEqualTo(Money.of("150.55", "EUR"));
        assertThat(reloaded.status()).isEqualTo(AccountStatus.OPEN);
        assertThat(reloaded.transferredToday()).isEqualTo(Money.of("100.00", "EUR"));
        assertThat(reloaded.lastTransferDate()).isEqualTo(TODAY);
    }

    @Test
    void anAccountCanBeFoundByItsIban() {
        Account account = newAccount("Ada Lovelace");
        transactions.inTransaction(() -> {
            accounts.save(account);
            return null;
        });

        assertThat(accounts.findByIban(account.iban()).map(Account::id)).contains(account.id());
    }

    @Test
    void accountNumbersAreNeverRepeated() {
        assertThat(accounts.nextAccountNumber()).isNotEqualTo(accounts.nextAccountNumber());
    }

    @Test
    void ledgerEntriesComeBackMostRecentFirst() {
        Account account = newAccount("Jesús Blázquez");
        LedgerEntry first = account.deposit(Money.of("100.00", "EUR"), new Operation(OperationId.newId(), NOW, TODAY));
        LedgerEntry second = account.deposit(
                Money.of("50.00", "EUR"), new Operation(OperationId.newId(), NOW.plusSeconds(60), TODAY));

        transactions.inTransaction(() -> {
            accounts.save(account);
            ledger.append(first);
            ledger.append(second);
            return null;
        });

        assertThat(ledger.findByAccount(account.id(), 0, 10))
                .extracting(LedgerEntry::id)
                .containsExactly(second.id(), first.id());
        assertThat(ledger.findById(first.id()).orElseThrow().amount()).isEqualTo(Money.of("100.00", "EUR"));
    }

    @Test
    void theSameOperationCannotBeRecordedTwice() {
        Account account = newAccount("Jesús Blázquez");
        LedgerEntry entry = account.deposit(Money.of("10.00", "EUR"), operation());
        OperationId operationId = entry.operationId();

        transactions.inTransaction(() -> {
            accounts.save(account);
            ledger.append(entry);
            processedOperations.record(new ProcessedOperation(operationId, "fingerprint", entry.id()));
            return null;
        });

        assertThat(processedOperations.find(operationId)).isPresent();

        // The database, not the application, is what refuses the duplicate.
        assertThatThrownBy(() -> transactions.inTransaction(() -> {
                    processedOperations.record(new ProcessedOperation(operationId, "fingerprint", entry.id()));
                    return null;
                }))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}
