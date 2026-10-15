package ledger_service.dto;

import ledger_service.Entity.Direction;
import ledger_service.Entity.LedgerEntry;

import java.time.Instant;

public record LedgerEntryResponse(Long id,
                                  Long transactionId,
                                  Long accountId,
                                  Direction direction,
                                  long amountMinor,
                                  String currency,
                                  Instant createdAt) {
    public static LedgerEntryResponse from(LedgerEntry entry) {
        return new LedgerEntryResponse(
                entry.getId(),
                entry.getTransaction().getId(),
                entry.getAccount().getId(),
                entry.getDirection(),
                entry.getAmountMinor(),
                entry.getCurrency(),
                entry.getCreatedAt()
        );
    }
}
