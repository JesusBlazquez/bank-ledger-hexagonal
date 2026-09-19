package io.github.jesusblazquez.ledger.api;

import io.github.jesusblazquez.ledger.application.port.in.DepositUseCase;
import io.github.jesusblazquez.ledger.application.port.in.GetAccountUseCase;
import io.github.jesusblazquez.ledger.application.port.in.GetTransactionHistoryUseCase;
import io.github.jesusblazquez.ledger.application.port.in.MovementCommand;
import io.github.jesusblazquez.ledger.application.port.in.OpenAccountUseCase;
import io.github.jesusblazquez.ledger.application.port.in.OpenAccountUseCase.OpenAccountCommand;
import io.github.jesusblazquez.ledger.application.port.in.WithdrawUseCase;
import io.github.jesusblazquez.ledger.domain.AccountId;
import io.github.jesusblazquez.ledger.domain.OperationId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * The REST adapter.
 *
 * <p>It only translates: HTTP in, use case out. There is no business rule here, which is why the
 * same rules would still hold if this application grew a command line or a message listener.
 */
@RestController
@RequestMapping("/api/accounts")
@Tag(name = "Accounts", description = "Open accounts, move money and read the ledger")
class AccountController {

    private final OpenAccountUseCase openAccount;
    private final DepositUseCase deposit;
    private final WithdrawUseCase withdraw;
    private final GetAccountUseCase getAccount;
    private final GetTransactionHistoryUseCase getHistory;

    AccountController(
            OpenAccountUseCase openAccount,
            DepositUseCase deposit,
            WithdrawUseCase withdraw,
            GetAccountUseCase getAccount,
            GetTransactionHistoryUseCase getHistory) {
        this.openAccount = openAccount;
        this.deposit = deposit;
        this.withdraw = withdraw;
        this.getAccount = getAccount;
        this.getHistory = getHistory;
    }

    @PostMapping
    @Operation(summary = "Open an account and issue its IBAN")
    ResponseEntity<AccountResponse> open(@Valid @RequestBody OpenAccountRequest request) {
        AccountResponse account = AccountResponse.of(openAccount.open(new OpenAccountCommand(
                request.holderName(), request.dailyTransferLimit().toMoney())));

        return ResponseEntity.created(URI.create("/api/accounts/" + account.id()))
                .body(account);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Read an account with its current balance")
    AccountResponse byId(@PathVariable UUID id) {
        return AccountResponse.of(getAccount.byId(new AccountId(id)));
    }

    @PostMapping("/{id}/deposits")
    @Operation(summary = "Pay money into an account")
    MovementResponse deposit(
            @PathVariable UUID id,
            @RequestHeader("Idempotency-Key") UUID idempotencyKey,
            @Valid @RequestBody MovementRequest request) {

        return MovementResponse.of(deposit.deposit(movementCommand(id, idempotencyKey, request, "deposit")));
    }

    @PostMapping("/{id}/withdrawals")
    @Operation(summary = "Take money out of an account")
    MovementResponse withdraw(
            @PathVariable UUID id,
            @RequestHeader("Idempotency-Key") UUID idempotencyKey,
            @Valid @RequestBody MovementRequest request) {

        return MovementResponse.of(withdraw.withdraw(movementCommand(id, idempotencyKey, request, "withdrawal")));
    }

    @GetMapping("/{id}/transactions")
    @Operation(summary = "Read the movements of an account, most recent first")
    List<LedgerEntryResponse> transactions(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        return getHistory.forAccount(new AccountId(id), page, size).stream()
                .map(LedgerEntryResponse::of)
                .toList();
    }

    private MovementCommand movementCommand(UUID id, UUID idempotencyKey, MovementRequest request, String kind) {
        return new MovementCommand(
                new AccountId(id),
                request.amount().toMoney(),
                new OperationId(idempotencyKey),
                RequestFingerprint.of(
                        kind, id, request.amount().amount(), request.amount().currency()));
    }
}
