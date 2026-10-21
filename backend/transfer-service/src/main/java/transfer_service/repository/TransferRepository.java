package transfer_service.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import transfer_service.entity.Transfer;

import java.util.Optional;

public interface TransferRepository extends JpaRepository<Transfer, Long> {
    Optional<Transfer> findByIdempotencyKey(String idempotencyKey);
}
