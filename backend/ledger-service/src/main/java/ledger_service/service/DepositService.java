package ledger_service.service;


import ledger_service.Entity.*;
import ledger_service.dto.EntryRequest;
import ledger_service.dto.PostTransactionRequest;
import ledger_service.dto.PostTransactionResponse;
import ledger_service.exception.AccountNotActiveException;
import ledger_service.exception.AccountNotFoundException;
import ledger_service.exception.InvalidEntryException;
import ledger_service.repository.AccountRepo;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
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


    public PostTransactionResponse deposit(
            Long customerAccountId,
            long amountMinor,
            String reference
    ) {
        if (reference == null || reference.isBlank()) {
            throw new InvalidEntryException("reference is required");
        }
        Account customer = accounts.findById(customerAccountId)
                .orElseThrow(AccountNotFoundException::new);
        if (customer.getType() != AccountType.CUSTOMER
                || customer.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountNotActiveException(
                    "Customer account must be an ACTIVE CUSTOMER account");
        }

        Account bank = accounts.findFirstByTypeAndCurrencyAndStatus(
                        AccountType.BANK_CASH,
                        customer.getCurrency(),
                        AccountStatus.ACTIVE)
                .orElseThrow(() -> new AccountNotFoundException(
                        "No active BANK_CASH account for currency " + customer.getCurrency()));
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

        return ledger.post(request, "deposit-" + reference.trim());
    }
}
