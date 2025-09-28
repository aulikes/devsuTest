package com.devsu.transaction.application.service;

import com.devsu.transaction.application.exception.AccountNotFoundException;
import com.devsu.transaction.application.result.MovementResult;
import com.devsu.transaction.domain.model.account.Account;
import com.devsu.transaction.domain.model.account.Movement;
import com.devsu.transaction.domain.model.money.Money;
import com.devsu.transaction.domain.repository.AccountRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ListMovementsByDateServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private ListMovementsByDateService service;

    @Test
    void execute_returnsMappedMovements_andCorrectRange() {
        // Arrange
        String accountNumber = "ACC-1";
        LocalDate from = LocalDate.of(2024, 5, 10);
        LocalDate to   = LocalDate.of(2024, 5, 12);

        Long accountId = 42L;
        Account account = mock(Account.class);
        when(account.getId()).thenReturn(accountId);
        when(accountRepository.findByAccountNumber(accountNumber))
                .thenReturn(Optional.of(account));

        Movement m1 = mock(Movement.class);
        when(m1.getId()).thenReturn(100L);
        when(m1.getHappenedAt()).thenReturn(Instant.parse("2024-05-10T10:15:30Z"));
        when(m1.getAmount()).thenReturn(Money.of(new BigDecimal("50.00")));
        when(m1.getBalanceAfter()).thenReturn(Money.of(new BigDecimal("150.00")));

        Movement m2 = mock(Movement.class);
        when(m2.getId()).thenReturn(101L);
        when(m2.getHappenedAt()).thenReturn(Instant.parse("2024-05-11T08:00:00Z"));
        when(m2.getAmount()).thenReturn(Money.of(new BigDecimal("25.50")));
        when(m2.getBalanceAfter()).thenReturn(Money.of(new BigDecimal("124.50")));

        when(accountRepository.findMovementsByAccountIdAndDateRange(eq(accountId), any(), any()))
                .thenReturn(List.of(m1, m2));

        // Act
        List<MovementResult> results = service.execute(accountNumber, from, to);

        // Assert: mapeo
        assertEquals(2, results.size());
        MovementResult r1 = results.get(0);
        MovementResult r2 = results.get(1);

        assertEquals(100L, r1.id());
        assertEquals(accountId, r1.accountId());
        assertEquals(Instant.parse("2024-05-10T10:15:30Z"), r1.happenedAt());
        assertEquals(new BigDecimal("50.00"), r1.amount());
        assertEquals(new BigDecimal("150.00"), r1.balanceAfter());

        assertEquals(101L, r2.id());
        assertEquals(accountId, r2.accountId());
        assertEquals(Instant.parse("2024-05-11T08:00:00Z"), r2.happenedAt());
        assertEquals(new BigDecimal("25.50"), r2.amount());
        assertEquals(new BigDecimal("124.50"), r2.balanceAfter());

        // Assert: rango consultado al repositorio
        ArgumentCaptor<Instant> fromCap = ArgumentCaptor.forClass(Instant.class);
        ArgumentCaptor<Instant> toCap = ArgumentCaptor.forClass(Instant.class);
        verify(accountRepository).findMovementsByAccountIdAndDateRange(eq(accountId), fromCap.capture(), toCap.capture());

        Instant expectedFrom = from.atStartOfDay(ZoneOffset.UTC).toInstant();               // 2024-05-10T00:00Z
        Instant expectedTo   = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();     // 2024-05-13T00:00Z

        assertEquals(expectedFrom, fromCap.getValue(), "from Instant incorrecto");
        assertEquals(expectedTo, toCap.getValue(), "to Instant (exclusive) incorrecto");
    }

    @Test
    void execute_throwsAccountNotFound_ifAccountDoesNotExist() {
        // Arrange
        String accountNumber = "NOPE";
        when(accountRepository.findByAccountNumber(accountNumber)).thenReturn(Optional.empty());

        // Act + Assert
        AccountNotFoundException ex = assertThrows(
                AccountNotFoundException.class,
                () -> service.execute(accountNumber, LocalDate.now(), LocalDate.now())
        );
        assertTrue(ex.getMessage().contains(accountNumber));
        verify(accountRepository, never()).findMovementsByAccountIdAndDateRange(any(), any(), any());
    }
}
