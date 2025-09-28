package com.devsu.user.infrastructure.persistence.repository;

import com.devsu.user.infrastructure.persistence.entity.PersonJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonJpaRepository extends JpaRepository<PersonJpaEntity, Long> {

    boolean existsByIdentificationNumber(String identificationNumber);

    Optional<PersonJpaEntity> findByIdentificationNumber(String identificationNumber);
}
