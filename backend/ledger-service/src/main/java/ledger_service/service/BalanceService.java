package ledger_service.service;


import ledger_service.Entity.Account;
import ledger_service.dto.BalanceResponse;
import ledger_service.exception.AccountNotFoundException;
import ledger_service.repository.AccountRepo;
import ledger_service.repository.HoldRepo;
import ledger_service.repository.LedgerEntryRepo;
import org.springframework.transaction.annotation.Transactional;

public class BalanceService {
    private final AccountRepo accounts;
    private final LedgerEntryRepo entries;
    private final HoldRepo holds;

    public BalanceService(
            AccountRepo accounts,
            LedgerEntryRepo entries,
            HoldRepo holds
    ) {
        this.accounts = accounts;
        this.entries = entries;
        this.holds = holds;
    }

    @Transactional(readOnly = true)
    public BalanceResponse get(Long id) {
        Account account = accounts.findById(id)
                .orElseThrow(AccountNotFoundException::new);

        long ledger = entries.calculateBalance(
                id,
                account.getCurrency()
        );

        long held = holds.activeHoldTotal(
                id,
                account.getCurrency()
        );

        return new BalanceResponse(
                id,
                account.getCurrency(),
                ledger,
                held,
                ledger - held
        );
    }
}
