package com.devsu.user.infrastructure.persistence.adapter;

import com.devsu.user.application.port.CatalogQueryPort;
import com.devsu.user.infrastructure.persistence.repository.GenderJpaRepository;
import com.devsu.user.infrastructure.persistence.repository.IdentificationTypeJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adaptador JPA que implementa el puerto de consulta de catálogos.
 */
@Repository
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class JpaCatalogQueryAdapter implements CatalogQueryPort {

    private final GenderJpaRepository genderRepo;
    private final IdentificationTypeJpaRepository idTypeRepo;

    // Se consulta existencia del género por código
    @Override
    public boolean genderExists(String genderCode) {
        return genderRepo.findByCode(genderCode).isPresent();
    }

    // Se consulta existencia del tipo de identificación por código
    @Override
    public boolean identificationTypeExists(String idTypeCode) {
        return idTypeRepo.findByCode(idTypeCode).isPresent();
    }
}
