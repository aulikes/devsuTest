package com.devsu.user.application.service;

import com.devsu.user.application.command.UpdateClientCommand;
import com.devsu.user.application.exception.ClientNotFoundException;
import com.devsu.user.application.exception.DuplicateIdentificationException;
import com.devsu.user.application.exception.InvalidCatalogCodeException;
import com.devsu.user.application.mapper.ClientAppMapper;
import com.devsu.user.application.port.CatalogQueryPort;
import com.devsu.user.application.port.PasswordHasher;
import com.devsu.user.application.result.ClientResult;
import com.devsu.user.domain.client.Client;
import com.devsu.user.domain.client.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación que orquesta la actualización de clientes.
 * Se validan catálogos, unicidad de identificación, hashing condicional y persistencia.
 */
@Service
@RequiredArgsConstructor
public class UpdateClientService {

    // Se mantiene el puerto de repositorio de clientes
    private final ClientRepository repository;
    // Se mantiene el hasher para password
    private final PasswordHasher passwordHasher;
    // Se mantiene el puerto de consulta de catálogos
    private final CatalogQueryPort catalogQuery;

    // Se ejecuta el flujo completo de actualización de cliente
    @Transactional
    public ClientResult execute(UpdateClientCommand command) {
        // Se obtiene el cliente existente o se lanza excepción de no encontrado
        Client existing = repository.findById(command.id())
                .orElseThrow(() -> new ClientNotFoundException("Client not found id=" + command.id()));

        // Se validan códigos de catálogo
        if (!catalogQuery.genderExists(command.gender())) {
            throw new InvalidCatalogCodeException("Gender code not found: " + command.gender());
        }
        if (!catalogQuery.identificationTypeExists(command.identificationType())) {
            throw new InvalidCatalogCodeException("IdentificationType code not found: " + command.identificationType());
        }

        // Se valida unicidad por identificación si hay cambio de número
        if (!existing.getIdentificationNumber().equals(command.identificationNumber())) {
            if (repository.existsByIdentification(command.identificationNumber())) {
                throw new DuplicateIdentificationException(
                        "Identification already registered: " + command.identificationNumber());
            }
        }

        // Se determina si se debe hashear una nueva contraseña
        String hashedOrNull = null;
        if (command.password() != null && !command.password().isBlank()) {
            hashedOrNull = passwordHasher.hash(command.password());
        }

        // Se aplican cambios al agregado existente
        ClientAppMapper.applyUpdate(existing, command, hashedOrNull);

        // Se persiste y se retorna el resultado
        Client saved = repository.save(existing);
        return ClientAppMapper.toResult(saved);
    }
}
