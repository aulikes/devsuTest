package com.devsu.user.infrastructure.persistence.mapper;

import com.devsu.user.domain.client.Client;
import com.devsu.user.domain.person.Gender;
import com.devsu.user.domain.person.IdentificationType;
import com.devsu.user.infrastructure.persistence.entity.ClientJpaEntity;
import com.devsu.user.infrastructure.persistence.entity.GenderJpaEntity;
import com.devsu.user.infrastructure.persistence.entity.IdentificationTypeJpaEntity;
import com.devsu.user.infrastructure.persistence.entity.PersonJpaEntity;

/**
 * Mapper de persistencia que traduce entre el modelo de dominio y las entidades JPA.
 */
public final class ClientPersistenceMapper {

    private ClientPersistenceMapper() {}

    /**
     * Construye el grafo completo (Client + Person) listo para persistir con un único save().
     * Se asume que los catálogos (género y tipo de identificación) ya han sido resueltos.
     */
    public static ClientJpaEntity toEntityGraphForCreate(Client domain,
                                                         GenderJpaEntity genderRef,
                                                         IdentificationTypeJpaEntity idTypeRef) {
        // Se arma la persona desde el dominio y catálogos
        PersonJpaEntity person = toPersonEntity(domain, genderRef, idTypeRef);

        // Se arma el cliente y se enlaza la persona (cascade PERSIST/MERGE hará el resto)
        return toClientEntity(domain, person);
    }

    // Se construye una entidad PersonJpaEntity a partir del dominio y catálogos ya resueltos
    public static PersonJpaEntity toPersonEntity(Client domain,
                                                 GenderJpaEntity genderRef,
                                                 IdentificationTypeJpaEntity idTypeRef) {
        return new PersonJpaEntity(
                domain.getIdPersona(),
                domain.getFirstName(),
                domain.getLastName(),
                domain.getBirthDate(),
                domain.getAddress(),
                domain.getPhone(),
                domain.getIdentificationNumber(),
                genderRef,
                idTypeRef
        );
    }

    // Se actualiza una entidad PersonJpaEntity existente con datos del dominio
    public static void copyToPersonEntity(Client domain,
                                          GenderJpaEntity genderRef,
                                          IdentificationTypeJpaEntity idTypeRef,
                                          PersonJpaEntity target) {
        target.setFirstName(domain.getFirstName());
        target.setLastName(domain.getLastName());
        target.setBirthDate(domain.getBirthDate());
        target.setAddress(domain.getAddress());
        target.setPhone(domain.getPhone());
        target.setIdentificationNumber(domain.getIdentificationNumber());
        target.setGender(genderRef);
        target.setIdentificationType(idTypeRef);
    }

    // Se construye una entidad ClientJpaEntity a partir del dominio y la persona persistida
    public static ClientJpaEntity toClientEntity(Client domain, PersonJpaEntity person) {
        return new ClientJpaEntity(
                domain.getId(),
                person,
                domain.getClientId(),
                domain.getPassword(),
                domain.isActive()
        );
    }

    // Se actualiza una entidad ClientJpaEntity existente con datos del dominio
    public static void copyToClientEntity(Client domain, ClientJpaEntity target) {
        target.setClientId(domain.getClientId());
        target.setPasswordHash(domain.getPassword()); // se asume ya hasheado
        target.setActive(domain.isActive());
    }

    // Se transforma ClientJpaEntity (y su Person asociada) al dominio
    public static Client toDomain(ClientJpaEntity entity) {
        var p = entity.getPerson();
        return Client.fromPersistence(
                entity.getId(),
                p.getId(),
                p.getFirstName(),
                p.getLastName(),
                Gender.valueOf(p.getGender().getCode()),
                p.getBirthDate(),
                IdentificationType.valueOf(p.getIdentificationType().getCode()),
                p.getIdentificationNumber(),
                p.getAddress(),
                p.getPhone(),
                entity.getClientId(),
                entity.getPasswordHash(),
                entity.isActive()
        );
    }
}
