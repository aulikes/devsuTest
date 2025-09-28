package com.devsu.transaction.application.service;

import com.devsu.transaction.application.exception.DateReportException;
import com.devsu.transaction.application.result.AccountStatementReport;
import com.devsu.transaction.domain.model.account.Account;
import com.devsu.transaction.domain.model.account.Movement;
import com.devsu.transaction.domain.repository.AccountRepository;
import com.devsu.transaction.infrastructure.http.clients.UserClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountStatementReportService {

    private final AccountRepository accountRepository;
    private final UserClient userClient;  // <- Feign

    @Transactional(readOnly = true)
    public AccountStatementReport execute(String clientId, LocalDate from, LocalDate to) {
        if (from.isAfter(to)) throw new DateReportException("la fecha 'Desde' no puede ser posterior a 'Hasta'");

        Instant fromTs = from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toExclusive = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        // 1) Traer info del cliente por Feign
        var clientDto = userClient.getClient(clientId);
        var clientInfo = new AccountStatementReport.Client(
                clientDto.firstName(),
                clientDto.lastName(),
                clientDto.identificationType(),
                clientDto.identificationNumber(),
                clientDto.status()
        );

        // 2) Traer cuentas + movimientos en rango
        List<Account> accounts = accountRepository.findByClientIdWithMovementsBetween(clientId, fromTs, toExclusive);

        var accountItems = accounts.stream()
                .map(a -> new AccountStatementReport.AccountItem(
                        a.getAccountNumber(),
                        a.getType().name(),
                        new BigDecimal(a.getInitialBalance().toString()),
                        new BigDecimal(a.getCurrentBalance().toString()),
                        a.isActive(),
                        a.getMovements().stream()
                                .map(AccountStatementReportService::toMovementItem)
                                .toList()
                ))
                .toList();

        return new AccountStatementReport(
                clientInfo,
                fromTs,
                toExclusive.minusMillis(1),
                accountItems
        );
    }

    private static AccountStatementReport.MovementItem toMovementItem(Movement m) {
        return new AccountStatementReport.MovementItem(
                m.getHappenedAt(),
                m.getType().name(),
                new BigDecimal(m.getAmount().toString()),
                new BigDecimal(m.getBalanceAfter().toString())
        );
    }
}
