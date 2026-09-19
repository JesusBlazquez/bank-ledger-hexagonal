package io.github.jesusblazquez.ledger.application.service;

import io.github.jesusblazquez.ledger.application.exception.AccountNotFoundException;
import io.github.jesusblazquez.ledger.application.port.in.AccountView;
import io.github.jesusblazquez.ledger.application.port.in.GetAccountUseCase;
import io.github.jesusblazquez.ledger.application.port.in.GetTransactionHistoryUseCase;
import io.github.jesusblazquez.ledger.application.port.out.AccountRepository;
import io.github.jesusblazquez.ledger.application.port.out.LedgerRepository;
import io.github.jesusblazquez.ledger.domain.AccountId;
import io.github.jesusblazquez.ledger.domain.LedgerEntry;
import java.util.List;

/** Read-only side: no transactions, no events, nothing to undo. */
public class AccountQueryService implements GetAccountUseCase, GetTransactionHistoryUseCase {

    private static final int MAX_PAGE_SIZE = 100;

    private final AccountRepository accounts;
    private final LedgerRepository ledger;

    public AccountQueryService(AccountRepository accounts, LedgerRepository ledger) {
        this.accounts = accounts;
        this.ledger = ledger;
    }

    @Override
    public AccountView byId(AccountId id) {
        return accounts.findById(id).map(AccountView::of).orElseThrow(() -> new AccountNotFoundException(id));
    }

    @Override
    public List<LedgerEntry> forAccount(AccountId accountId, int page, int size) {
        if (accounts.findById(accountId).isEmpty()) {
            throw new AccountNotFoundException(accountId);
        }
        return ledger.findByAccount(accountId, Math.max(page, 0), Math.clamp(size, 1, MAX_PAGE_SIZE));
    }
}
