package ledger_service.controller;

import ledger_service.dto.BalanceResponse;
import ledger_service.service.BalanceService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class BalanceController {

    private final BalanceService service;

    public BalanceController(BalanceService s) {
        service = s;
    }

    @GetMapping("/{id}/balance")
    public BalanceResponse balance(@PathVariable Long id) {
        return service.get(id);
    }
}
