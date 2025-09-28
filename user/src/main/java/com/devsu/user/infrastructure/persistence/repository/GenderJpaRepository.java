package com.devsu.user.infrastructure.persistence.repository;

import com.devsu.user.infrastructure.persistence.entity.GenderJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GenderJpaRepository extends JpaRepository<GenderJpaEntity, Long> {

    Optional<GenderJpaEntity> findByCode(String code);
}
