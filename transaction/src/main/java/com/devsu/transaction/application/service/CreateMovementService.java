package com.devsu.transaction.application.service;

import com.devsu.transaction.application.command.CreateMovementCommand;
import com.devsu.transaction.application.exception.AccountNotFoundException;
import com.devsu.transaction.application.exception.MovementNotFoundException;
import com.devsu.transaction.application.mapper.MovementAppMapper;
import com.devsu.transaction.application.result.MovementResult;
import com.devsu.transaction.domain.model.account.Account;
import com.devsu.transaction.domain.model.account.Movement;
import com.devsu.transaction.domain.repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación para registrar movimientos (depósito/retiro).
 */
@Service
@RequiredArgsConstructor
public class CreateMovementService {

    private final AccountRepository accountRepository;

    @Transactional
    public MovementResult execute(CreateMovementCommand cmd) {
        // Se localiza la cuenta; si no existe se lanza excepción técnica
        Account account = accountRepository.findByAccountNumber(cmd.accountNumber())
                .orElseThrow(() -> new AccountNotFoundException("Account not found: " + cmd.accountNumber()));

        String uuid = account.registerMovement(cmd.amount());

        // Se persiste el agregado completo; JPA insertará los movimientos nuevos
        Account persisted = accountRepository.save(account);

        // Se toma el último movimiento (por orden cronológico) ya con ID asignado
        Movement last = accountRepository.findMovementByAccountIdAndUuid(persisted.getId(), uuid)
                .orElseThrow(() -> new MovementNotFoundException("Movement not found: " + uuid));

        return MovementAppMapper.toResult(persisted, last);
    }
}
