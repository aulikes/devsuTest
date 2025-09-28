package com.devsu.transaction.infrastructure.persistence.adapter;

import com.devsu.transaction.domain.model.account.Account;
import com.devsu.transaction.domain.model.account.Movement;
import com.devsu.transaction.domain.model.money.Money;
import com.devsu.transaction.domain.repository.AccountRepository;
import com.devsu.transaction.infrastructure.persistence.entity.AccountEntity;
import com.devsu.transaction.infrastructure.persistence.entity.AccountTypeEntity;
import com.devsu.transaction.infrastructure.persistence.entity.MovementEntity;
import com.devsu.transaction.infrastructure.persistence.mappers.AccountPersistenceMapper;
import com.devsu.transaction.infrastructure.persistence.repository.AccountJpaRepository;
import com.devsu.transaction.infrastructure.persistence.repository.AccountTypeJpaRepository;
import com.devsu.transaction.infrastructure.persistence.repository.MovementJpaRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Adapter de persistencia del Account.
 */
@Repository
@RequiredArgsConstructor
public class AccountRepositoryAdapter implements AccountRepository {

    private final AccountJpaRepository accountJpaRepository;
    private final AccountTypeJpaRepository accountTypeJpaRepository;
    private final MovementJpaRepository movementJpaRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Account> findByAccountNumber(String accountNumber) {
        return accountJpaRepository.findByAccountNumber(accountNumber)
                .map(AccountPersistenceMapper::toDomainShallow);
    }

    @Override
    @Transactional
    public Account save(Account account) {
        // Se resuelve el catálogo para mapear el tipo de cuenta
        String typeCode = AccountPersistenceMapper.toEntityTypeCode(account.getType());
        AccountTypeEntity typeEntity = accountTypeJpaRepository.findByCode(typeCode)
                .orElseThrow(() -> new EntityNotFoundException("AccountType code not found: " + typeCode));

        // Mapear el agregado completo (cuenta + movimientos)
        AccountEntity toPersist = AccountPersistenceMapper.toEntityWithMovements(account, typeEntity);

        // Guardar (Hibernate hará persist o merge según haya id)
        AccountEntity persisted = accountJpaRepository.save(toPersist);

        // Recargar con fetch de movimientos para devolver el agregado completo
        return accountJpaRepository.findByIdWithMovements(persisted.getId())
                .map(AccountPersistenceMapper::toDomainWithMovements)
                .orElseThrow(() -> new EntityNotFoundException("Account not found after save id=" + persisted.getId()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Movement> findMovementsByAccountIdAndDateRange(Long accountId, Instant from, Instant to) {
        return movementJpaRepository
                .findByAccount_IdAndHappenedAtBetweenOrderByHappenedAtAsc(accountId, from, to)
                .stream()
                .map(AccountPersistenceMapper::toDomain) // ya tienes este método: MovementEntity -> Movement
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Movement> findMovementByAccountIdAndUuid(Long accountId, String uuid) {
        return movementJpaRepository
                .findByAccount_IdAndUuid(accountId, uuid)
                .map(AccountPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Account> findByClientIdWithMovementsBetween(String clientId, Instant from, Instant to) {
        var accountEntities = accountJpaRepository.findByClientId(clientId);
        if (accountEntities.isEmpty()) return List.of();

        var ids = accountEntities.stream().map(AccountEntity::getId).toList();

        var movementEntities =
                movementJpaRepository.findByAccount_IdInAndHappenedAtBetweenOrderByHappenedAtDesc(ids, from, to);

        // Agrupar movimientos por cuenta
        Map<Long, List<MovementEntity>> byAccount = movementEntities.stream()
                .collect(Collectors.groupingBy(me -> me.getAccount().getId()));

        // Mapear a dominio inyectando SOLO movimientos en rango
        return accountEntities.stream()
                .map(ae -> AccountPersistenceMapper.toDomain(ae,
                        byAccount.getOrDefault(ae.getId(), List.of())))
                .toList();
    }
}
