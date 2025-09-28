package com.devsu.transaction.application.service;

import com.devsu.transaction.application.command.CreateMovementCommand;
import com.devsu.transaction.application.result.MovementResult;
import com.devsu.transaction.domain.exception.InsufficientFundsException;
import com.devsu.transaction.domain.model.account.*;
import com.devsu.transaction.domain.model.money.Money;
import com.devsu.transaction.domain.repository.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Se valida el comportamiento del caso de uso de creación de movimientos.
 */
@ExtendWith(MockitoExtension.class)
class CreateMovementServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private CreateMovementService service;

    private Account persistedActive(String number, String balance) {
        return Account.fromPersistence(
                1L,
                number,
                AccountType.AHORROS,
                Money.of(new BigDecimal(balance)),
                "1001L",
                Instant.now(),
                true,
                new ArrayList<>(),
                Money.of(new BigDecimal(balance))
        );
    }

    @BeforeEach
    void resetMocks() {
        reset(accountRepository);
    }

    @Test
    void shouldRegisterDepositAndReturnPersistedMovement() {
        // Se prepara la cuenta encontrada por número
        Account loaded = persistedActive("ACC-100", "100.00");
        when(accountRepository.findByAccountNumber("ACC-100"))
                .thenReturn(Optional.of(loaded));

        // Se simula el estado que devolvería la persistencia al guardar la cuenta
        Account returnedBySave = Account.fromPersistence(
                1L,
                "ACC-100",
                AccountType.AHORROS,
                Money.of(new BigDecimal("100.00")),
                "1001L",
                loaded.getCreatedAt(),
                true,
                List.of(), // no es necesario poblar movimientos aquí
                Money.of(new BigDecimal("150.00"))
        );
        when(accountRepository.save(any(Account.class))).thenReturn(returnedBySave);

        // Se simula la búsqueda por (accountId, uuid) utilizada por el servicio tras registrar el movimiento
        Instant now = Instant.now();
        when(accountRepository.findMovementByAccountIdAndUuid(eq(1L), anyString()))
                .thenReturn(Optional.of(
                        Movement.fromPersistence(
                                10L,
                                MovementType.DEPOSIT,
                                Money.of(new BigDecimal("50.00")),
                                Money.of(new BigDecimal("150.00")),
                                now,
                                "UUID-1"
                        )
                ));

        // Se ejecuta el caso de uso con un depósito de 50.00
        MovementResult result = service.execute(new CreateMovementCommand("ACC-100", new BigDecimal("50.00")));

        // Se verifica interacción con repositorio
        verify(accountRepository).findByAccountNumber("ACC-100");
        verify(accountRepository).save(any(Account.class));
        verify(accountRepository).findMovementByAccountIdAndUuid(eq(1L), anyString());
        verifyNoMoreInteractions(accountRepository);

        // Se valida el DTO de salida
        assertThat(result).isNotNull();
        assertThat(result.id()).isEqualTo(10L);
        assertThat(result.accountId()).isEqualTo(1L);
        assertThat(result.amount().toString()).isEqualTo("50.00");
        assertThat(result.balanceAfter().toString()).isEqualTo("150.00");
        assertThat(result.happenedAt()).isEqualTo(now);
    }

    @Test
    void shouldFailWithdrawalWhenInsufficientFunds() {
        // Se prepara la cuenta con saldo 30.00
        Account loaded = persistedActive("ACC-200", "30.00");
        when(accountRepository.findByAccountNumber("ACC-200"))
                .thenReturn(Optional.of(loaded));

        // Se intenta retirar 50.00 con monto negativo
        assertThrows(InsufficientFundsException.class, () ->
                service.execute(new CreateMovementCommand("ACC-200", new BigDecimal("-50.00")))
        );

        // Se verifica que no se persiste ni se consulta el movimiento cuando el dominio rechaza la operación
        verify(accountRepository).findByAccountNumber("ACC-200");
        verify(accountRepository, never()).save(any(Account.class));
        verify(accountRepository, never()).findMovementByAccountIdAndUuid(anyLong(), anyString());
        verifyNoMoreInteractions(accountRepository);
    }
}
