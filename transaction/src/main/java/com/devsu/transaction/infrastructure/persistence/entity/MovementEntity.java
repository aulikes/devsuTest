package com.devsu.transaction.infrastructure.persistence.entity;

import com.devsu.transaction.domain.model.account.MovementType;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "movements")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MovementEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false, updatable = false,
            foreignKey = @ForeignKey(name = "fk_movements_account"))
    private AccountEntity account;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, updatable = false, length = 20)
    private MovementType type;

    @Column(name = "amount", nullable = false, updatable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    // Saldo inmediatamente después de aplicar el movimiento
    @Column(name = "balance_after", nullable = false, updatable = false, precision = 19, scale = 2)
    private BigDecimal balanceAfter;

    @Column(name = "uuid", nullable = false, updatable = false, unique = true, length = 36)
    private String uuid;

    @Column(name = "happened_at", nullable = false, updatable = false)
    private Instant happenedAt;

    @PrePersist
    void onCreate() {
        happenedAt = Instant.now();
    }
}
