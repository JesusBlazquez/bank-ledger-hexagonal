package io.github.jesusblazquez.ledger.application.port.out;

import io.github.jesusblazquez.ledger.domain.AccountId;
import io.github.jesusblazquez.ledger.domain.LedgerEntry;
import io.github.jesusblazquez.ledger.domain.LedgerEntryId;
import java.util.List;
import java.util.Optional;

/** The ledger itself: entries can only be appended and read back. There is no update or delete. */
public interface LedgerRepository {

    void append(LedgerEntry entry);

    Optional<LedgerEntry> findById(LedgerEntryId id);

    /** Movements of an account, most recent first. */
    List<LedgerEntry> findByAccount(AccountId accountId, int page, int size);
}
