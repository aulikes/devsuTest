package com.devsu.user.application.service;

import com.devsu.user.application.command.CreateClientCommand;
import com.devsu.user.application.exception.DuplicateIdentificationException;
import com.devsu.user.application.exception.InvalidCatalogCodeException;
import com.devsu.user.application.mapper.ClientAppMapper;
import com.devsu.user.application.port.AccountNumberGenerator;
import com.devsu.user.application.port.CatalogQueryPort;
import com.devsu.user.application.port.PasswordHasher;
import com.devsu.user.application.result.ClientResult;
import com.devsu.user.domain.client.Client;
import com.devsu.user.domain.client.ClientRepository;
import com.devsu.user.domain.person.Gender;
import com.devsu.user.domain.person.IdentificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación que orquesta la creación de clientes.
 * Se aplica hashing, mapeo al dominio, reglas y persistencia.
 */
@Service
@RequiredArgsConstructor
public class CreateClientService {

    private final ClientRepository repository;
    private final PasswordHasher passwordHasher;
    private final CatalogQueryPort catalogQuery;
    private final AccountNumberGenerator accountNumberGenerator;

    // Se ejecuta el flujo completo de creación de cliente
    @Transactional
    public ClientResult execute(CreateClientCommand command) {
        // Se valida que el código de género exista
        if (!catalogQuery.genderExists(command.gender())) {
            throw new InvalidCatalogCodeException("Gender code not found: " + command.gender());
        }
        // Se valida que el código de tipo de identificación exista
        if (!catalogQuery.identificationTypeExists(command.identificationType())) {
            throw new InvalidCatalogCodeException("IdentificationType code not found: " + command.identificationType());
        }
        // Se valida unicidad por identificación si hay cambio de número
        if (repository.existsByIdentification(command.identificationNumber())) {
            throw new DuplicateIdentificationException(
                    "Identification already registered: " + command.identificationNumber());
        }
        // Se genera el número de cliente
        String generatedNumber = accountNumberGenerator.generate();

        // Se aplica hashing a la contraseña recibida
        String hashed = passwordHasher.hash(command.password());

        // Se mapea el command al dominio con enums, usando el hash
        Client toSave = Client.create(
                command.firstName(),
                command.lastName(),
                Gender.valueOf(command.gender().toUpperCase()),
                command.birthDate(),
                IdentificationType.valueOf(command.identificationType().toUpperCase()),
                command.identificationNumber(),
                command.address(),
                command.phone(),
                generatedNumber,
                hashed);

        // Se delega el registro al servicio de dominio (unidades e invariantes)
        Client saved = repository.save(toSave);

        // Se mapea el agregado persistido a Result para infraestructura
        return ClientAppMapper.toResult(saved);
    }
}
