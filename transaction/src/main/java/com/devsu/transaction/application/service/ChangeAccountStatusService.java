package com.devsu.transaction.application.service;

import com.devsu.transaction.application.exception.AccountNotFoundException;
import com.devsu.transaction.application.mapper.AccountAppMapper;
import com.devsu.transaction.application.result.AccountResult;
import com.devsu.transaction.domain.model.account.Account;
import com.devsu.transaction.domain.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Servicio de aplicación para cambiar el estado (activo/inactivo) de una cuenta.
 * - Busca la cuenta por número.
 * - Aplica activación/desactivación de forma idempotente.
 */
@Service
@RequiredArgsConstructor
public class ChangeAccountStatusService {

    private final AccountRepository accountRepository;

    @Transactional
    public AccountResult execute(String accountNumber, boolean active) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + accountNumber));

        // Idempotencia: solo cambia si es necesario
        if (account.isActive() != active) {
            if (active) account.activate();
            else account.deactivate();
            account = accountRepository.save(account);
        }

        return AccountAppMapper.toResult(account);
    }
}
