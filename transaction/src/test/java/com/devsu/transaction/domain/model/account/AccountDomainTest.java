package com.devsu.transaction.domain.model.account;

import com.devsu.transaction.domain.exception.AccountNotPersistedException;
import com.devsu.transaction.domain.exception.InactiveAccountException;
import com.devsu.transaction.domain.exception.InsufficientFundsException;
import com.devsu.transaction.domain.exception.InvalidAmountException;
import com.devsu.transaction.domain.model.money.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AccountDomainTest {

    private Account persistedAccountWithBalance(String accountNumber, String initial, boolean active) {
        // Se construye una cuenta “persistida” (con id) con saldo inicial indicado
        return Account.fromPersistence(
                1L,
                accountNumber,
                AccountType.AHORROS,
                Money.of(new BigDecimal(initial)),
                "1001L",
                Instant.now(),
                active,
                new ArrayList<>(),
                Money.of(new BigDecimal(initial))
        );
    }

    @Test
    void shouldDepositWhenAmountIsPositive() {
        // Se prepara una cuenta con saldo 100.00
        Account acc = persistedAccountWithBalance("ACC-001", "100.00", true);

        // Se registra un depósito de 50.00
        acc.registerMovement(new BigDecimal("50.00"));

        // Se verifica que el saldo actual sea 150.00
        assertThat(acc.getCurrentBalance().toString()).isEqualTo("150.00");
        // Se verifica que haya exactamente un movimiento
        assertThat(acc.getMovements()).hasSize(1);
        // Se verifica que el movimiento sea de tipo DEPOSIT y su balanceAfter sea 150.00
        assertThat(acc.getMovements().getFirst().getType()).isEqualTo(MovementType.DEPOSIT);
        assertThat(acc.getMovements().getFirst().getBalanceAfter().toString()).isEqualTo("150.00");
    }

    @Test
    void shouldWithdrawWhenAmountIsNegativeAndFundsAvailable() {
        // Se prepara una cuenta con saldo 100.00
        Account acc = persistedAccountWithBalance("ACC-002", "100.00", true);

        // Se registra un retiro de -40.00
        acc.registerMovement(new BigDecimal("-40.00"));

        // Se verifica que el saldo actual sea 60.00
        assertThat(acc.getCurrentBalance().toString()).isEqualTo("60.00");
        // Se verifica que haya exactamente un movimiento
        assertThat(acc.getMovements()).hasSize(1);
        // Se verifica que el movimiento sea de tipo WITHDRAWAL y su balanceAfter sea 60.00
        assertThat(acc.getMovements().getFirst().getType()).isEqualTo(MovementType.WITHDRAWAL);
        assertThat(acc.getMovements().getFirst().getBalanceAfter().toString()).isEqualTo("60.00");
    }

    @Test
    void shouldFailWithdrawalWhenInsufficientFunds() {
        // Se prepara una cuenta con saldo 30.00
        Account acc = persistedAccountWithBalance("ACC-003", "30.00", true);

        // Se intenta retirar -50.00 y se verifica que lance InsufficientFundsException
        assertThrows(InsufficientFundsException.class,
                () -> acc.registerMovement(new BigDecimal("-50.00")));

        // Se verifica que no se haya alterado el saldo ni agregado movimientos
        assertThat(acc.getCurrentBalance().toString()).isEqualTo("30.00");
        assertThat(acc.getMovements()).isEmpty();
    }

    @Test
    void shouldFailWhenAmountIsZero() {
        // Se prepara una cuenta con saldo 100.00
        Account acc = persistedAccountWithBalance("ACC-004", "100.00", true);

        // Se intenta registrar monto 0.00 y se verifica InvalidAmountException
        assertThrows(InvalidAmountException.class,
                () -> acc.registerMovement(new BigDecimal("0.00")));

        // Se verifica que no haya cambios
        assertThat(acc.getCurrentBalance().toString()).isEqualTo("100.00");
        assertThat(acc.getMovements()).isEmpty();
    }

    @Test
    void shouldFailWhenAccountIsInactive() {
        // Se prepara una cuenta inactiva con saldo 100.00
        Account acc = persistedAccountWithBalance("ACC-005", "100.00", false);

        // Se intenta un depósito y se verifica InactiveAccountException
        assertThrows(InactiveAccountException.class,
                () -> acc.registerMovement(new BigDecimal("10.00")));

        // Se verifica que no haya cambios
        assertThat(acc.getCurrentBalance().toString()).isEqualTo("100.00");
        assertThat(acc.getMovements()).isEmpty();
    }

    @Test
    void shouldFailWhenAccountHasNoId() {
        // Se construye una cuenta “nueva” sin id
        Account acc = Account.create(
                "ACC-006",
                AccountType.AHORROS,
                Money.of(new BigDecimal("100.00")),
                "1001L"
        );

        // Se intenta registrar un movimiento y se verifica AccountNotPersistedException
        assertThrows(AccountNotPersistedException.class,
                () -> acc.registerMovement(new BigDecimal("10.00")));

        // Se verifica que no haya cambios
        assertThat(acc.getCurrentBalance().toString()).isEqualTo("100.00");
        assertThat(acc.getMovements()).isEmpty();
    }
}
