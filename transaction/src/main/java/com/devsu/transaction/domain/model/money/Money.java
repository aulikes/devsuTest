package com.devsu.transaction.domain.model.money;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object de dinero para el microservicio de transacciones.
 * Se replica aquí para mantener bounded context separado.
 */
public final class Money {
    // Se persiste la cantidad con escala 2
    private final BigDecimal amount;

    // Se crea el VO normalizando escala
    public static Money of(BigDecimal value) {
        Objects.requireNonNull(value, "amount");
        return new Money(value.setScale(2, BigDecimal.ROUND_HALF_UP));
    }

    // Se inicializa el VO con el monto ya normalizado
    private Money(BigDecimal amount) { this.amount = amount; }

    // Se devuelve el valor nativo
    public BigDecimal value() { return amount; }

    // Se suma otra cantidad y se retorna nuevo VO
    public Money add(Money other) {
        return of(this.amount.add(other.amount));
    }

    // Se resta otra cantidad y se retorna nuevo VO
    public Money subtract(Money other) {
        return of(this.amount.subtract(other.amount));
    }

    // compara si es mayor o igual a otra cantidad
    public boolean gte(Money other) {
        return this.amount.compareTo(other.amount) >= 0;
    }

    // valida si es positiva
    public boolean isPositive() { return amount.signum() > 0; }

    // valida si es negativa
    public boolean isNegative() { return amount.signum() < 0; }

    // Se valida si es cero
    public boolean isZero() { return amount.signum() == 0; }

    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Money)) return false;
        return amount.compareTo(((Money) o).amount) == 0;
    }
    @Override public int hashCode() { return amount.stripTrailingZeros().hashCode(); }
    @Override public String toString() { return amount.toPlainString(); }

}
