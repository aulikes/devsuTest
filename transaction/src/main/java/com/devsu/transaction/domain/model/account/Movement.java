package com.devsu.transaction.domain.model.account;

import com.devsu.transaction.domain.exception.InvalidAmountException;
import com.devsu.transaction.domain.model.money.Money;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Entidad Movimiento (hijo del agregado Account).
 * Solo el agregado puede crear instancias nuevas (fábrica con visibilidad de paquete).
 */
public class Movement {

    private final Long id;               // nulo hasta persistir
    private final Instant happenedAt;
    private final MovementType type;
    private final Money amount;
    private final Money balanceAfter;
    private final String uuid;

    private Movement(Long id, MovementType type, Money amount, Money balanceAfter, Instant happenedAt, String uuid) {
        this.id = id;
        this.type = Objects.requireNonNull(type, "type");
        this.happenedAt = Objects.requireNonNull(happenedAt, "happenedAt");
        if (Objects.requireNonNull(amount, "amount").isZero())
            throw new InvalidAmountException("amount != 0");
        this.amount = amount;
        this.balanceAfter = Objects.requireNonNull(balanceAfter, "balanceAfter");
        this.uuid = Objects.requireNonNull(uuid, "uuid Movement");
    }

    // ===== Fábricas =====
    /** Solo el paquete clases dentro del paq. pueden crear movimientos nuevos del dominio. */
    static Movement create(MovementType type, Money amount, Money balanceAfter) {
        String uuid = UUID.randomUUID().toString();
        return new Movement(null, type, amount, balanceAfter, Instant.now(), uuid);
    }

    /** Pública para rehidratación desde persistencia. */
    public static Movement fromPersistence(Long id, MovementType type, Money amount,
                                           Money balanceAfter, Instant happenedAt, String uuid) {
        if (id == null) throw new IllegalArgumentException("El id no puede ser nulo");
        return new Movement(id, type, amount, balanceAfter, happenedAt, uuid);
    }

    // ===== Getters =====
    public Long getId() { return id; }
    public Instant getHappenedAt() { return happenedAt; }
    public MovementType getType() { return type; }
    public Money getAmount() { return amount; }
    public Money getBalanceAfter() { return balanceAfter; }
    public String getUuid() {return uuid;}
}
