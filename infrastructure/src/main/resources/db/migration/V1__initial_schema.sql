-- Accounts: one row per account. The balance is a derived value kept for performance;
-- it can always be recomputed by summing the ledger entries.
CREATE TABLE accounts (
    id                  UUID PRIMARY KEY,
    iban                VARCHAR(34)  NOT NULL UNIQUE,
    holder_name         VARCHAR(140) NOT NULL,
    currency            CHAR(3)      NOT NULL,
    balance_amount      NUMERIC(19, 2) NOT NULL,
    status              VARCHAR(16)  NOT NULL,
    daily_transfer_limit NUMERIC(19, 2) NOT NULL,
    transferred_on      DATE,
    transferred_today   NUMERIC(19, 2) NOT NULL DEFAULT 0,
    version             BIGINT       NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT balance_not_negative CHECK (balance_amount >= 0),
    CONSTRAINT daily_limit_not_negative CHECK (daily_transfer_limit >= 0)
);

-- Ledger entries: append only. Rows are never updated or deleted.
CREATE TABLE ledger_entries (
    id              UUID PRIMARY KEY,
    account_id      UUID         NOT NULL REFERENCES accounts (id),
    type            VARCHAR(16)  NOT NULL,
    amount          NUMERIC(19, 2) NOT NULL,
    currency        CHAR(3)      NOT NULL,
    balance_after   NUMERIC(19, 2) NOT NULL,
    counterparty_iban VARCHAR(34),
    description     VARCHAR(140),
    operation_id    UUID         NOT NULL,
    occurred_at     TIMESTAMPTZ  NOT NULL DEFAULT now(),

    CONSTRAINT amount_is_positive CHECK (amount > 0)
);

CREATE INDEX idx_ledger_entries_account_occurred_at ON ledger_entries (account_id, occurred_at DESC);

-- Idempotency: the UNIQUE constraint is the real guarantee. Two concurrent requests carrying
-- the same key cannot both be applied, because the database rejects the second insert.
CREATE TABLE processed_operations (
    idempotency_key VARCHAR(64) PRIMARY KEY,
    request_hash    VARCHAR(64) NOT NULL,
    response_body   TEXT        NOT NULL,
    processed_at    TIMESTAMPTZ NOT NULL DEFAULT now()
);
