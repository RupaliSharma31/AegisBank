package ledger_service.repository;

import ledger_service.Entity.LedgerTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LedgerTransactionRepo extends JpaRepository<LedgerTransaction, Long > {
    Optional<LedgerTransaction> findByIdempotencyKey(String key);
}
