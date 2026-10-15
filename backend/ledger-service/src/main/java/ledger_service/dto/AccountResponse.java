package ledger_service.dto;

import ledger_service.Entity.Account;
import ledger_service.Entity.AccountStatus;
import ledger_service.Entity.AccountType;

import java.time.Instant;

public record AccountResponse(
        Long id,
        String customerId,
        String currency,
        AccountType type,
        AccountStatus status,
        Instant createdAt
) {
    public static AccountResponse from(Account account) {
        return new AccountResponse(
                account.getId(),
                account.getCustomerId(),
                account.getCurrency(),
                account.getType(),
                account.getStatus(),
                account.getCreatedAt()
        );
    }
}

