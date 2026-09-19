-- Account numbers are issued by the database so two concurrent openings cannot get the same one.
CREATE SEQUENCE account_number_seq START WITH 1 INCREMENT BY 1;

-- What we store for a processed operation is the entry it produced, not a response body.
-- Migrations that have already been applied are never edited, so the change goes in a new file.
ALTER TABLE processed_operations RENAME COLUMN response_body TO resulting_entry_id;
ALTER TABLE processed_operations
    ALTER COLUMN resulting_entry_id TYPE UUID USING resulting_entry_id::uuid;

-- CHAR pads with spaces and confuses schema validation; VARCHAR is what the mapping expects.
ALTER TABLE accounts ALTER COLUMN currency TYPE VARCHAR(3);
ALTER TABLE ledger_entries ALTER COLUMN currency TYPE VARCHAR(3);
