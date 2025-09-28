package com.devsu.transaction.infrastructure.persistence.mappers;

import com.devsu.transaction.domain.model.account.Account;
import com.devsu.transaction.domain.model.account.AccountType;
import com.devsu.transaction.domain.model.account.Movement;
import com.devsu.transaction.domain.model.money.Money;
import com.devsu.transaction.infrastructure.persistence.entity.AccountEntity;
import com.devsu.transaction.infrastructure.persistence.entity.AccountTypeEntity;
import com.devsu.transaction.infrastructure.persistence.entity.MovementEntity;

import java.math.BigDecimal;
import java.util.List;

/**
 * Mapper puro entre el agregado de dominio (Account) y las entidades JPA.
 * - No aplica reglas de negocio.
 * - Asigna la relación inversa (MovementEntity.account) como parte del mapeo.
 */
public final class AccountPersistenceMapper {

    private AccountPersistenceMapper() {}

    /* ===================== Entity -> Domain ===================== */
    public static Account toDomain(AccountEntity e, List<MovementEntity> movementEntities) {
        List<Movement> movements = movementEntities.stream()
                .map(AccountPersistenceMapper::toDomain)
                .toList();

        return Account.fromPersistence(
                e.getId(),
                e.getAccountNumber(),
                toDomainType(e.getType()),
                Money.of(e.getInitialBalance()),
                e.getClientId(),
                e.getCreatedAt(),
                e.isActive(),
                movements,
                Money.of(e.getCurrentBalance())
        );
    }

    /** Mapea AccountEntity a dominio sin historial (carga ligera). */
    public static Account toDomainShallow(AccountEntity e) {
        return Account.fromPersistence(
                e.getId(),
                e.getAccountNumber(),
                toDomainType(e.getType()),
                Money.of(e.getInitialBalance()),
                e.getClientId(),
                e.getCreatedAt(),
                e.isActive(),
                List.of(),
                Money.of(e.getCurrentBalance())
        );
    }

    /** Mapea AccountEntity a dominio con historial completo. */
    public static Account toDomainWithMovements(AccountEntity e) {
        List<Movement> history = e.getMovements().stream()
                .map(AccountPersistenceMapper::toDomainMovement)
                .toList();

        return Account.fromPersistence(
                e.getId(),
                e.getAccountNumber(),
                toDomainType(e.getType()),
                Money.of(e.getInitialBalance()),
                e.getClientId(),
                e.getCreatedAt(),
                e.isActive(),
                history,
                Money.of(e.getCurrentBalance())
        );
    }

    /** Mapea MovementEntity a Movement (dominio). */
    public static Movement toDomainMovement(MovementEntity e) {
        return Movement.fromPersistence(
                e.getId(),
                e.getType(),
                Money.of(e.getAmount()),
                Money.of(e.getBalanceAfter()),
                e.getHappenedAt(),
                e.getUuid()
        );
    }

    /* ===================== Domain -> Entity ===================== */

    /**
     * Crea o actualiza una AccountEntity en la BD.
     */
    private static AccountEntity toEntity(Account account, AccountTypeEntity typeEntity) {
        AccountEntity e = new AccountEntity();
        e.setId(account.getId()); // puede ser null (cuenta nueva) o no (update/merge)
        e.setAccountNumber(account.getAccountNumber());
        e.setType(typeEntity);
        e.setInitialBalance(toBD(account.getInitialBalance()));
        e.setCurrentBalance(toBD(account.getCurrentBalance()));
        e.setActive(account.isActive());
        e.setClientId(account.getClientId());
        e.setCreatedAt(account.getCreatedAt());
        return e;
    }

    /**
     * Crea una AccountEntity con TODA la información (incluye movimientos).
     */
    public static AccountEntity toEntityWithMovements(Account account, AccountTypeEntity typeEntity) {
        AccountEntity e = toEntity(account, typeEntity);

        // Se construyen los hijos y se enlaza la relación inversa
        List<MovementEntity> children = account.getMovements().stream()
                .map(m -> toMovementEntityAttached(m, e))
                .toList();
        e.setMovements(children);

        return e;
    }

    public static Movement toDomain(MovementEntity e) {
        return Movement.fromPersistence(
                e.getId(),
                e.getType(),
                Money.of(e.getAmount()),
                Money.of(e.getBalanceAfter()),
                e.getHappenedAt(),
                e.getUuid()
        );
    }

    /** Convierte un Movement del dominio a MovementEntity y lo enlaza al parent. */
    private static MovementEntity toMovementEntityAttached(Movement m, AccountEntity parent) {
        MovementEntity e = new MovementEntity();
        e.setId(m.getId());
        e.setType(m.getType());
        e.setAmount(toBD(m.getAmount()));
        e.setBalanceAfter(toBD(m.getBalanceAfter()));
        e.setHappenedAt(m.getHappenedAt());
        e.setUuid(m.getUuid());
        e.setAccount(parent); // relación inversa
        return e;
    }

    /* ===================== Helpers ===================== */

    private static BigDecimal toBD(Money m) {
        return new BigDecimal(m.toString());
    }

    /** Se asume que account_types.code coincide con el enum (AHORROS | CORRIENTE). */
    private static AccountType toDomainType(AccountTypeEntity t) {
        return AccountType.valueOf(t.getCode());
    }

    /** Útil si el adapter necesita resolver el catálogo por código. */
    public static String toEntityTypeCode(AccountType type) {
        return type.name();
    }
}
