package io.github.jesusblazquez.ledger.application.fake;

import io.github.jesusblazquez.ledger.application.port.out.TransactionRunner;
import java.util.function.Supplier;

/** Runs the work straight away: transactional behaviour is the infrastructure's job to prove. */
public class DirectTransactionRunner implements TransactionRunner {

    @Override
    public <T> T inTransaction(Supplier<T> work) {
        return work.get();
    }
}
