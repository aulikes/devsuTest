package com.devsu.transaction.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "accounts")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class AccountEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_number", updatable = false, nullable = false, length = 32)
    private String accountNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_type_id", updatable = false, nullable = false,
            foreignKey = @ForeignKey(name = "fk_accounts_type"))
    private AccountTypeEntity type;

    @Column(name = "initial_balance", updatable = false, nullable = false, precision = 19, scale = 2)
    private BigDecimal initialBalance;

    @Column(name = "current_balance", nullable = false, precision = 19, scale = 2)
    private BigDecimal currentBalance;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Column(name = "client_id", updatable = false, nullable = false)
    private String clientId;

    @Column(name = "created_at", updatable = false, nullable = false)
    private Instant createdAt;

    @OneToMany(
            mappedBy = "account",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.MERGE}, // solo inserciones por cascada y merge cuando existe
            orphanRemoval = false
    )
    @OrderBy("happenedAt ASC") // orden determinista si se materializa la colección
    private List<MovementEntity> movements = new ArrayList<>();

    @PrePersist
    void onCreate() {
        createdAt = Instant.now();
    }
}
