package ledger_service.Entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;
@Entity
@Table(name = "ledger_transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class LedgerTransaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "transaction_ref",
            nullable = false,
            unique = true,
            length = 36
    )
    private String transactionRef = UUID.randomUUID().toString();

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

    @Column(
            name = "created_at",
            nullable = false,
            insertable = false,
            updatable = false
    )
    private Instant createdAt;



    public void markReversed() {
        status = TransactionStatus.REVERSED;
    }
}
