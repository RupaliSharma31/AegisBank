package ledger_service.service;


import ledger_service.Entity.*;
import ledger_service.dto.EntryRequest;
import ledger_service.dto.LedgerEntryResponse;
import ledger_service.dto.PostTransactionRequest;
import ledger_service.dto.PostTransactionResponse;
import ledger_service.exception.*;
import ledger_service.repository.AccountRepo;
import ledger_service.repository.HoldRepo;
import ledger_service.repository.LedgerEntryRepo;
import ledger_service.repository.LedgerTransactionRepo;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

public class LedgerService {
    private final AccountRepo accounts;
    private final LedgerTransactionRepo transactions;
    private final LedgerEntryRepo entries;
    private final HoldRepo holds;

    public LedgerService(
            AccountRepo accounts,
            LedgerTransactionRepo transactions,
            LedgerEntryRepo entries,
            HoldRepo holds
    ) {
        this.accounts = accounts;
        this.transactions = transactions;
        this.entries = entries;
        this.holds = holds;
    }

    @Transactional
    public PostTransactionResponse post(
            PostTransactionRequest request,
            String idempotencyKey
    ) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            var oldTransaction =
                    transactions.findByIdempotencyKey(idempotencyKey);

            if (oldTransaction.isPresent()) {
                return PostTransactionResponse.from(
                        oldTransaction.get(),
                        true
                );
            }
        }

        if (request.entries() == null || request.entries().size() < 2) {
            throw new InvalidEntryException(
                    "At least two entries are required"
            );
        }

        Map<String, long[]> totals = new HashMap<>();
        Set<Long> accountIds = new TreeSet<>();

        for (EntryRequest entry : request.entries()) {
            if (entry.amountMinor() <= 0) {
                throw new InvalidEntryException(
                        "amountMinor must be positive"
                );
            }

            String currency =
                    entry.currency().toUpperCase(Locale.ROOT);

            long[] total = totals.computeIfAbsent(
                    currency,
                    key -> new long[2]
            );

            int index = entry.direction() == Direction.DEBIT ? 0 : 1;

            try {
                total[index] = Math.addExact(
                        total[index],
                        entry.amountMinor()
                );
            } catch (ArithmeticException exception) {
                throw new InvalidEntryException(
                        "Amount total overflow"
                );
            }

            accountIds.add(entry.accountId());
        }

        for (long[] total : totals.values()) {
            if (total[0] != total[1]) {
                throw new UnbalancedTransactionException();
            }
        }

        Map<Long, Account> lockedAccounts = new HashMap<>();

        for (Long accountId : accountIds) {
            Account account = accounts.findByIdForUpdate(accountId)
                    .orElseThrow(AccountNotFoundException::new);

            lockedAccounts.put(accountId, account);
        }

        Map<String, Long> debitTotals = new HashMap<>();

        for (EntryRequest entry : request.entries()) {
            Account account = lockedAccounts.get(entry.accountId());

            String currency =
                    entry.currency().toUpperCase(Locale.ROOT);

            if (account.getStatus() !=
                    ledger_service.Entity.AccountStatus.ACTIVE) {
                throw new AccountNotActiveException(
                        "Account " + account.getId() + " must be ACTIVE"
                );
            }

            if (!account.getCurrency().equals(currency)) {
                throw new CurrencyMismatchException(
                        "Account " + account.getId()
                                + " currency is " + account.getCurrency()
                                + ", entry currency is " + currency
                );
            }

            if (entry.direction() == Direction.DEBIT
                    && account.getType() == AccountType.CUSTOMER) {

                String key = account.getId() + ":" + currency;

                try {
                    debitTotals.merge(
                            key,
                            entry.amountMinor(),
                            Math::addExact
                    );
                } catch (ArithmeticException exception) {
                    throw new InvalidEntryException(
                            "Debit total overflow"
                    );
                }
            }
        }

        for (Map.Entry<String, Long> debit : debitTotals.entrySet()) {
            String[] parts = debit.getKey().split(":", 2);

            Long accountId = Long.valueOf(parts[0]);
            String currency = parts[1];

            long balance = entries.calculateBalance(
                    accountId,
                    currency
            );

            long held = holds.activeHoldTotal(
                    accountId,
                    currency
            );

            if (balance - held < debit.getValue()) {
                throw new InsufficientFundsException();
            }
        }

        LedgerTransaction transaction = transactions.save(
                new LedgerTransaction(
                        request.type(),
                        request.description(),
                        blankToNull(idempotencyKey),
                        null
                )
        );

        for (EntryRequest entry : request.entries()) {
            entries.save(
                    new LedgerEntry(
                            transaction,
                            lockedAccounts.get(entry.accountId()),
                            entry.direction(),
                            entry.amountMinor(),
                            entry.currency().toUpperCase(Locale.ROOT)
                    )
            );
        }

        return PostTransactionResponse.from(transaction, false);
    }

    @Transactional
    public PostTransactionResponse reverse(Long id, String reason) {
        LedgerTransaction original = transactions.findById(id)
                .orElseThrow(TransactionNotFoundException::new);

        if (original.getStatus() == TransactionStatus.REVERSED) {
            throw new AlreadyReversedException();
        }

        List<LedgerEntry> originalEntries =
                entries.findByTransactionId(id);

        if (originalEntries.isEmpty()) {
            throw new InvalidEntryException(
                    "Transaction has no entries"
            );
        }

        Set<Long> accountIds = new TreeSet<>();

        originalEntries.forEach(
                entry -> accountIds.add(entry.getAccount().getId())
        );

        Map<Long, Account> lockedAccounts = new HashMap<>();

        for (Long accountId : accountIds) {
            Account account = accounts.findByIdForUpdate(accountId)
                    .orElseThrow(AccountNotFoundException::new);

            lockedAccounts.put(accountId, account);
        }

        LedgerTransaction reversal = transactions.save(
                new LedgerTransaction(
                        TransactionType.REVERSAL,
                        "Reversal of transaction " + id + ": " + reason,
                        null,
                        id
                )
        );

        for (LedgerEntry entry : originalEntries) {
            Direction opposite =
                    entry.getDirection() == Direction.DEBIT
                            ? Direction.CREDIT
                            : Direction.DEBIT;

            entries.save(
                    new LedgerEntry(
                            reversal,
                            lockedAccounts.get(entry.getAccount().getId()),
                            opposite,
                            entry.getAmountMinor(),
                            entry.getCurrency()
                    )
            );
        }

        original.markReversed();

        return PostTransactionResponse.from(reversal, false);
    }

    @Transactional(readOnly = true)
    public List<LedgerEntryResponse> accountEntries(Long accountId) {
        if (!accounts.existsById(accountId)) {
            throw new AccountNotFoundException();
        }

        return entries.findAccountEntries(accountId)
                .stream()
                .map(LedgerEntryResponse::from)
                .toList();
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}
