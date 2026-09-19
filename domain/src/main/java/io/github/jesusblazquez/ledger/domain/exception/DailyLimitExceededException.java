package io.github.jesusblazquez.ledger.domain.exception;

/** Thrown when a transfer would push the account over its limit for the business day. */
public class DailyLimitExceededException extends DomainException {

    public DailyLimitExceededException(String iban, String limit, String alreadyTransferred) {
        super("Account %s would exceed its daily transfer limit of %s (already transferred today: %s)"
                .formatted(iban, limit, alreadyTransferred));
    }
}
