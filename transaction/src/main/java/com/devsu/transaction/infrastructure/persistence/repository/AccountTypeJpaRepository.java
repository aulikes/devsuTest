package com.devsu.transaction.infrastructure.persistence.repository;

import com.devsu.transaction.infrastructure.persistence.entity.AccountTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountTypeJpaRepository extends JpaRepository<AccountTypeEntity, Long> {
    Optional<AccountTypeEntity> findByCode(String code);
    boolean existsByCode(String code);
}
