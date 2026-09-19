package io.github.jesusblazquez.ledger.application.port.in;

import io.github.jesusblazquez.ledger.domain.AccountId;
import io.github.jesusblazquez.ledger.domain.LedgerEntry;
import java.util.List;

/** Reads the movements of an account, most recent first. */
public interface GetTransactionHistoryUseCase {

    List<LedgerEntry> forAccount(AccountId accountId, int page, int size);
}
