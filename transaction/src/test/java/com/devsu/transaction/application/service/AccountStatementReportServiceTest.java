package com.devsu.transaction.application.service;

import com.devsu.transaction.application.exception.DateReportException;
import com.devsu.transaction.application.result.AccountStatementReport;
import com.devsu.transaction.domain.model.account.Account;
import com.devsu.transaction.domain.model.account.AccountType;
import com.devsu.transaction.domain.model.account.Movement;
import com.devsu.transaction.domain.model.account.MovementType;
import com.devsu.transaction.domain.model.money.Money;
import com.devsu.transaction.domain.repository.AccountRepository;
import com.devsu.transaction.infrastructure.http.clients.UserClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Pruebas unitarias de AccountStatementReportService.
 * Se valida el armado del reporte, conversión de fechas y la interacción con dependencias.
 */
@ExtendWith(MockitoExtension.class)
class AccountStatementReportServiceTest {

    private AccountStatementReportService service;

    @Mock
    private AccountRepository accountRepository;

    // Se usa deep stubs para encadenar userClient.getClient(...).firstName(), etc.
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private UserClient userClient;

    @BeforeEach
    void setUp() {
        service = new AccountStatementReportService(accountRepository, userClient);
    }

    @Test
    @DisplayName("Debe construir el reporte con cuentas y movimientos dentro del rango")
    void shouldBuildReport() {
        // Datos de entrada
        String clientId = "CL-123";
        LocalDate from = LocalDate.of(2025, 1, 1);
        LocalDate to = LocalDate.of(2025, 1, 31);

        // Rango esperado en UTC
        Instant expectedFrom = from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant expectedToExclusive = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        // Se stubbean campos del cliente devuelto por Feign (deep stubs)
        given(userClient.getClient(clientId).firstName()).willReturn("Ana");
        given(userClient.getClient(clientId).lastName()).willReturn("Pérez");
        given(userClient.getClient(clientId).identificationType()).willReturn("CC");
        given(userClient.getClient(clientId).identificationNumber()).willReturn("1234567890");
        given(userClient.getClient(clientId).status()).willReturn(true);

        // Se arma una cuenta persistida con movimientos (ya filtrados por el repo)
        Account account = Account.fromPersistence(
                10L,
                "001-ABC",
                AccountType.AHORROS,
                Money.of(new BigDecimal("1000.00")),
                "999L",
                Instant.parse("2024-12-31T23:59:59Z"),
                true,
                List.of(
                        Movement.fromPersistence(
                                1L,
                                MovementType.DEPOSIT,
                                Money.of(new BigDecimal("200.00")),
                                Money.of(new BigDecimal("1200.00")),
                                Instant.parse("2025-01-05T10:15:30Z"),
                                "UUID-1"
                        ),
                        Movement.fromPersistence(
                                2L,
                                MovementType.WITHDRAWAL,
                                Money.of(new BigDecimal("100.00")),
                                Money.of(new BigDecimal("1100.00")),
                                Instant.parse("2025-01-20T09:00:00Z"),
                                "UUID-2"
                        )
                ),
                Money.of(new BigDecimal("1100.00"))
        );

        given(accountRepository.findByClientIdWithMovementsBetween(clientId, expectedFrom, expectedToExclusive))
                .willReturn(List.of(account));

        // Ejecución
        AccountStatementReport report = service.execute(clientId, from, to);

        // Asserts del rango temporal
        assertEquals(expectedFrom, report.from());
        assertEquals(expectedToExclusive.minusMillis(1), report.to());

        // Asserts de cliente
        assertEquals("Ana", report.client().firstName());
        assertEquals("Pérez", report.client().lastName());
        assertEquals("CC", report.client().identificationType());
        assertEquals("1234567890", report.client().identificationNumber());
        assertTrue(report.client().active());

        // Asserts de cuentas
        assertEquals(1, report.accounts().size());
        var acc = report.accounts().getFirst();
        assertEquals("001-ABC", acc.accountNumber());
        assertEquals("AHORROS", acc.accountType());
        assertEquals(new BigDecimal("1000.00"), acc.initialBalance());
        assertEquals(new BigDecimal("1100.00"), acc.currentBalance());
        assertTrue(acc.active());

        // Asserts de movimientos
        assertEquals(2, acc.movements().size());
        assertEquals("DEPOSIT", acc.movements().getFirst().type());
        assertEquals(new BigDecimal("200.00"), acc.movements().getFirst().amount());
        assertEquals(new BigDecimal("1200.00"), acc.movements().getFirst().balanceAfter());

        // Verificaciones de interacción
        verify(userClient, atLeastOnce()).getClient(clientId);
        verify(accountRepository, times(1))
                .findByClientIdWithMovementsBetween(clientId, expectedFrom, expectedToExclusive);
    }

    @Test
    @DisplayName("Debe lanzar DateReportException si 'from' es posterior a 'to'")
    void shouldThrowIfFromAfterTo() {
        String clientId = "CL-X";
        LocalDate from = LocalDate.of(2025, 2, 1);
        LocalDate to = LocalDate.of(2025, 1, 31);

        assertThrows(DateReportException.class, () -> service.execute(clientId, from, to));
        verifyNoInteractions(userClient, accountRepository);
    }

    @Test
    @DisplayName("Debe retornar cuentas vacías cuando el repositorio no encuentra datos")
    void shouldReturnEmptyAccountsWhenRepoEmpty() {
        String clientId = "CL-EMPTY";
        LocalDate from = LocalDate.of(2025, 3, 1);
        LocalDate to = LocalDate.of(2025, 3, 31);

        Instant expectedFrom = from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant expectedToExclusive = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        // Se simula cliente existente
        given(userClient.getClient(clientId).firstName()).willReturn("Luis");
        given(userClient.getClient(clientId).lastName()).willReturn("Gómez");
        given(userClient.getClient(clientId).identificationType()).willReturn("CE");
        given(userClient.getClient(clientId).identificationNumber()).willReturn("ABC123");
        given(userClient.getClient(clientId).status()).willReturn(true);

        // Repo sin cuentas
        given(accountRepository.findByClientIdWithMovementsBetween(clientId, expectedFrom, expectedToExclusive))
                .willReturn(List.of());

        AccountStatementReport report = service.execute(clientId, from, to);

        assertNotNull(report);
        assertTrue(report.accounts().isEmpty());
        verify(userClient, atLeastOnce()).getClient(clientId);
        verify(accountRepository, times(1))
                .findByClientIdWithMovementsBetween(clientId, expectedFrom, expectedToExclusive);
    }
}
