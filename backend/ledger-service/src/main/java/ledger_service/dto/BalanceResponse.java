package ledger_service.dto;

public record BalanceResponse(
        Long accountId,
        String currency,
        long ledgerBalanceMinor,
        long heldAmountMinor,
        long availableBalanceMinor
) {
}
