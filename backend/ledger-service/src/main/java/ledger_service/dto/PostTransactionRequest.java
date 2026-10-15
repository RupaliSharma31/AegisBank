package ledger_service.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import ledger_service.Entity.TransactionType;

import java.util.List;

public record PostTransactionRequest(@NotNull
                                     TransactionType type,

                                     @Size(max = 255)
                                      String description,

                                     @NotEmpty
                                      @Size(min = 2, max = 100)
                                     List<@Valid EntryRequest> entries) {
}
