ALTER TABLE ledger_transactions ADD COLUMN request_hash VARCHAR(64);

CREATE UNIQUE INDEX uq_reversal_of ON ledger_transactions(reversal_of_id)
    WHERE reversal_of_id IS NOT NULL;

ALTER TABLE holds ADD CONSTRAINT uq_hold_ref UNIQUE (account_id , reference);