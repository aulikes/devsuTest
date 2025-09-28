package com.devsu.transaction.infrastructure.persistence.repository;

import com.devsu.transaction.infrastructure.persistence.entity.MovementEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MovementJpaRepository extends JpaRepository<MovementEntity, Long> {
    List<MovementEntity> findByAccount_IdAndHappenedAtBetweenOrderByHappenedAtAsc(
            Long accountId, Instant from, Instant to);

    List<MovementEntity> findByAccount_AccountNumberAndHappenedAtBetweenOrderByHappenedAtAsc(
            String accountNumber, Instant from, Instant to
    );

    List<MovementEntity> findByAccount_IdInAndHappenedAtBetweenOrderByHappenedAtDesc(
            List<Long> accountIds, Instant from, Instant to);

    Optional<MovementEntity> findByAccount_IdAndUuid(Long accountId, String uuid);
}
