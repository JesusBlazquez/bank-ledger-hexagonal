package io.github.jesusblazquez.ledger.persistence;

import io.github.jesusblazquez.ledger.application.port.out.LedgerRepository;
import io.github.jesusblazquez.ledger.domain.AccountId;
import io.github.jesusblazquez.ledger.domain.LedgerEntry;
import io.github.jesusblazquez.ledger.domain.LedgerEntryId;
import jakarta.persistence.EntityManager;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
class JpaLedgerRepository implements LedgerRepository {

    private final LedgerEntryJpaRepository entries;
    private final EntityManager entityManager;

    JpaLedgerRepository(LedgerEntryJpaRepository entries, EntityManager entityManager) {
        this.entries = entries;
        this.entityManager = entityManager;
    }

    @Override
    public void append(LedgerEntry entry) {
        // The ledger only ever grows, so this is an insert. Using persist also avoids the SELECT
        // that save() would issue to find out whether the row already exists.
        entityManager.persist(LedgerEntryMapper.toEntity(entry));
    }

    @Override
    public Optional<LedgerEntry> findById(LedgerEntryId id) {
        return entries.findById(id.value()).map(LedgerEntryMapper::toDomain);
    }

    @Override
    public List<LedgerEntry> findByAccount(AccountId accountId, int page, int size) {
        return entries.findByAccountIdOrderByOccurredAtDesc(accountId.value(), PageRequest.of(page, size)).stream()
                .map(LedgerEntryMapper::toDomain)
                .toList();
    }
}
