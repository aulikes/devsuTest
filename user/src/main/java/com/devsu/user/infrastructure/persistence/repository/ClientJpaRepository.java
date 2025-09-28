package com.devsu.user.infrastructure.persistence.repository;

import com.devsu.user.infrastructure.persistence.entity.ClientJpaEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ClientJpaRepository extends JpaRepository<ClientJpaEntity, Long> {

    @EntityGraph(attributePaths = "person")
    @Query("select c from ClientJpaEntity c where c.id = :id")
    Optional<ClientJpaEntity> findByIdWithPerson(@Param("id") Long id);

    @EntityGraph(attributePaths = "person")
    @Query("select c from ClientJpaEntity c where c.clientId = :clientId")
    Optional<ClientJpaEntity> findByClientIdWithPerson(@Param("clientId") String clientId);

    @Query("select exists (select 1 from PersonJpaEntity p where p.identificationNumber = :num)")
    boolean existsByPersonIdentificationNumber(@Param("num") String identificationNumber);

    // Se busca un cliente por su clientId (business key)
    Optional<ClientJpaEntity> findByClientId(String clientId);
}
