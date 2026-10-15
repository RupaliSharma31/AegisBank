package ledger_service.service;

import ledger_service.Entity.Account;
import ledger_service.dto.AccountResponse;
import ledger_service.dto.OpenAccountRequest;
import ledger_service.exception.AccountNotFoundException;
import ledger_service.repository.AccountRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class AccountService {
    private final AccountRepo accounts;

    public AccountService(AccountRepo accounts) {
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

        account.close();

        return AccountResponse.from(account);
    }
}
