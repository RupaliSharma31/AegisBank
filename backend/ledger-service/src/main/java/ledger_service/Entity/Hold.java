package ledger_service.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "holds", uniqueConstraints = {@UniqueConstraint(columnNames = {"account_id", "reference"})})
@Getter
public class Hold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "amount_minor", nullable = false)
    private long amountMinor;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    private HoldStatus status = HoldStatus.ACTIVE;

    @Column(nullable = false, length = 120)
    private String reference;
    @CreationTimestamp
    @Column(
            name = "created_at",
            nullable = false,
            updatable = false
    )
    private Instant createdAt;

    @Column(name = "released_at")
    private Instant releasedAt;

    public Hold(
            Account account,
            long amountMinor,
            String currency,
            String reference
    ) {
        this.account = account;
        this.amountMinor = amountMinor;
        this.currency = currency;
        this.reference = reference;
        this.status = HoldStatus.ACTIVE;
        this.createdAt = Instant.now();
    }

    public void release() {
        if (status == HoldStatus.RELEASED) {
            return;
        }

        status = HoldStatus.RELEASED;
        releasedAt = Instant.now();
    }


}

