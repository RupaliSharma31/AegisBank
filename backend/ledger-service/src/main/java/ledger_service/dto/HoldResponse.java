package ledger_service.dto;

import ledger_service.Entity.Hold;
import ledger_service.Entity.HoldStatus;

import java.time.Instant;

public record HoldResponse(Long id,
                           Long accountId,
                           long amountMinor,
                           String currency,
                           HoldStatus status,
                           String reference,
                           Instant createdAt,
                           Instant releasedAt) {
    public static HoldResponse from(Hold hold) {
        return new HoldResponse(
                hold.getId(),
                hold.getAccount().getId(),
                hold.getAmountMinor(),
                hold.getCurrency(),
                hold.getStatus(),
                hold.getReference(),
                hold.getCreatedAt(),
                hold.getReleasedAt()
        );
    }
}
