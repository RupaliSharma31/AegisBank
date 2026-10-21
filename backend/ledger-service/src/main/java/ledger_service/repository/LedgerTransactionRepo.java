package ledger_service.repository;

import jakarta.persistence.LockModeType;
import ledger_service.Entity.LedgerTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LedgerTransactionRepo extends JpaRepository<LedgerTransaction, Long> {
    Optional<LedgerTransaction> findByIdempotencyKey(String key);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select t from LedgerTransaction t where t.id = :id")
    Optional<LedgerTransaction> findByIdForUpdate(@Param("id") Long id);

    boolean existsByReversalOfId(Long reversalOfId);
}
