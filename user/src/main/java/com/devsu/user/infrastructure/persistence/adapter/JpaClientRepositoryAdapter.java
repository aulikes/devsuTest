package com.devsu.user.infrastructure.persistence.adapter;

import com.devsu.user.domain.client.Client;
import com.devsu.user.domain.client.ClientRepository;
import com.devsu.user.infrastructure.persistence.entity.ClientJpaEntity;
import com.devsu.user.infrastructure.persistence.entity.GenderJpaEntity;
import com.devsu.user.infrastructure.persistence.entity.IdentificationTypeJpaEntity;
import com.devsu.user.infrastructure.persistence.mapper.ClientPersistenceMapper;
import com.devsu.user.infrastructure.persistence.repository.ClientJpaRepository;
import com.devsu.user.infrastructure.persistence.repository.GenderJpaRepository;
import com.devsu.user.infrastructure.persistence.repository.IdentificationTypeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Adaptador JPA que persiste el agregado Client (Client + Person) con un único save().
 * - CREATE: mapea el grafo completo y hace clientRepo.save(...).
 * - UPDATE: trae el agregado con person (join fetch) y hace un único clientRepo.save(...).
 * No contiene reglas de negocio; solo mapea y delega.
 */
@Repository
@Transactional
@RequiredArgsConstructor
public class JpaClientRepositoryAdapter implements ClientRepository {

    private final ClientJpaRepository clientRepo;
    private final GenderJpaRepository genderRepo;
    private final IdentificationTypeJpaRepository idTypeRepo;

    @Override
    @Transactional(readOnly = true)
    public Optional<Client> findById(Long id) {
        return clientRepo.findByIdWithPerson(id).map(ClientPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Client> findByClientId(String clientId) {
        return clientRepo.findByClientIdWithPerson(clientId).map(ClientPersistenceMapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByIdentification(String identificationNumber) {
        return clientRepo.existsByPersonIdentificationNumber(identificationNumber);
    }

    @Override
    public Client save(Client client) {
        // Referencias de catálogo (ya validadas en aplicación)
        GenderJpaEntity genderRef = genderRepo.findByCode(client.getGender().name())
                .orElseThrow(() -> new IllegalStateException("Gender not seeded: " + client.getGender().name()));
        IdentificationTypeJpaEntity idTypeRef = idTypeRepo.findByCode(client.getIdentificationType().name())
                .orElseThrow(() -> new IllegalStateException("IdentificationType not seeded: " + client.getIdentificationType().name()));

        ClientJpaEntity toPersist;

//        if (client.getId() == null) {
            // CREATE: se arma el grafo completo (client + person) y se hace un único save
            toPersist = ClientPersistenceMapper.toEntityGraphForCreate(client, genderRef, idTypeRef);
//        } else {
//            // UPDATE: se carga el agregado con la persona y se copian cambios (un único save)
//            ClientJpaEntity managed = clientRepo.findByIdWithPerson(client.getId())
//                    .orElseThrow(() -> new IllegalStateException("Client not found id=" + client.getId()));
//            ClientPersistenceMapper.copyToAggregate(client, genderRef, idTypeRef, managed);
//            toPersist = managed;
//        }

        ClientJpaEntity persisted = clientRepo.save(toPersist);
        return ClientPersistenceMapper.toDomain(persisted);
    }
}
