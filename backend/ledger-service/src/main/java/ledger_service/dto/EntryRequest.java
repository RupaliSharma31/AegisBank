package ledger_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Positive;
import ledger_service.Entity.Direction;

public record EntryRequest(
        @NotNull
        @Positive
        Long accountId,

        @NotNull
        Direction direction,

        @Positive
        long amountMinor,

        @NotBlank
        @Pattern(regexp = "[A-Za-z]{3}")
        String currency
) {

}
