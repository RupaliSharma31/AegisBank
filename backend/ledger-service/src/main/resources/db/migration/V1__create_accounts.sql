
CREATE TABLE accounts (
    id BIGINT NOT NULL AUTO_INCREMENT,
    account_number VARCHAR(34) NOT NULL,
    customer_id VARCHAR(100) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    account_type VARCHAR(24) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT pk_accounts PRIMARY KEY (id),
    CONSTRAINT uk_accounts_account_number UNIQUE (account_number)
);
