package io.github.jesusblazquez.ledger.api;

import io.github.jesusblazquez.ledger.application.port.in.AccountView;

public record AccountResponse(
        String id,
        String iban,
        String holderName,
        MoneyDto balance,
        String status,
        MoneyDto dailyTransferLimit,
        MoneyDto transferredToday) {

    static AccountResponse of(AccountView view) {
        return new AccountResponse(
                view.id().toString(),
                view.iban().value(),
                view.holderName(),
                MoneyDto.of(view.balance()),
                view.status().name(),
                MoneyDto.of(view.dailyTransferLimit()),
                MoneyDto.of(view.transferredToday()));
    }
}
