
CREATE TABLE ledger_entries (
    id BIGINT NOT NULL AUTO_INCREMENT,
    transaction_id BIGINT NOT NULL,
    account_id BIGINT NOT NULL,
    direction VARCHAR(8) NOT NULL,
    amount_minor BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_ledger_entries PRIMARY KEY (id),

    CONSTRAINT fk_entries_transaction
        FOREIGN KEY (transaction_id)
        REFERENCES ledger_transactions(id),

    CONSTRAINT fk_entries_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(id),

    INDEX idx_entries_account (account_id, id)
);
