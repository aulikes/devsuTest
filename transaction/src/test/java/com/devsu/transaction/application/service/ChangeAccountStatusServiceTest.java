package com.devsu.transaction.application.service;

import com.devsu.transaction.application.result.AccountResult;
import com.devsu.transaction.domain.model.account.Account;
import com.devsu.transaction.domain.model.account.AccountType;
import com.devsu.transaction.domain.model.money.Money;
import com.devsu.transaction.domain.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Se valida el cambio de estado de cuenta y la idempotencia del servicio.
 */
@ExtendWith(MockitoExtension.class)
class ChangeAccountStatusServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private ChangeAccountStatusService service;

    private Account persisted(String number, boolean active, String initial, String current) {
        // Se crea una cuenta persistida con el estado requerido
        return Account.fromPersistence(
                7L,
                number,
                AccountType.AHORROS,
                Money.of(new BigDecimal(initial)),
                "2002L",
                Instant.now(),
                active,
                new ArrayList<>(),
                Money.of(new BigDecimal(current))
        );
    }

    @Test
    void shouldActivateWhenInactive() {
        // Se prepara cuenta inactiva
        Account loaded = persisted("ACC-777", false, "0.00", "0.00");
        when(accountRepository.findByAccountNumber("ACC-777"))
                .thenReturn(Optional.of(loaded));

        // Se simula guardado devolviendo la misma cuenta pero activa
        Account saved = persisted("ACC-777", true, "0.00", "0.00");
        when(accountRepository.save(any(Account.class))).thenReturn(saved);

        // Se ejecuta la activación
        AccountResult result = service.execute("ACC-777", true);

        // Se verifica interacción con repositorio
        verify(accountRepository).findByAccountNumber("ACC-777");
        verify(accountRepository).save(any(Account.class));

        // Se valida respuesta
        assertThat(result).isNotNull();
        assertThat(result.accountNumber()).isEqualTo("ACC-777");
        assertThat(result.active()).isTrue();
    }

    @Test
    void shouldNotSaveWhenAlreadySameStatus() {
        // Se prepara cuenta ya activa
        Account loaded = persisted("ACC-888", true, "50.00", "50.00");
        when(accountRepository.findByAccountNumber("ACC-888"))
                .thenReturn(Optional.of(loaded));

        // Se ejecuta activación idempotente
        AccountResult result = service.execute("ACC-888", true);

        // Se verifica que no se llamara a save
        verify(accountRepository).findByAccountNumber("ACC-888");
        verify(accountRepository, never()).save(any(Account.class));

        // Se valida que conserve estado
        assertThat(result.active()).isTrue();
        assertThat(result.initialBalance().toString()).isEqualTo("50.00");
        assertThat(result.currentBalance().toString()).isEqualTo("50.00");
    }
}
