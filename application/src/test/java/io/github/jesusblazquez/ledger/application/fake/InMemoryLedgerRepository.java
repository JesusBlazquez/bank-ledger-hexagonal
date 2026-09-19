package io.github.jesusblazquez.ledger.application.fake;

import io.github.jesusblazquez.ledger.application.port.out.LedgerRepository;
import io.github.jesusblazquez.ledger.domain.AccountId;
import io.github.jesusblazquez.ledger.domain.LedgerEntry;
import io.github.jesusblazquez.ledger.domain.LedgerEntryId;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class InMemoryLedgerRepository implements LedgerRepository {

    private final List<LedgerEntry> entries = new ArrayList<>();

    @Override
    public void append(LedgerEntry entry) {
        entries.add(entry);
    }

    @Override
    public Optional<LedgerEntry> findById(LedgerEntryId id) {
        return entries.stream().filter(entry -> entry.id().equals(id)).findFirst();
    }

    @Override
    public List<LedgerEntry> findByAccount(AccountId accountId, int page, int size) {
        return entries.stream()
                .filter(entry -> entry.accountId().equals(accountId))
                .sorted(Comparator.comparing(LedgerEntry::occurredAt).reversed())
                .skip((long) page * size)
                .limit(size)
                .toList();
    }

    public List<LedgerEntry> all() {
        return List.copyOf(entries);
    }
}
