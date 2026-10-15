package ledger_service.dto;

import jakarta.validation.constraints.*;

public record HoldRequest(
        @NotNull
        @Positive
        Long accountId,

        @Positive
        long amountMinor,

        @NotBlank
        @Pattern(regexp = "[A-Za-z]{3}")
        String currency,

        @NotBlank
        @Size(max = 120)
        String reference
) {

}
