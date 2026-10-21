package ledger_service.service;


import ledger_service.Entity.Account;
import ledger_service.Entity.AccountStatus;
import ledger_service.Entity.Hold;
import ledger_service.dto.HoldRequest;
import ledger_service.dto.HoldResponse;
import ledger_service.exception.*;
import ledger_service.repository.AccountRepo;
import ledger_service.repository.HoldRepo;
import ledger_service.repository.LedgerEntryRepo;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class HoldService {
    private final AccountRepo accounts;
    private final HoldRepo holds;
    private final LedgerEntryRepo entries;

    public HoldService(
            AccountRepo accounts,
            HoldRepo holds,
            LedgerEntryRepo entries
    ) {
        this.accounts = accounts;
        this.holds = holds;
        this.entries = entries;
    }

    @Transactional
    public HoldResponse create(HoldRequest request) {
        Account account = accounts.findByIdForUpdate(request.accountId())
                .orElseThrow(AccountNotFoundException::new);

        String currency = request.currency().toUpperCase(Locale.ROOT);

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException("Account " + account.getId() + " must be ACTIVE");
        }

        if (!account.getCurrency().equals(currency)) {
            throw new CurrencyMismatchException("Account currency is " + account.getCurrency()
                    + ", hold currency is " + currency);
        }

        long balance = entries.calculateBalance(
                account.getId(),
                currency
        );

        long held = holds.activeHoldTotal(
                account.getId(),
                currency
        );

        if (balance - held < request.amountMinor()) {
            throw new InsufficientFundsException();
        }

        Hold hold = new Hold(
                account,
                request.amountMinor(),
                currency,
                request.reference()
        );

        return HoldResponse.from(holds.save(hold));
    }

    @Transactional
    public HoldResponse release(Long id) {
        Hold hold = holds.findById(id)
                .orElseThrow(HoldNotFoundException::new);

        accounts.findByIdForUpdate(hold.getAccount().getId())
                .orElseThrow(AccountNotFoundException::new);

        hold.release();

        return HoldResponse.from(hold);
    }
}
