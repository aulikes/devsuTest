package com.devsu.transaction.domain.model.money;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

class MoneyTest {

    @Test
    void shouldAddCorrectly() {
        // Se crea un monto base
        Money base = Money.of(new BigDecimal("100.00"));

        // Se suma 25.30 al monto base
        Money result = base.add(Money.of(new BigDecimal("25.30")));

        // Se verifica el resultado de la suma
        assertThat(result.toString()).isEqualTo("125.30");
        // Se verifica que el objeto original permanezca sin cambios si la implementación es inmutable
        assertThat(base.toString()).isEqualTo("100.00");
    }

    @Test
    void shouldSubtractCorrectly() {
        // Se crea un monto base
        Money base = Money.of(new BigDecimal("100.00"));

        // Se resta 40.00 al monto base
        Money result = base.subtract(Money.of(new BigDecimal("40.00")));

        // Se verifica el resultado de la resta
        assertThat(result.toString()).isEqualTo("60.00");
    }

    @Test
    void shouldIdentifySigns() {
        // Se crean montos con valores positivo, cero y negativo
        Money pos = Money.of(new BigDecimal("10.00"));
        Money zero = Money.of(new BigDecimal("0.00"));
        Money neg = Money.of(new BigDecimal("-1.00"));

        // Se verifican los predicados de signo
        assertThat(pos.isPositive()).isTrue();
        assertThat(pos.isZero()).isFalse();
        assertThat(pos.isNegative()).isFalse();

        assertThat(zero.isZero()).isTrue();
        assertThat(zero.isPositive()).isFalse();
        assertThat(zero.isNegative()).isFalse();

        assertThat(neg.isNegative()).isTrue();
        assertThat(neg.isZero()).isFalse();
        assertThat(neg.isPositive()).isFalse();
    }
}
