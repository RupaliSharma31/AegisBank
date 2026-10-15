package ledger_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import ledger_service.Entity.AccountType;

public record OpenAccountRequest(@NotBlank
                                   @Size(max = 100)
                                   String customerId,

                                 @NotBlank
                                   @Pattern(regexp = "[A-Za-z]{3}")
                                   String currency,

                                 @NotNull
                                  AccountType type) {
}
