package com.devsu.transaction.domain.repository;

import com.devsu.transaction.domain.model.account.Account;
import com.devsu.transaction.domain.model.account.Movement;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AccountRepository {
    // Se busca una cuenta por su número único
    Optional<Account> findByAccountNumber(String accountNumber);
    // Se persiste la cuenta (crear/actualizar)
    Account save(Account account);
    // consultar movimientos por ID de cuenta y rango de fechas
    List<Movement> findMovementsByAccountIdAndDateRange(Long accountId, Instant from, Instant to);
    // consultar el movimiento por cuenta y uuid
    Optional<Movement> findMovementByAccountIdAndUuid(Long accountId, String uuid);
    // para reportes
    List<Account> findByClientIdWithMovementsBetween(String clientId, Instant from, Instant to);
}
