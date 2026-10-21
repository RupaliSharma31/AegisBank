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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Service
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
        if (request.type() == TransactionType.REVERSAL) {
            throw new InvalidEntryException(
                    "REVERSAL type cannot be posted directly"
            );
        }

        if (request.entries() == null || request.entries().size() < 2) {
            throw new InvalidEntryException(
                    "At least two entries are required"
            );
        }

        String key = blankToNull(idempotencyKey);
        String requestHash = hash(request);

        // 1st idempotency check
        if (key != null) {
            var old = transactions.findByIdempotencyKey(key);
            if (old.isPresent()) {
                return replay(old.get(), requestHash);
            }
        }


        System.out.println("Idempotency key received: [" + key + "]");

        if (key != null) {
            var old = transactions.findByIdempotencyKey(key);

            System.out.println("Existing transaction found: " + old.isPresent());

            if (old.isPresent()) {
                return replay(old.get(), requestHash);
            }
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
                    c -> new long[2]
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

        // 2nd idempotency check (lock ke baad)
        if (key != null) {
            var old = transactions.findByIdempotencyKey(key);
            if (old.isPresent()) {
                return replay(old.get(), requestHash);
            }
        }

        Map<String, Long> debitTotals = new HashMap<>();

        for (EntryRequest entry : request.entries()) {
            Account account = lockedAccounts.get(entry.accountId());

            String currency =
                    entry.currency().toUpperCase(Locale.ROOT);

            if (account.getStatus() != AccountStatus.ACTIVE) {
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

                String debitKey = account.getId() + ":" + currency;

                try {
                    debitTotals.merge(
                            debitKey,
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

        checkFunds(debitTotals);

        LedgerTransaction transaction = transactions.save(
                new LedgerTransaction(
                        request.type(),
                        request.description(),
                        key,
                        null,
                        requestHash
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
        LedgerTransaction original = transactions.findByIdForUpdate(id)
                .orElseThrow(TransactionNotFoundException::new);

        if (original.getType() == TransactionType.REVERSAL) {
            throw new InvalidEntryException(
                    "A reversal cannot be reversed"
            );
        }

        if (original.getStatus() == TransactionStatus.REVERSED
                || transactions.existsByReversalOfId(id)) {
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

        // reversal ka DEBIT leg customer ko negative na kare
        Map<String, Long> debitTotals = new HashMap<>();

        for (LedgerEntry e : originalEntries) {
            Account acc = lockedAccounts.get(e.getAccount().getId());
            boolean reversalIsDebit = e.getDirection() == Direction.CREDIT;

            if (reversalIsDebit && acc.getType() == AccountType.CUSTOMER) {
                try {
                    debitTotals.merge(
                            acc.getId() + ":" + e.getCurrency(),
                            e.getAmountMinor(),
                            Math::addExact
                    );
                } catch (ArithmeticException ex) {
                    throw new InvalidEntryException(
                            "Debit total overflow"
                    );
                }
            }
        }

        checkFunds(debitTotals);

        LedgerTransaction reversal = transactions.save(
                new LedgerTransaction(
                        TransactionType.REVERSAL,
                        "Reversal of transaction " + id + ": " + reason,
                        null,
                        id,
                        null
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

    // ---------- helpers ----------

    private void checkFunds(Map<String, Long> debitTotals) {
        for (Map.Entry<String, Long> debit : debitTotals.entrySet()) {
            String[] parts = debit.getKey().split(":", 2);

            Long accountId = Long.valueOf(parts[0]);
            String currency = parts[1];

            long balance = entries.calculateBalance(accountId, currency);
            long held = holds.activeHoldTotal(accountId, currency);

            if (balance - held < debit.getValue()) {
                throw new InsufficientFundsException();
            }
        }
    }

    private PostTransactionResponse replay(
            LedgerTransaction old,
            String requestHash
    ) {
        if (old.getRequestHash() != null
                && !old.getRequestHash().equals(requestHash)) {
            throw new IdempotencyConflictException(
                    "Idempotency key already used with a different request"
            );
        }
        return PostTransactionResponse.from(old, true);
    }

    private String hash(PostTransactionRequest r) {
        try {
            StringBuilder sb = new StringBuilder(r.type().name());
            for (EntryRequest e : r.entries()) {
                sb.append('|').append(e.accountId())
                        .append(',').append(e.direction())
                        .append(',').append(e.amountMinor())
                        .append(',').append(e.currency().toUpperCase(Locale.ROOT));
            }
            byte[] d = MessageDigest.getInstance("SHA-256")
                    .digest(sb.toString().getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(d);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(ex);
        }
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank()
                ? null
                : value.trim();
    }
}