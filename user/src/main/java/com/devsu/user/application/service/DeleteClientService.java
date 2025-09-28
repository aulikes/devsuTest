package com.devsu.user.application.service;

import com.devsu.user.application.exception.ClientNotFoundException;
import com.devsu.user.domain.client.Client;
import com.devsu.user.domain.client.ClientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de aplicación que realiza la baja lógica del cliente.
 * Si el cliente no existe, se lanza ClientNotFoundException.
 * Si ya está inactivo, se considera idempotente y no realiza cambios.
 */
@Service
@RequiredArgsConstructor
public class DeleteClientService {

    private final ClientRepository repository;

    // Se ejecuta la baja lógica cambiando el estado a INACTIVE
    @Transactional
    public void execute(Long id) {
        Client client = repository.findById(id)
                .orElseThrow(() -> new ClientNotFoundException("Client not found id=" + id));

        if (client.isActive()) {
            client.deactivate();
            repository.save(client);
        }
    }
}
