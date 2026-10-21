
CREATE TABLE transfers (
    id BIGINT NOT NULL AUTO_INCREMENT,

    sender_account_id BIGINT NOT NULL,
    recipient_account_id BIGINT NOT NULL,

    amount_minor BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    description VARCHAR(255),

    idempotency_key VARCHAR(120) NOT NULL,

    status VARCHAR(20) NOT NULL,

    ledger_response JSON,
    failure_message VARCHAR(1000),

    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,

    CONSTRAINT pk_transfers
        PRIMARY KEY (id),

    CONSTRAINT uk_transfers_idempotency_key
        UNIQUE (idempotency_key),

    CONSTRAINT chk_transfers_amount_positive
        CHECK (amount_minor > 0),

    CONSTRAINT chk_transfers_different_accounts
        CHECK (sender_account_id <> recipient_account_id)
);