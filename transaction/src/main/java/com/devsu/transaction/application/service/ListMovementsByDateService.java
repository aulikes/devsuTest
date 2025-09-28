package com.devsu.transaction.application.service;

import com.devsu.transaction.application.exception.AccountNotFoundException;
import com.devsu.transaction.application.result.MovementResult;
import com.devsu.transaction.domain.model.account.Account;
import com.devsu.transaction.domain.model.account.Movement;
import com.devsu.transaction.domain.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ListMovementsByDateService {

    private final AccountRepository accountRepository;

    @Transactional(readOnly = true)
    public List<MovementResult> execute(String accountNumber, LocalDate from, LocalDate to) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

        Instant fromI = from.atStartOfDay(ZoneOffset.UTC).toInstant();
        Instant toI = to.plusDays(1).atStartOfDay(ZoneOffset.UTC).toInstant();

        List<Movement> movements = accountRepository
                .findMovementsByAccountIdAndDateRange(account.getId(), fromI, toI);

        return movements.stream()
                .map(m -> new MovementResult(
                        m.getId(),
                        account.getId(),
                        m.getHappenedAt(),
                        new BigDecimal(m.getAmount().toString()),
                        new BigDecimal(m.getBalanceAfter().toString()),
                        m.getUuid()
                ))
                .toList();
    }
}