
CREATE TABLE holds (
    id BIGINT NOT NULL AUTO_INCREMENT,
    account_id BIGINT NOT NULL,
    amount_minor BIGINT NOT NULL,
    currency VARCHAR(3) NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    reference VARCHAR(120) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    released_at TIMESTAMP NULL,

    CONSTRAINT pk_holds PRIMARY KEY (id),

    CONSTRAINT fk_holds_account
        FOREIGN KEY (account_id)
        REFERENCES accounts(id)
);
