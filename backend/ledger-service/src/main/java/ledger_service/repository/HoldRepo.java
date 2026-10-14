package ledger_service.repository;

import ledger_service.Entity.Hold;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface HoldRepo extends JpaRepository<Hold, Long > {
    @Query("""
        SELECT COALESCE(SUM(h.amountMinor), 0)
        FROM Hold h
        WHERE h.account.id = :accountId
          AND h.currency = :currency
          AND h.status = ledger_service.Entity.HoldStatus.ACTIVE
        """)
    long activeHoldTotal(
            @Param("accountId") Long accountId,
            @Param("currency") String currency
    );
}
