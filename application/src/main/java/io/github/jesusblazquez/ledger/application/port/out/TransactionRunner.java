package io.github.jesusblazquez.ledger.application.port.out;

import java.util.function.Supplier;

/**
 * Runs a block of work atomically.
 *
 * <p>A transfer touches two accounts and writes two ledger entries: either all of it happens or
 * none of it does. Instead of annotating the use cases with {@code @Transactional} — which would
 * drag Spring into this module — the application asks for atomicity through a port, and the
 * infrastructure decides how to provide it.
 */
public interface TransactionRunner {

    <T> T inTransaction(Supplier<T> work);
}
