package ledger_service.controller;

import jakarta.validation.Valid;
import ledger_service.dto.HoldRequest;
import ledger_service.dto.HoldResponse;
import ledger_service.service.HoldService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/holds")
public class HoldController {

    private final HoldService service;

    public HoldController(HoldService s) {
        service = s;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public HoldResponse create(
            @Valid @RequestBody HoldRequest r) {
        return service.create(r);
    }

    @DeleteMapping("/{id}")
    public HoldResponse release(@PathVariable Long id) {
        return service.release(id);
    }
}
