package io.github.jesusblazquez.ledger.api;

import io.github.jesusblazquez.ledger.application.port.in.TransferUseCase;
import io.github.jesusblazquez.ledger.application.port.in.TransferUseCase.TransferCommand;
import io.github.jesusblazquez.ledger.domain.AccountId;
import io.github.jesusblazquez.ledger.domain.Iban;
import io.github.jesusblazquez.ledger.domain.OperationId;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transfers")
@Tag(name = "Transfers", description = "Move money between accounts of this ledger")
class TransferController {

    private final TransferUseCase transfer;

    TransferController(TransferUseCase transfer) {
        this.transfer = transfer;
    }

    @PostMapping
    @Operation(summary = "Transfer money between two accounts")
    MovementResponse transfer(
            @RequestHeader("Idempotency-Key") UUID idempotencyKey, @Valid @RequestBody TransferRequest request) {

        TransferCommand command = new TransferCommand(
                new AccountId(UUID.fromString(request.sourceAccountId())),
                Iban.of(request.destinationIban()),
                request.amount().toMoney(),
                new OperationId(idempotencyKey),
                RequestFingerprint.of(
                        "transfer",
                        request.sourceAccountId(),
                        request.destinationIban(),
                        request.amount().amount(),
                        request.amount().currency()));

        return MovementResponse.of(transfer.transfer(command));
    }
}
