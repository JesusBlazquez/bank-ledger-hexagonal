package io.github.jesusblazquez.ledger.config;

import io.github.jesusblazquez.ledger.application.port.out.TransactionRunner;
import java.util.function.Supplier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * Gives the application the atomicity it asks for, using Spring's transaction template.
 *
 * <p>This is the only place in the codebase that knows Spring manages transactions. The use cases
 * just say "do this atomically".
 */
@Component
class SpringTransactionRunner implements TransactionRunner {

    private final TransactionTemplate transactionTemplate;

    SpringTransactionRunner(TransactionTemplate transactionTemplate) {
        this.transactionTemplate = transactionTemplate;
    }

    @Override
    public <T> T inTransaction(Supplier<T> work) {
        return transactionTemplate.execute(status -> work.get());
    }
}
