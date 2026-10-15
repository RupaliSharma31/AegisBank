package ledger_service.dto;

import ledger_service.Entity.LedgerTransaction;
import ledger_service.Entity.TransactionStatus;
import ledger_service.Entity.TransactionType;

import java.time.Instant;

public record PostTransactionResponse(Long id,
                                      String transactionRef,
                                      TransactionType type,
                                      TransactionStatus status,
                                      String description,
                                      Instant createdAt,
                                      boolean duplicate) {
    public static PostTransactionResponse from(
            LedgerTransaction transaction,
            boolean duplicate
    ) {
        return new PostTransactionResponse(
                transaction.getId(),
                transaction.getTransactionRef(),
                transaction.getType(),
                transaction.getStatus(),
                transaction.getDescription(),
                transaction.getCreatedAt(),
                duplicate
        );
    }

}
