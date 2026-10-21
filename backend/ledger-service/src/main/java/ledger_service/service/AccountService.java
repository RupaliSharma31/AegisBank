package ledger_service.service;

import ledger_service.Entity.Account;
import ledger_service.dto.AccountResponse;
import ledger_service.dto.OpenAccountRequest;
import ledger_service.exception.AccountNotFoundException;
import ledger_service.repository.AccountRepo;
import ledger_service.repository.HoldRepo;
import ledger_service.repository.LedgerEntryRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AccountService {
    private final AccountRepo accounts;

    private final LedgerEntryRepo entries;
    private final HoldRepo holds;

    public AccountService(AccountRepo accounts, LedgerEntryRepo entries,
                          HoldRepo holds) {
        this.entries = entries;
        this.holds = holds;
        this.accounts = accounts;
    }

    @Transactional
    public AccountResponse open(OpenAccountRequest request) {
        String currency = request.currency().toUpperCase(Locale.ROOT);

        Account account = new Account(
                request.customerId().trim(),
                currency,
                request.type()
        );

        return AccountResponse.from(accounts.save(account));
    }

    @Transactional(readOnly = true)
    public AccountResponse get(Long id) {
        Account account = accounts.findById(id)
                .orElseThrow(AccountNotFoundException::new);

        return AccountResponse.from(account);
    }

    @Transactional
    public AccountResponse freeze(Long id) {
        Account account = accounts.findByIdForUpdate(id)
                .orElseThrow(AccountNotFoundException::new);

        account.freeze();

        return AccountResponse.from(account);
    }

    @Transactional
    public AccountResponse close(Long id) {
        Account account = accounts.findByIdForUpdate(id)
                .orElseThrow(AccountNotFoundException::new);
        Account acc = accounts.findByIdForUpdate(id).orElseThrow(AccountNotFoundException::new);

        long balance = entries.calculateBalance(id, acc.getCurrency());
        long held = holds.activeHoldTotal(id, acc.getCurrency());
        if (balance != 0 || held != 0) {
            throw new IllegalStateException("Balance and holds must be zero to close the account");
        }

        account.close();

        return AccountResponse.from(account);
    }
}
