package com.devsu.user.infrastructure.web.controllers;

import com.devsu.user.application.service.*;
import com.devsu.user.infrastructure.web.dto.ClientResponse;
import com.devsu.user.infrastructure.web.dto.CreateClientRequest;
import com.devsu.user.infrastructure.web.dto.UpdateClientRequest;
import com.devsu.user.infrastructure.web.mappers.ClientWebMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controlador REST para recursos de clientes.
 * Este endpoint recibe el request validado, invoca el servicio de aplicación
 * y retorna la representación de respuesta apropiada.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/clientes")
public class ClientsController {

    private final CreateClientService createClientService;
    private final UpdateClientService updateClientService;
    private final GetClientByIdService getClientByIdService;
    private final GetClientByClientIdService getClientByClientIdService;
    private final DeleteClientService deleteClientService;
    private final ClientWebMapper mapper;

    // Se expone POST /clientes
    @PostMapping
    public ResponseEntity<ClientResponse> create(@Valid @RequestBody CreateClientRequest request) {
        var command = mapper.toCommand(request);
        var result = createClientService.execute(command);
        var response = mapper.toResponse(result);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // Se expone PUT /clientes/{id} para actualizar un cliente
    @PutMapping("/{id}")
    public ResponseEntity<ClientResponse> update(@PathVariable Long id,
                                                 @Valid @RequestBody UpdateClientRequest request) {
        var command = mapper.toCommand(id, request);
        var result = updateClientService.execute(command);
        return ResponseEntity.ok(mapper.toResponse(result));
    }

    //Endpoint que recupera un cliente por su identificador.
    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> getById(@PathVariable Long id) {
        var result = getClientByIdService.execute(id);
        return ResponseEntity.ok(mapper.toResponse(result));
    }

    //Endpoint que recupera un cliente por su clientId.
    @GetMapping("/clientId/{clientId}")
    public ResponseEntity<ClientResponse> getById(@PathVariable String clientId) {
        var result = getClientByClientIdService.execute(clientId);
        return ResponseEntity.ok(mapper.toResponse(result));
    }

    /**
     * Endpoint que realiza la baja lógica del cliente.
     * Retorna 204 No Content en caso de éxito.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        deleteClientService.execute(id);
        return ResponseEntity.noContent().build();
    }
}
