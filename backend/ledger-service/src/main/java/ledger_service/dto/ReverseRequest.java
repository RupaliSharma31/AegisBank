package ledger_service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReverseRequest(@NotBlank
                             @Size(max = 255)
                             String reason) {
}
