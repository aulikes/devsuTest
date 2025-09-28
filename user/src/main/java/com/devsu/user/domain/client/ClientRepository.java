package com.devsu.user.domain.client;

import java.util.Optional;

/**
 * Puerto de dominio para persistencia/búsqueda de clientes.
 * La implementación vivirá en infraestructura.
 */
public interface ClientRepository {
    // Se busca un cliente por su ID técnico
    Optional<Client> findById(Long id);
    // Se busca un cliente por su business key clientId
    Optional<Client> findByClientId(String clientId);
    // Se guarda o actualiza un cliente
    Client save(Client client);
    // Se valida existencia por número de identificación
    boolean existsByIdentification( String identificationNumber );
}
