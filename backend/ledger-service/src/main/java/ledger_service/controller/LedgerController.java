package ledger_service.controller;

import jakarta.validation.Valid;
import ledger_service.dto.LedgerEntryResponse;
import ledger_service.dto.PostTransactionRequest;
import ledger_service.dto.PostTransactionResponse;
import ledger_service.dto.ReverseRequest;
import ledger_service.service.LedgerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ledger")
public class LedgerController {

    private final LedgerService service;

    public LedgerController(LedgerService s) {
        service = s;
    }

    @PostMapping("/post")
    public PostTransactionResponse post(
            @RequestHeader(value = "Idempotency-Key", required = false) String key,
            @Valid @RequestBody PostTransactionRequest r) {
        return service.post(r, key);
    }

    @PostMapping("/{transactionId}/reverse")
    public PostTransactionResponse reverse(
            @PathVariable Long transactionId,
            @Valid @RequestBody ReverseRequest r) {
        return service.reverse(transactionId, r.reason());
    }

    @GetMapping("/accounts/{accountId}/entries")
    public List<LedgerEntryResponse> entries(
            @PathVariable Long accountId) {
        return service.accountEntries(accountId);
    }
}
