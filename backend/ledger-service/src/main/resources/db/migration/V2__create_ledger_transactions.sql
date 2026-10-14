
CREATE TABLE ledger_transactions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    transaction_ref VARCHAR(36) NOT NULL,
    idempotency_key VARCHAR(120),
    transaction_type VARCHAR(24) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'POSTED',
    description VARCHAR(255),
    reversal_of_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_ledger_transactions PRIMARY KEY (id),

    CONSTRAINT uk_ledger_transactions_ref
        UNIQUE (transaction_ref),

    CONSTRAINT uk_ledger_transactions_idempotency
        UNIQUE (idempotency_key)
);
