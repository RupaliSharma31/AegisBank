package ledger_service.controller;

import jakarta.validation.Valid;
import ledger_service.dto.AccountResponse;
import ledger_service.dto.OpenAccountRequest;
import ledger_service.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService s) {
        service = s;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AccountResponse open(
            @Valid @RequestBody OpenAccountRequest r) {
        return service.open(r);
    }

    @GetMapping("/{id}")
    public AccountResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PostMapping("/{id}/freeze")
    public AccountResponse freeze(@PathVariable Long id) {
        return service.freeze(id);
    }

    @PostMapping("/{id}/close")
    public AccountResponse close(@PathVariable Long id) {
        return service.close(id);
    }
}
