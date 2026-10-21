package transfer_service.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(
        name = "transfers",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_transfers_idempotency_key",
                        columnNames = "idempotency_key"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transfer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "sender_account_id", nullable = false)
    private Long senderAccountId;

    @Column(name = "recipient_account_id", nullable = false)
    private Long recipientAccountId;

    @Column(name = "amount_minor", nullable = false)
    private long amountMinor;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(length = 255)
    private String description;

    @Column(name = "idempotency_key", nullable = false, length = 120, unique = true)
    private String idempotencyKey;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TransferStatus status;

    @Column(name = "ledger_response", columnDefinition = "JSON")
    private String ledgerResponse;

    @Column(name = "failure_message", length = 1000)
    private String failureMessage;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    public Transfer(
            Long senderAccountId,
            Long recipientAccountId,
            long amountMinor,
            String currency,
            String description,
            String idempotencyKey
    ) {
        this.senderAccountId = senderAccountId;
        this.recipientAccountId = recipientAccountId;
        this.amountMinor = amountMinor;
        this.currency = currency;
        this.description = description;
        this.idempotencyKey = idempotencyKey;
        this.status = TransferStatus.INITIATED;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = Instant.now();
    }

    public void markCompleted(String ledgerResponse) {
        this.status = TransferStatus.COMPLETED;
        this.ledgerResponse = ledgerResponse;
        this.failureMessage = null;
    }

    public void markFailed(String failureMessage) {
        this.status = TransferStatus.FAILED;
        this.failureMessage = failureMessage;
    }

    public boolean hasSameRequest(
            Long sender,
            Long recipient,
            long amount,
            String currency
    ) {
        return senderAccountId.equals(sender)
                && recipientAccountId.equals(recipient)
                && amountMinor == amount
                && this.currency.equalsIgnoreCase(currency);
    }
}