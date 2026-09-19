package io.github.jesusblazquez.ledger.config;

import io.github.jesusblazquez.ledger.application.port.in.DepositUseCase;
import io.github.jesusblazquez.ledger.application.port.in.OpenAccountUseCase;
import io.github.jesusblazquez.ledger.application.port.in.TransferUseCase;
import io.github.jesusblazquez.ledger.application.port.in.WithdrawUseCase;
import io.github.jesusblazquez.ledger.application.port.out.AccountRepository;
import io.github.jesusblazquez.ledger.application.port.out.DomainEventPublisher;
import io.github.jesusblazquez.ledger.application.port.out.LedgerRepository;
import io.github.jesusblazquez.ledger.application.port.out.ProcessedOperations;
import io.github.jesusblazquez.ledger.application.port.out.TransactionRunner;
import io.github.jesusblazquez.ledger.application.service.AccountQueryService;
import io.github.jesusblazquez.ledger.application.service.DepositService;
import io.github.jesusblazquez.ledger.application.service.OpenAccountService;
import io.github.jesusblazquez.ledger.application.service.TransferService;
import io.github.jesusblazquez.ledger.application.service.WithdrawService;
import java.time.Clock;
import java.time.ZoneId;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Wires the use cases.
 *
 * <p>The application classes carry no Spring annotations, so they are assembled here as ordinary
 * objects. That is what keeps them usable — and testable — without a container.
 */
@Configuration
class UseCaseConfiguration {

    /** Business days follow the bank's timezone, not the server's. */
    @Bean
    Clock clock() {
        return Clock.system(ZoneId.of("Europe/Madrid"));
    }

    @Bean
    OpenAccountUseCase openAccountUseCase(
            AccountRepository accounts, DomainEventPublisher events, TransactionRunner transactions, Clock clock) {
        return new OpenAccountService(accounts, events, transactions, clock);
    }

    @Bean
    DepositUseCase depositUseCase(
            AccountRepository accounts,
            LedgerRepository ledger,
            ProcessedOperations processedOperations,
            DomainEventPublisher events,
            TransactionRunner transactions,
            Clock clock) {
        return new DepositService(accounts, ledger, processedOperations, events, transactions, clock);
    }

    @Bean
    WithdrawUseCase withdrawUseCase(
            AccountRepository accounts,
            LedgerRepository ledger,
            ProcessedOperations processedOperations,
            DomainEventPublisher events,
            TransactionRunner transactions,
            Clock clock) {
        return new WithdrawService(accounts, ledger, processedOperations, events, transactions, clock);
    }

    @Bean
    TransferUseCase transferUseCase(
            AccountRepository accounts,
            LedgerRepository ledger,
            ProcessedOperations processedOperations,
            DomainEventPublisher events,
            TransactionRunner transactions,
            Clock clock) {
        return new TransferService(accounts, ledger, processedOperations, events, transactions, clock);
    }

    /**
     * One bean serves both read use cases: {@link AccountQueryService} implements the two
     * interfaces, so declaring it once is enough for Spring to inject it wherever either is asked
     * for. Declaring it three times would leave two beans of the same type and an ambiguous
     * injection point.
     */
    @Bean
    AccountQueryService accountQueryService(AccountRepository accounts, LedgerRepository ledger) {
        return new AccountQueryService(accounts, ledger);
    }
}
