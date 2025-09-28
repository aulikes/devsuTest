package com.devsu.user.infrastructure.persistence.repository;

import com.devsu.user.infrastructure.persistence.entity.IdentificationTypeJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface IdentificationTypeJpaRepository extends JpaRepository<IdentificationTypeJpaEntity, Long> {

    Optional<IdentificationTypeJpaEntity> findByCode(String code);
}
