package ledger_service.repository;

import ledger_service.Entity.LedgerEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LedgerEntryRepo extends JpaRepository<ledger_service.Entity.LedgerEntry, Long> {

    @Query("""
            SELECT COALESCE(SUM(
                CASE
                    WHEN e.direction = ledger_service.Entity.Direction.CREDIT
                    THEN e.amountMinor
                    ELSE -e.amountMinor
                END
            ), 0)
            FROM LedgerEntry e
            WHERE e.account.id = :accountId
              AND e.currency = :currency
            """)
    long calculateBalance(@Param("accountId") Long accountId, @Param("currency") String currency);

    @Query("""
            SELECT e FROM LedgerEntry e
            JOIN FETCH e.transaction
            WHERE e.account.id = :accountId
            ORDER BY e.id DESC
            """)
    List<LedgerEntry> findAccountEntries(@Param("accountId") Long accountId);

    List<LedgerEntry> findByTransactionId(Long transactionId);
}
