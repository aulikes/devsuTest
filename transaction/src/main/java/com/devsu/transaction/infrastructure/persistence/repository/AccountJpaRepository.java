package com.devsu.transaction.infrastructure.persistence.repository;

import com.devsu.transaction.infrastructure.persistence.entity.AccountEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface AccountJpaRepository extends JpaRepository<AccountEntity, Long> {

    Optional<AccountEntity> findByAccountNumber(String accountNumber);

    @Query("""
           select distinct a
           from AccountEntity a
           left join fetch a.type
           left join fetch a.movements
           where a.id = :id
           """)
    Optional<AccountEntity> findByIdWithMovements(@Param("id") Long id);

    List<AccountEntity> findByClientId(String clientId);
}
