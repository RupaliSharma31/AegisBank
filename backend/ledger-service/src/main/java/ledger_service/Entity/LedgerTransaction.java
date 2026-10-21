package ledger_service.Entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "ledger_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LedgerTransaction {
    @Column(
            name = "transaction_ref",
            nullable = false,
            unique = true,
            length = 36
    )
    private final String transactionRef = UUID.randomUUID().toString();
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(
            name = "idempotency_key",
            unique = true,
            length = 120
    )
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "transaction_type",
            nullable = false,
            length = 24
    )
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private TransactionStatus status = TransactionStatus.POSTED;

    @Column(length = 255)
    private String description;

    @Column(name = "reversal_of_id")
    private Long reversalOfId;
    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,

            updatable = false
    )
    private Instant createdAt;
    @Column(name = "request_hash", length = 64)
    private String requestHash;


    public LedgerTransaction(
            TransactionType type,
            String description,
            String idempotencyKey,
            Long reversalOfId,
            String requestHash
    ) {
        this.type = type;
        this.description = description;
        this.idempotencyKey = idempotencyKey;
        this.reversalOfId = reversalOfId;
        this.requestHash = requestHash;

    }

    public void markReversed() {
        status = TransactionStatus.REVERSED;
    }
}
