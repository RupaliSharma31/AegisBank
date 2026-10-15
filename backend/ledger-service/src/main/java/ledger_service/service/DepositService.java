package ledger_service.service;


import ledger_service.Entity.*;
import ledger_service.dto.EntryRequest;
import ledger_service.dto.PostTransactionRequest;
import ledger_service.dto.PostTransactionResponse;
import ledger_service.exception.AccountNotFoundException;
import ledger_service.repository.AccountRepo;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public class DepositService {
    private final AccountRepo accounts;
    private final LedgerService ledger;

    public DepositService(
            AccountRepo accounts,
            LedgerService ledger
    ) {
        this.accounts = accounts;
        this.ledger = ledger;
    }

    @Transactional
    public PostTransactionResponse deposit(
            Long customerAccountId,
            long amountMinor,
            String reference
    ) {
        Account customer = accounts.findById(customerAccountId)
                .orElseThrow(AccountNotFoundException::new);

        Account bank = accounts.findAll()
                .stream()
                .filter(account ->
                        account.getType() == AccountType.BANK_CASH
                                && account.getCurrency().equals(customer.getCurrency())
                                && account.getStatus() == AccountStatus.ACTIVE
                )
                .findFirst()
                .orElseThrow(() ->
                        new AccountNotFoundException(
                                "No active BANK_CASH account for currency "
                                        + customer.getCurrency()
                        )
                );

        PostTransactionRequest request = new PostTransactionRequest(
                TransactionType.DEPOSIT,
                "Deposit: " + reference,
                List.of(
                        new EntryRequest(
                                bank.getId(),
                                Direction.DEBIT,
                                amountMinor,
                                bank.getCurrency()
                        ),
                        new EntryRequest(
                                customer.getId(),
                                Direction.CREDIT,
                                amountMinor,
                                customer.getCurrency()
                        )
                )
        );

        return ledger.post(request, "deposit-" + reference);
    }
}
